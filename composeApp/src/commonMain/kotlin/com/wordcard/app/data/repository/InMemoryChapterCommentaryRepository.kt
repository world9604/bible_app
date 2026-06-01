package com.wordcard.app.data.repository

import com.wordcard.app.data.source.CommentaryDataSource
import com.wordcard.app.domain.model.ChapterCommentary
import com.wordcard.app.domain.model.ChapterQna
import com.wordcard.app.domain.repository.ChapterCommentaryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Fallback implementation used on platforms without SQLite (e.g. wasmJs).
 * Loads the bundled commentary file lazily and keeps it in memory.
 */
class InMemoryChapterCommentaryRepository(
    private val source: CommentaryDataSource,
) : ChapterCommentaryRepository {

    private val cache = MutableStateFlow<Map<Key, ChapterCommentary>>(emptyMap())
    private val mutex = Mutex()
    @Volatile private var loaded = false

    override fun observe(bookId: String, chapter: Int): Flow<ChapterCommentary?> = flow {
        ensureLoaded()
        emitAllValues(bookId, chapter)
    }

    override suspend fun get(bookId: String, chapter: Int): ChapterCommentary? {
        ensureLoaded()
        return cache.value[Key(bookId, chapter)]
    }

    private suspend fun ensureLoaded() {
        if (loaded) return
        mutex.withLock {
            if (loaded) return
            val file = source.loadAll()
            if (file != null) {
                val byKey = file.entries.associate { entry ->
                    Key(entry.bookId, entry.chapter) to ChapterCommentary(
                        bookId = entry.bookId,
                        chapter = entry.chapter,
                        summary = entry.summary,
                        body = entry.body,
                        qna = entry.qna.mapIndexed { idx, q ->
                            ChapterQna(ordinal = idx, question = q.q, answer = q.a)
                        },
                        model = file.model,
                        generatedAt = entry.generatedAt,
                    )
                }
                cache.value = byKey
            }
            loaded = true
        }
    }

    private suspend fun kotlinx.coroutines.flow.FlowCollector<ChapterCommentary?>.emitAllValues(
        bookId: String,
        chapter: Int,
    ) {
        val key = Key(bookId, chapter)
        cache.map { it[key] }.collect { emit(it) }
    }

    private data class Key(val bookId: String, val chapter: Int)
}
