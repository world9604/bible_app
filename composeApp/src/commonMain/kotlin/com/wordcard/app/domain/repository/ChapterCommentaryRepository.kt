package com.wordcard.app.domain.repository

import com.wordcard.app.domain.model.ChapterCommentary
import kotlinx.coroutines.flow.Flow

interface ChapterCommentaryRepository {
    fun observe(bookId: String, chapter: Int): Flow<ChapterCommentary?>
    suspend fun get(bookId: String, chapter: Int): ChapterCommentary?
}
