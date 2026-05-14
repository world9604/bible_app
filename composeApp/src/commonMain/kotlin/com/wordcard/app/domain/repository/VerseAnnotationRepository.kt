package com.wordcard.app.domain.repository

import com.wordcard.app.domain.model.HighlightColor
import com.wordcard.app.domain.model.VerseAnnotation
import kotlinx.coroutines.flow.Flow

interface VerseAnnotationRepository {
    /** Emits annotations for the given chapter, keyed by verse number. */
    fun observe(bookId: String, chapter: Int): Flow<Map<Int, VerseAnnotation>>

    suspend fun setBookmark(bookId: String, chapter: Int, verses: Set<Int>, bookmarked: Boolean)

    suspend fun setHighlight(bookId: String, chapter: Int, verses: Set<Int>, color: HighlightColor?)

    suspend fun setMemo(bookId: String, chapter: Int, verseNumber: Int, memo: String?)
}
