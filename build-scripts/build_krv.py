#!/usr/bin/env python3
"""
Fetches the Korean Revised Version (개역한글, KRV) Bible from getBible v2 API
and emits a JSON file consumed by KrvBibleDataSource.

Usage:
    python3 build-scripts/build_krv.py

Output:
    composeApp/src/commonMain/composeResources/files/krv.json

Network: ~1189 chapter requests, throttled to ~4 req/s. Responses are cached
under build-scripts/.cache/ so re-runs are essentially free. Delete the cache
to force a fresh fetch.
"""
from __future__ import annotations

import json
import pathlib
import sys
import time
import urllib.error
import urllib.request
from typing import Any

ROOT = pathlib.Path(__file__).resolve().parent.parent
OUT = ROOT / "composeApp/src/commonMain/composeResources/files/krv.json"
CACHE = pathlib.Path(__file__).resolve().parent / ".cache"
ENDPOINT = "https://api.getbible.net/v2/korean/{book}/{chapter}.json"
SLEEP_BETWEEN = 0.25
RETRIES = 4
BACKOFF_BASE = 1.7

# (Paratext id, 한국어 이름, 약어, 'OLD'/'NEW', 장 수, getBible 책 번호)
BOOKS: list[tuple[str, str, str, str, int, int]] = [
    # 구약 39
    ("GEN", "창세기", "창", "OLD", 50, 1),
    ("EXO", "출애굽기", "출", "OLD", 40, 2),
    ("LEV", "레위기", "레", "OLD", 27, 3),
    ("NUM", "민수기", "민", "OLD", 36, 4),
    ("DEU", "신명기", "신", "OLD", 34, 5),
    ("JOS", "여호수아", "수", "OLD", 24, 6),
    ("JDG", "사사기", "삿", "OLD", 21, 7),
    ("RUT", "룻기", "룻", "OLD", 4, 8),
    ("1SA", "사무엘상", "삼상", "OLD", 31, 9),
    ("2SA", "사무엘하", "삼하", "OLD", 24, 10),
    ("1KI", "열왕기상", "왕상", "OLD", 22, 11),
    ("2KI", "열왕기하", "왕하", "OLD", 25, 12),
    ("1CH", "역대상", "대상", "OLD", 29, 13),
    ("2CH", "역대하", "대하", "OLD", 36, 14),
    ("EZR", "에스라", "스", "OLD", 10, 15),
    ("NEH", "느헤미야", "느", "OLD", 13, 16),
    ("EST", "에스더", "에", "OLD", 10, 17),
    ("JOB", "욥기", "욥", "OLD", 42, 18),
    ("PSA", "시편", "시", "OLD", 150, 19),
    ("PRO", "잠언", "잠", "OLD", 31, 20),
    ("ECC", "전도서", "전", "OLD", 12, 21),
    ("SNG", "아가", "아", "OLD", 8, 22),
    ("ISA", "이사야", "사", "OLD", 66, 23),
    ("JER", "예레미야", "렘", "OLD", 52, 24),
    ("LAM", "예레미야애가", "애", "OLD", 5, 25),
    ("EZK", "에스겔", "겔", "OLD", 48, 26),
    ("DAN", "다니엘", "단", "OLD", 12, 27),
    ("HOS", "호세아", "호", "OLD", 14, 28),
    ("JOL", "요엘", "욜", "OLD", 3, 29),
    ("AMO", "아모스", "암", "OLD", 9, 30),
    ("OBA", "오바댜", "옵", "OLD", 1, 31),
    ("JON", "요나", "욘", "OLD", 4, 32),
    ("MIC", "미가", "미", "OLD", 7, 33),
    ("NAM", "나훔", "나", "OLD", 3, 34),
    ("HAB", "하박국", "합", "OLD", 3, 35),
    ("ZEP", "스바냐", "습", "OLD", 3, 36),
    ("HAG", "학개", "학", "OLD", 2, 37),
    ("ZEC", "스가랴", "슥", "OLD", 14, 38),
    ("MAL", "말라기", "말", "OLD", 4, 39),
    # 신약 27
    ("MAT", "마태복음", "마", "NEW", 28, 40),
    ("MRK", "마가복음", "막", "NEW", 16, 41),
    ("LUK", "누가복음", "눅", "NEW", 24, 42),
    ("JHN", "요한복음", "요", "NEW", 21, 43),
    ("ACT", "사도행전", "행", "NEW", 28, 44),
    ("ROM", "로마서", "롬", "NEW", 16, 45),
    ("1CO", "고린도전서", "고전", "NEW", 16, 46),
    ("2CO", "고린도후서", "고후", "NEW", 13, 47),
    ("GAL", "갈라디아서", "갈", "NEW", 6, 48),
    ("EPH", "에베소서", "엡", "NEW", 6, 49),
    ("PHP", "빌립보서", "빌", "NEW", 4, 50),
    ("COL", "골로새서", "골", "NEW", 4, 51),
    ("1TH", "데살로니가전서", "살전", "NEW", 5, 52),
    ("2TH", "데살로니가후서", "살후", "NEW", 3, 53),
    ("1TI", "디모데전서", "딤전", "NEW", 6, 54),
    ("2TI", "디모데후서", "딤후", "NEW", 4, 55),
    ("TIT", "디도서", "딛", "NEW", 3, 56),
    ("PHM", "빌레몬서", "몬", "NEW", 1, 57),
    ("HEB", "히브리서", "히", "NEW", 13, 58),
    ("JAS", "야고보서", "약", "NEW", 5, 59),
    ("1PE", "베드로전서", "벧전", "NEW", 5, 60),
    ("2PE", "베드로후서", "벧후", "NEW", 3, 61),
    ("1JN", "요한일서", "요일", "NEW", 5, 62),
    ("2JN", "요한이서", "요이", "NEW", 1, 63),
    ("3JN", "요한삼서", "요삼", "NEW", 1, 64),
    ("JUD", "유다서", "유", "NEW", 1, 65),
    ("REV", "요한계시록", "계", "NEW", 22, 66),
]


