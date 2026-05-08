package com.wordcard.app.data.mapper

import com.wordcard.app.data.model.BibleBookDto
import com.wordcard.app.data.model.ChapterDto
import com.wordcard.app.domain.model.Book
import com.wordcard.app.domain.model.Chapter
import com.wordcard.app.domain.model.Testament
import com.wordcard.app.domain.model.Verse

internal fun BibleBookDto.toBook(): Book = Book(
    id = id,
    name = name,
    abbreviation = abbr,
    testament = if (testament.equals("OLD", ignoreCase = true)) Testament.OLD else Testament.NEW,
    chapterCount = chapters.size,
)

internal fun ChapterDto.toChapter(bookId: String): Chapter = Chapter(
    bookId = bookId,
    number = n,
    verses = verses.map { v ->
        Verse(
            bookId = bookId,
            chapter = n,
            number = v.v,
            text = v.t,
        )
    },
)
