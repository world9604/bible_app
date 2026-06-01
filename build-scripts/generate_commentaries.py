#!/usr/bin/env python3
"""
Pre-generates AI commentary + Q&A for every chapter of the Korean Bible (KRV)
using Claude Opus 4.7. The result is written as a single bundled JSON file
that the app seeds into its local SQLite database on first launch — so the
app never has to call the Anthropic API at runtime.

Usage:
    export ANTHROPIC_API_KEY=sk-ant-...
    python3 build-scripts/generate_commentaries.py            # generate everything
    python3 build-scripts/generate_commentaries.py --book GEN # one book only
    python3 build-scripts/generate_commentaries.py --book GEN --chapter 1

Output:
    composeApp/src/commonMain/composeResources/files/commentaries.json

Per-chapter responses are also cached under build-scripts/.commentary_cache/
so re-runs only generate what's missing (and re-runs are essentially free).

Cost estimate: ~1189 chapters × ~1500 output tokens ≈ ~1.8M output tokens.
At Opus 4.7 pricing ($15/MTok input, $75/MTok output) the full run is on the
order of $130–$160 in API spend, one time. The result ships with the app.

Requires:
    pip install anthropic
"""
from __future__ import annotations

import argparse
import asyncio
import json
import os
import pathlib
import re
import sys
import time
from datetime import datetime, timezone
from typing import Any

try:
    from anthropic import AsyncAnthropic
    from anthropic._exceptions import APIStatusError, APIConnectionError
except ImportError:
    print("ERROR: pip install anthropic", file=sys.stderr)
    sys.exit(1)

ROOT = pathlib.Path(__file__).resolve().parent.parent
KRV_PATH = ROOT / "composeApp/src/commonMain/composeResources/files/krv.json"
OUT_PATH = ROOT / "composeApp/src/commonMain/composeResources/files/commentaries.json"
CACHE_DIR = pathlib.Path(__file__).resolve().parent / ".commentary_cache"

MODEL = "claude-opus-4-7"
MAX_TOKENS = 4000
CONCURRENCY = 6
MAX_RETRIES = 5
RETRY_BASE_DELAY = 2.0

SYSTEM_PROMPT = """당신은 한국 개신교 신학에 정통한 성경 해설자입니다.
주어진 성경 본문(개역한글)에 대해 다음을 한국어로 제공합니다:
1) 짧은 요약 (2~3문장)
2) 풍부한 해설 본문 (350~600자, 마크다운 사용 가능): 본문의 역사적·신학적 맥락,
   핵심 구절, 적용점을 균형있게 다룹니다. 특정 교파에 치우치지 않고
   복음주의 정통 기독교의 통상적 이해를 따릅니다.
3) 일반 독자가 궁금해할 만한 질문-답변 4개 (각 답변 100~180자):
   "이게 무슨 뜻인가요?", "왜 이런 일이 일어났나요?", "오늘날 어떻게 적용하나요?" 같은
   실질적이고 다양한 각도의 질문이어야 합니다.

응답은 반드시 다음 JSON 스키마만 포함하는 단일 JSON 객체여야 합니다.
다른 어떤 텍스트도 추가하지 마세요. JSON 시작 전이나 끝난 뒤에 어떤 설명도 붙이지 마세요:

{
  "summary": "...",
  "body": "...",
  "qna": [
    {"q": "...", "a": "..."},
    {"q": "...", "a": "..."},
    {"q": "...", "a": "..."},
    {"q": "...", "a": "..."}
  ]
}
"""


def load_bible() -> dict[str, Any]:
    with KRV_PATH.open(encoding="utf-8") as f:
        return json.load(f)


def cache_path(book_id: str, chapter: int) -> pathlib.Path:
    return CACHE_DIR / f"{book_id}_{chapter:03d}.json"


def load_cached(book_id: str, chapter: int) -> dict | None:
    path = cache_path(book_id, chapter)
    if not path.exists():
        return None
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except Exception:
        return None


def save_cached(book_id: str, chapter: int, data: dict) -> None:
    CACHE_DIR.mkdir(parents=True, exist_ok=True)
    path = cache_path(book_id, chapter)
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2), encoding="utf-8")


def build_chapter_text(verses: list[dict]) -> str:
    lines = []
    for v in verses:
        lines.append(f"{v['v']}. {v['t']}")
    return "\n".join(lines)


def extract_json(text: str) -> dict:
    """Pull the first JSON object out of a model reply, tolerating stray text."""
    text = text.strip()
    if text.startswith("```"):
        text = re.sub(r"^```(?:json)?\s*", "", text)
        text = re.sub(r"\s*```\s*$", "", text)
    start = text.find("{")
    end = text.rfind("}")
    if start == -1 or end == -1 or end <= start:
        raise ValueError(f"no JSON object in reply: {text[:200]!r}")
    return json.loads(text[start : end + 1])


