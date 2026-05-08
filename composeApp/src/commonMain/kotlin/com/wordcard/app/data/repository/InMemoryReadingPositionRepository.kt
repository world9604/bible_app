package com.wordcard.app.data.repository

import com.wordcard.app.domain.model.ReadingPosition
import com.wordcard.app.domain.repository.ReadingPositionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemoryReadingPositionRepository : ReadingPositionRepository {
    private val state = MutableStateFlow(ReadingPosition.Default)
    override fun observe(): Flow<ReadingPosition> = state.asStateFlow()
    override suspend fun save(position: ReadingPosition) {
        state.value = position
    }
}
