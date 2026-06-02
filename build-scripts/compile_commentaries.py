#!/usr/bin/env python3
"""
Compiles whatever per-chapter JSON files exist under
`build-scripts/.commentary_cache/` into the final bundled
`composeApp/src/commonMain/composeResources/files/commentaries.json`.

This is the no-API-key counterpart to `generate_commentaries.py`. When
chapters are generated interactively (by an LLM assistant writing the
cache files directly), use this script to assemble the bundle whenever
you want to ship the current progress.

Usage:
    python3 build-scripts/compile_commentaries.py

The script tolerates partial progress: it bundles only the chapters that
have cache files. Run it again after generating more chapters to refresh
the bundle.
"""
from __future__ import annotations

import json
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
KRV_PATH = ROOT / "composeApp/src/commonMain/composeResources/files/krv.json"
OUT_PATH = ROOT / "composeApp/src/commonMain/composeResources/files/commentaries.json"
CACHE_DIR = pathlib.Path(__file__).resolve().parent / ".commentary_cache"

MODEL = "claude-opus-4-7"


def main() -> None:
    if not CACHE_DIR.exists():
        print(f"no cache directory at {CACHE_DIR}", file=sys.stderr)
        sys.exit(1)

    bible = json.loads(KRV_PATH.read_text(encoding="utf-8"))
    book_order = {b["id"]: i for i, b in enumerate(bible["books"])}

    entries = []
    for path in sorted(CACHE_DIR.glob("*.json")):
        try:
            entry = json.loads(path.read_text(encoding="utf-8"))
        except Exception as e:
            print(f"  skipping {path.name}: {e}", file=sys.stderr)
            continue
        if not all(k in entry for k in ("book", "chapter", "summary", "body", "qna", "generated_at")):
            print(f"  skipping {path.name}: missing fields", file=sys.stderr)
            continue
        entries.append(entry)

    entries.sort(key=lambda e: (book_order.get(e["book"], 999), e["chapter"]))

    bundle = {
        "version": "1",
        "model": MODEL,
        "entries": entries,
    }
    OUT_PATH.parent.mkdir(parents=True, exist_ok=True)
    OUT_PATH.write_text(
        json.dumps(bundle, ensure_ascii=False, indent=2),
        encoding="utf-8",
    )

    total_chapters = sum(len(b["chapters"]) for b in bible["books"])
    print(f"wrote {len(entries)}/{total_chapters} entries to {OUT_PATH.relative_to(ROOT)}")


if __name__ == "__main__":
    main()
