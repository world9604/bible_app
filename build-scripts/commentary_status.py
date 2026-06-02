#!/usr/bin/env python3
"""
Prints how many chapters have cached commentary and which books still
have missing chapters. Useful when generation is being done
interactively across multiple sessions.

Usage:
    python3 build-scripts/commentary_status.py
"""
from __future__ import annotations

import json
import pathlib

ROOT = pathlib.Path(__file__).resolve().parent.parent
KRV_PATH = ROOT / "composeApp/src/commonMain/composeResources/files/krv.json"
CACHE_DIR = pathlib.Path(__file__).resolve().parent / ".commentary_cache"


def main() -> None:
    bible = json.loads(KRV_PATH.read_text(encoding="utf-8"))

    total_chapters = 0
    done = 0
    missing_by_book: list[tuple[str, str, list[int]]] = []

    for book in bible["books"]:
        book_id = book["id"]
        book_name = book["name"]
        book_missing: list[int] = []
        for ch in book["chapters"]:
            total_chapters += 1
            path = CACHE_DIR / f"{book_id}_{ch['n']:03d}.json"
            if path.exists():
                done += 1
            else:
                book_missing.append(ch["n"])
        if book_missing:
            missing_by_book.append((book_id, book_name, book_missing))

    pct = (done / total_chapters * 100) if total_chapters else 0
    print(f"progress: {done}/{total_chapters} ({pct:.1f}%)")

    if missing_by_book:
        print("\nbooks with missing chapters:")
        for book_id, book_name, missing in missing_by_book:
            if len(missing) > 8:
                rng = f"{missing[0]}..{missing[-1]} ({len(missing)} missing)"
            else:
                rng = ", ".join(str(c) for c in missing)
            print(f"  {book_id} {book_name}: {rng}")


if __name__ == "__main__":
    main()
