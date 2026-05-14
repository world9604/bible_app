package com.wordcard.app.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.wordcard.app.database.BibleDatabase
import com.wordcard.app.domain.model.ReadingPosition
import com.wordcard.app.domain.repository.ReadingPositionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SqlDelightReadingPositionRepository(
    private val db: BibleDatabase,
) : ReadingPositionRepository {

    private val queries get() = db.bibleDatabaseQueries

    override fun observe(): Flow<ReadingPosition> =
        queries.selectReadingPosition()
            .asFlow()
            .mapToOneOrNull(Dispatchers.Default)
            .map { row ->
                row?.let {
                    ReadingPosition(
                        bookId = it.book_id,
                        chapter = it.chapter.toInt(),
                        verse = it.verse.toInt(),
                    )
                } ?: ReadingPosition.Default
            }

    override suspend fun save(position: ReadingPosition) = withContext(Dispatchers.Default) {
        queries.upsertReadingPosition(
            book_id = position.bookId,
            chapter = position.chapter.toLong(),
            verse = position.verse.toLong(),
        )
    }
}
