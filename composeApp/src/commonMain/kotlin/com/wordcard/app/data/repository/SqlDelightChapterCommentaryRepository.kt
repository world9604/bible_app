package com.wordcard.app.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.wordcard.app.data.source.CommentaryDataSource
import com.wordcard.app.database.BibleDatabase
import com.wordcard.app.domain.model.ChapterCommentary
import com.wordcard.app.domain.model.ChapterQna
import com.wordcard.app.domain.repository.ChapterCommentaryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Reads ChapterCommentary rows out of SQLDelight. On first access the
 * bundled `commentaries.json` resource (if present) is loaded and inserted
 * into the local database so future reads are pure SQL.
 */
class SqlDelightChapterCommentaryRepository(
    private val db: BibleDatabase,
    private val source: CommentaryDataSource,
) : ChapterCommentaryRepository {

    private val queries get() = db.bibleDatabaseQueries
    private val seedMutex = Mutex()
    @Volatile private var seeded = false

    override fun observe(bookId: String, chapter: Int): Flow<ChapterCommentary?> = flow {
        ensureSeeded()
        emitAll(
            queries.selectCommentaryForChapter(bookId, chapter.toLong())
                .asFlow()
                .mapToOneOrNull(Dispatchers.Default)
                .map { row ->
                    row?.let {
                        val qna = queries.selectQnaForChapter(it.book_id, it.chapter)
                            .executeAsList()
                            .map { qnaRow ->
                                ChapterQna(
                                    ordinal = qnaRow.ordinal.toInt(),
                                    question = qnaRow.question,
                                    answer = qnaRow.answer,
                                )
                            }
                        ChapterCommentary(
                            bookId = it.book_id,
                            chapter = it.chapter.toInt(),
                            summary = it.summary,
                            body = it.content,
                            qna = qna,
                            model = it.model,
                            generatedAt = it.generated_at,
                        )
                    }
                }
        )
    }

    override suspend fun get(bookId: String, chapter: Int): ChapterCommentary? =
        withContext(Dispatchers.Default) {
            ensureSeeded()
            val row = queries.selectCommentaryForChapter(bookId, chapter.toLong())
                .executeAsOneOrNull() ?: return@withContext null
            val qna = queries.selectQnaForChapter(row.book_id, row.chapter)
                .executeAsList()
                .map { ChapterQna(it.ordinal.toInt(), it.question, it.answer) }
            ChapterCommentary(
                bookId = row.book_id,
                chapter = row.chapter.toInt(),
                summary = row.summary,
                body = row.content,
                qna = qna,
                model = row.model,
                generatedAt = row.generated_at,
            )
        }

    private suspend fun ensureSeeded() {
        if (seeded) return
        seedMutex.withLock {
            if (seeded) return
            val file = source.loadAll()
            if (file != null) {
                val existing = queries.countCommentaries().executeAsOne()
                if (existing < file.entries.size) {
                    queries.transaction {
                        file.entries.forEach { entry ->
                            queries.upsertCommentary(
                                book_id = entry.bookId,
                                chapter = entry.chapter.toLong(),
                                summary = entry.summary,
                                content = entry.body,
                                model = file.model,
                                generated_at = entry.generatedAt,
                            )
                            queries.deleteQnaForChapter(entry.bookId, entry.chapter.toLong())
                            entry.qna.forEachIndexed { index, qna ->
                                queries.insertQna(
                                    book_id = entry.bookId,
                                    chapter = entry.chapter.toLong(),
                                    ordinal = index.toLong(),
                                    question = qna.q,
                                    answer = qna.a,
                                )
                            }
                        }
                    }
                }
            }
            seeded = true
        }
    }
}
