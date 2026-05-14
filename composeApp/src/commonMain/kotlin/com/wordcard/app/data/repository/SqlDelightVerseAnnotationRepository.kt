package com.wordcard.app.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.wordcard.app.database.BibleDatabase
import com.wordcard.app.database.VerseAnnotationEntity
import com.wordcard.app.domain.model.HighlightColor
import com.wordcard.app.domain.model.VerseAnnotation
import com.wordcard.app.domain.repository.VerseAnnotationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SqlDelightVerseAnnotationRepository(
    private val db: BibleDatabase,
) : VerseAnnotationRepository {

    private val queries get() = db.bibleDatabaseQueries

    override fun observe(bookId: String, chapter: Int): Flow<Map<Int, VerseAnnotation>> =
        queries.selectAnnotationsForChapter(bookId, chapter.toLong())
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> rows.associate { it.verse_number.toInt() to it.toDomain() } }

    override suspend fun setBookmark(
        bookId: String,
        chapter: Int,
        verses: Set<Int>,
        bookmarked: Boolean,
    ) = withContext(Dispatchers.Default) {
        queries.transaction {
            val existing = chapterIndex(bookId, chapter)
            verses.forEach { verseNumber ->
                val current = existing[verseNumber]
                upsert(
                    bookId = bookId,
                    chapter = chapter,
                    verseNumber = verseNumber,
                    bookmarked = bookmarked,
                    highlight = current?.highlight,
                    memo = current?.memo,
                )
            }
        }
    }

    override suspend fun setHighlight(
        bookId: String,
        chapter: Int,
        verses: Set<Int>,
        color: HighlightColor?,
    ) = withContext(Dispatchers.Default) {
        queries.transaction {
            val existing = chapterIndex(bookId, chapter)
            verses.forEach { verseNumber ->
                val current = existing[verseNumber]
                upsert(
                    bookId = bookId,
                    chapter = chapter,
                    verseNumber = verseNumber,
                    bookmarked = current?.bookmarked == 1L,
                    highlight = color?.name,
                    memo = current?.memo,
                )
            }
        }
    }

    override suspend fun setMemo(
        bookId: String,
        chapter: Int,
        verseNumber: Int,
        memo: String?,
    ) = withContext(Dispatchers.Default) {
        val normalized = memo?.takeIf { it.isNotBlank() }
        queries.transaction {
            val current = chapterIndex(bookId, chapter)[verseNumber]
            upsert(
                bookId = bookId,
                chapter = chapter,
                verseNumber = verseNumber,
                bookmarked = current?.bookmarked == 1L,
                highlight = current?.highlight,
                memo = normalized,
            )
        }
    }

    private fun chapterIndex(bookId: String, chapter: Int): Map<Int, VerseAnnotationEntity> =
        queries.selectAnnotationsForChapter(bookId, chapter.toLong())
            .executeAsList()
            .associateBy { it.verse_number.toInt() }

    private fun upsert(
        bookId: String,
        chapter: Int,
        verseNumber: Int,
        bookmarked: Boolean,
        highlight: String?,
        memo: String?,
    ) {
        val empty = !bookmarked && highlight == null && memo.isNullOrBlank()
        if (empty) {
            queries.deleteAnnotation(bookId, chapter.toLong(), verseNumber.toLong())
        } else {
            queries.upsertAnnotation(
                book_id = bookId,
                chapter = chapter.toLong(),
                verse_number = verseNumber.toLong(),
                bookmarked = if (bookmarked) 1L else 0L,
                highlight = highlight,
                memo = memo,
            )
        }
    }

    private fun VerseAnnotationEntity.toDomain(): VerseAnnotation = VerseAnnotation(
        bookId = book_id,
        chapter = chapter.toInt(),
        verseNumber = verse_number.toInt(),
        bookmarked = bookmarked == 1L,
        highlight = highlight?.let { runCatching { HighlightColor.valueOf(it) }.getOrNull() },
        memo = memo,
    )
}
