package com.wordcard.app.data.repository

import com.wordcard.app.domain.model.HighlightColor
import com.wordcard.app.domain.model.VerseAnnotation
import com.wordcard.app.domain.repository.VerseAnnotationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class InMemoryVerseAnnotationRepository : VerseAnnotationRepository {
    private data class Key(val bookId: String, val chapter: Int, val verse: Int)

    private val state = MutableStateFlow<Map<Key, VerseAnnotation>>(emptyMap())

    override fun observe(bookId: String, chapter: Int): Flow<Map<Int, VerseAnnotation>> =
        state.map { all ->
            all.entries
                .filter { it.key.bookId == bookId && it.key.chapter == chapter }
                .associate { it.key.verse to it.value }
        }

    override suspend fun setBookmark(bookId: String, chapter: Int, verses: Set<Int>, bookmarked: Boolean) {
        mutate { current ->
            verses.fold(current) { acc, v ->
                val key = Key(bookId, chapter, v)
                val existing = acc[key] ?: VerseAnnotation(bookId, chapter, v)
                val updated = existing.copy(bookmarked = bookmarked)
                if (updated.isEmpty) acc - key else acc + (key to updated)
            }
        }
    }

    override suspend fun setHighlight(bookId: String, chapter: Int, verses: Set<Int>, color: HighlightColor?) {
        mutate { current ->
            verses.fold(current) { acc, v ->
                val key = Key(bookId, chapter, v)
                val existing = acc[key] ?: VerseAnnotation(bookId, chapter, v)
                val updated = existing.copy(highlight = color)
                if (updated.isEmpty) acc - key else acc + (key to updated)
            }
        }
    }

    override suspend fun setMemo(bookId: String, chapter: Int, verseNumber: Int, memo: String?) {
        mutate { current ->
            val key = Key(bookId, chapter, verseNumber)
            val existing = current[key] ?: VerseAnnotation(bookId, chapter, verseNumber)
            val normalized = memo?.takeIf { it.isNotBlank() }
            val updated = existing.copy(memo = normalized)
            if (updated.isEmpty) current - key else current + (key to updated)
        }
    }

    private fun mutate(block: (Map<Key, VerseAnnotation>) -> Map<Key, VerseAnnotation>) {
        state.value = block(state.value)
    }
}
