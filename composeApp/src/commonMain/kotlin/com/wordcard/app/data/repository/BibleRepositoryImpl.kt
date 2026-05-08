package com.wordcard.app.data.repository

import com.wordcard.app.data.mapper.toBook
import com.wordcard.app.data.mapper.toChapter
import com.wordcard.app.data.model.BibleBookDto
import com.wordcard.app.data.source.BibleDataSource
import com.wordcard.app.domain.model.Book
import com.wordcard.app.domain.model.Chapter
import com.wordcard.app.domain.repository.BibleRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class BibleRepositoryImpl(
    private val source: BibleDataSource,
) : BibleRepository {

    private val mutex = Mutex()
    private var cache: List<BibleBookDto>? = null

    private suspend fun all(): List<BibleBookDto> = mutex.withLock {
        cache ?: source.loadAll().also { cache = it }
    }

    override suspend fun books(): List<Book> = all().map { it.toBook() }

    override suspend fun book(bookId: String): Book? =
        all().firstOrNull { it.id == bookId }?.toBook()

    override suspend fun chapter(bookId: String, chapterNumber: Int): Chapter? {
        val book = all().firstOrNull { it.id == bookId } ?: return null
        val chapter = book.chapters.firstOrNull { it.n == chapterNumber } ?: return null
        return chapter.toChapter(bookId)
    }
}
