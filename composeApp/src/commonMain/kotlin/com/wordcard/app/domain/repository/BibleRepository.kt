package com.wordcard.app.domain.repository

import com.wordcard.app.domain.model.Book
import com.wordcard.app.domain.model.Chapter

interface BibleRepository {
    suspend fun books(): List<Book>
    suspend fun book(bookId: String): Book?
    suspend fun chapter(bookId: String, chapterNumber: Int): Chapter?
}
