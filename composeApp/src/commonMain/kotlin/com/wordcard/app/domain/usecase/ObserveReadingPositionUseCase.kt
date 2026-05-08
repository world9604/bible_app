package com.wordcard.app.domain.usecase

import com.wordcard.app.domain.model.ReadingPosition
import com.wordcard.app.domain.repository.ReadingPositionRepository
import kotlinx.coroutines.flow.Flow

class ObserveReadingPositionUseCase(private val repository: ReadingPositionRepository) {
    operator fun invoke(): Flow<ReadingPosition> = repository.observe()
}

class SaveReadingPositionUseCase(private val repository: ReadingPositionRepository) {
    suspend operator fun invoke(position: ReadingPosition) = repository.save(position)
}