async def generate_one(
    client: AsyncAnthropic,
    book_id: str,
    book_name: str,
    chapter_num: int,
    chapter_text: str,
    sem: asyncio.Semaphore,
) -> dict:
    cached = load_cached(book_id, chapter_num)
    if cached is not None:
        return cached

    user_prompt = (
        f"성경: {book_name} ({book_id}) {chapter_num}장 (개역한글)\n\n"
        f"본문:\n{chapter_text}\n\n"
        "위 본문에 대한 해설과 Q&A를 지정된 JSON 형식으로만 답하세요."
    )

    async with sem:
        for attempt in range(MAX_RETRIES):
            try:
                resp = await client.messages.create(
                    model=MODEL,
                    max_tokens=MAX_TOKENS,
                    system=SYSTEM_PROMPT,
                    messages=[{"role": "user", "content": user_prompt}],
                )
                content = "".join(
                    block.text for block in resp.content if getattr(block, "text", None)
                )
                parsed = extract_json(content)
                summary = str(parsed.get("summary", "")).strip()
                body = str(parsed.get("body", "")).strip()
                qna_raw = parsed.get("qna", []) or []
                qna = [
                    {"q": str(q.get("q", "")).strip(), "a": str(q.get("a", "")).strip()}
                    for q in qna_raw
                    if isinstance(q, dict) and q.get("q") and q.get("a")
                ]
                if not summary or not body or len(qna) < 2:
                    raise ValueError(
                        f"incomplete response: summary={len(summary)} body={len(body)} qna={len(qna)}"
                    )
                record = {
                    "book": book_id,
                    "chapter": chapter_num,
                    "summary": summary,
                    "body": body,
                    "qna": qna,
                    "generated_at": datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ"),
                }
                save_cached(book_id, chapter_num, record)
                return record
            except (APIStatusError, APIConnectionError) as e:
                wait = RETRY_BASE_DELAY * (2 ** attempt)
                print(
                    f"  [{book_id} {chapter_num}] API error (attempt {attempt + 1}/{MAX_RETRIES}): "
                    f"{e!r}; retrying in {wait:.1f}s",
                    file=sys.stderr,
                )
                await asyncio.sleep(wait)
            except (ValueError, json.JSONDecodeError) as e:
                wait = RETRY_BASE_DELAY * (2 ** attempt)
                print(
                    f"  [{book_id} {chapter_num}] parse error (attempt {attempt + 1}/{MAX_RETRIES}): "
                    f"{e}; retrying in {wait:.1f}s",
                    file=sys.stderr,
                )
                await asyncio.sleep(wait)
        raise RuntimeError(f"failed to generate {book_id} {chapter_num} after {MAX_RETRIES} attempts")


async def main_async(args: argparse.Namespace) -> None:
    if not os.environ.get("ANTHROPIC_API_KEY"):
        print("ERROR: set ANTHROPIC_API_KEY", file=sys.stderr)
        sys.exit(1)

    bible = load_bible()
    targets = []
    for book in bible["books"]:
        if args.book and book["id"] != args.book:
            continue
        for ch in book["chapters"]:
            if args.chapter is not None and ch["n"] != args.chapter:
                continue
            targets.append((book["id"], book["name"], ch["n"], build_chapter_text(ch["verses"])))

    if not targets:
        print("nothing to generate", file=sys.stderr)
        sys.exit(1)

    cached_count = sum(1 for b, _, c, _ in targets if cache_path(b, c).exists())
    print(
        f"target: {len(targets)} chapters ({cached_count} already cached, "
        f"{len(targets) - cached_count} to generate)"
    )

    client = AsyncAnthropic()
    sem = asyncio.Semaphore(CONCURRENCY)
    start = time.time()
    done = 0
    failed: list[tuple[str, int, BaseException]] = []

    async def run_one(book_id, book_name, chap_num, chap_text):
        nonlocal done
        try:
            rec = await generate_one(client, book_id, book_name, chap_num, chap_text, sem)
            done += 1
            if done % 10 == 0 or done == len(targets):
                elapsed = time.time() - start
                rate = done / elapsed if elapsed > 0 else 0
                eta = (len(targets) - done) / rate if rate > 0 else 0
                print(
                    f"  progress {done}/{len(targets)} "
                    f"({rate:.2f} ch/s, ETA {eta/60:.1f}m)",
                    flush=True,
                )
            return rec
        except BaseException as e:
            failed.append((book_id, chap_num, e))
            return None

    results = await asyncio.gather(
        *[run_one(b, n, c, t) for b, n, c, t in targets]
    )

    if failed:
        print(f"\nFAILED chapters ({len(failed)}):", file=sys.stderr)
        for b, c, e in failed:
            print(f"  {b} {c}: {e!r}", file=sys.stderr)

    if args.book or args.chapter is not None:
        print(f"\nfinished partial run ({len(targets) - len(failed)} succeeded). "
              "Re-run without filters to build the full bundle.")
        return

    succeeded = [r for r in results if r is not None]
    if len(succeeded) < len(targets):
        print(
            f"\n{len(targets) - len(succeeded)} chapters failed; not writing bundle. "
            "Re-run to retry only the missing ones (cached results are reused).",
            file=sys.stderr,
        )
        sys.exit(2)

    succeeded.sort(key=lambda r: (book_order(bible, r["book"]), r["chapter"]))
    bundle = {
        "version": "1",
        "model": MODEL,
        "entries": succeeded,
    }
    OUT_PATH.parent.mkdir(parents=True, exist_ok=True)
    OUT_PATH.write_text(json.dumps(bundle, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"\nwrote {len(succeeded)} entries to {OUT_PATH.relative_to(ROOT)}")


def book_order(bible: dict, book_id: str) -> int:
    for i, b in enumerate(bible["books"]):
        if b["id"] == book_id:
            return i
    return 999


def main() -> None:
    parser = argparse.ArgumentParser(description="Generate AI commentary for every Bible chapter.")
    parser.add_argument("--book", help="Limit to a single book id (e.g. GEN). Skips writing the bundle.")
    parser.add_argument("--chapter", type=int, help="Limit to a single chapter (with --book).")
    args = parser.parse_args()
    asyncio.run(main_async(args))


if __name__ == "__main__":
    main()
