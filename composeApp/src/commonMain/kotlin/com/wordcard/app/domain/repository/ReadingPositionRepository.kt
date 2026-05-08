package com.wordcard.app.domain.repository

import com.wordcard.app.domain.model.ReadingPosition
import kotlinx.coroutines.flow.Flow

interface ReadingPositionRepository {
    fun observe(): Flow<ReadingPosition>
    suspend fun save(position: ReadingPosition)
}