def fetch_chapter(book_num: int, chapter: int) -> dict[str, Any]:
    """Fetch one chapter, with on-disk cache and retry/backoff."""
    cache_file = CACHE / f"{book_num:02d}_{chapter:03d}.json"
    if cache_file.exists():
        return json.loads(cache_file.read_text(encoding="utf-8"))

    url = ENDPOINT.format(book=book_num, chapter=chapter)
    last_err: Exception | None = None
    for attempt in range(RETRIES):
        try:
            req = urllib.request.Request(url, headers={"User-Agent": "wordcard-bible/0.1"})
            with urllib.request.urlopen(req, timeout=20) as r:
                raw = r.read()
            payload = json.loads(raw)
            CACHE.mkdir(parents=True, exist_ok=True)
            cache_file.write_text(json.dumps(payload, ensure_ascii=False), encoding="utf-8")
            return payload
        except (urllib.error.URLError, json.JSONDecodeError, TimeoutError) as e:
            last_err = e
            if attempt < RETRIES - 1:
                time.sleep(BACKOFF_BASE ** attempt)
    raise RuntimeError(f"failed after {RETRIES} attempts: {url} ({last_err})")


def normalize_verses(raw: dict[str, Any], book_id: str, chapter: int) -> list[dict[str, Any]]:
    """getBible v2 chapter response shape (verified):
    {
      "translation": "Korean", "abbreviation": "korean", "lang": "ko",
      "book_nr": 1, "book_name": "창세기", "chapter": 1,
      "verses": [{"chapter": 1, "verse": 1, "name": "...", "text": "..."}, ...]
    }
    """
    verses_raw = raw.get("verses")
    if not isinstance(verses_raw, list) or not verses_raw:
        raise ValueError(f"{book_id} {chapter}: missing or empty 'verses' field")
    out = []
    for v in verses_raw:
        text = (v.get("text") or "").strip()
        verse_num = v.get("verse")
        if verse_num is None:
            raise ValueError(f"{book_id} {chapter}: verse missing 'verse' field")
        if not text:
            raise ValueError(f"{book_id} {chapter}:{verse_num}: empty text")
        out.append({"v": int(verse_num), "t": text})
    return out


def build() -> None:
    books_out: list[dict[str, Any]] = []
    total_verses = 0
    print(f"[krv] fetching 66 books → {OUT.relative_to(ROOT)}", file=sys.stderr)

    for (book_id, kname, kabbr, testament, chapter_count, num) in BOOKS:
        chapters_out: list[dict[str, Any]] = []
        for ch in range(1, chapter_count + 1):
            raw = fetch_chapter(num, ch)
            verses = normalize_verses(raw, book_id, ch)
            chapters_out.append({"n": ch, "verses": verses})
            total_verses += len(verses)
            # Only sleep when we actually hit the network (cache miss path).
            cache_file = CACHE / f"{num:02d}_{ch:03d}.json"
            # Heuristic: sleep if cache existed BEFORE fetch (we just wrote it).
            # Skip sleep if the file is older than 5s — means it was cached.
            if cache_file.exists() and (time.time() - cache_file.stat().st_mtime) < 5:
                time.sleep(SLEEP_BETWEEN)
        print(
            f"  {book_id:>3} {kname:<10} {chapter_count:>3}장 {sum(len(c['verses']) for c in chapters_out):>5}절",
            file=sys.stderr,
        )
        books_out.append({
            "id": book_id,
            "name": kname,
            "abbr": kabbr,
            "testament": testament,
            "chapters": chapters_out,
        })

    if not (31_000 <= total_verses <= 31_200):
        raise AssertionError(f"unexpected total verse count: {total_verses}")
    if len(books_out) != 66:
        raise AssertionError(f"expected 66 books, got {len(books_out)}")

    payload = {
        "version": "KRV",
        "versionName": "개역한글",
        "language": "ko",
        "books": books_out,
    }
    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text(
        json.dumps(payload, ensure_ascii=False, separators=(",", ":")),
        encoding="utf-8",
    )
    size_mb = OUT.stat().st_size / (1024 * 1024)
    print(
        f"[krv] wrote {OUT.relative_to(ROOT)} — {len(books_out)} books, "
        f"{total_verses} verses, {size_mb:.2f} MB",
        file=sys.stderr,
    )


if __name__ == "__main__":
    build()
