package com.wordcard.app.domain.usecase

import com.wordcard.app.domain.model.ChapterCommentary
import com.wordcard.app.domain.repository.ChapterCommentaryRepository
import kotlinx.coroutines.flow.Flow

class ObserveChapterCommentaryUseCase(
    private val repository: ChapterCommentaryRepository,
) {
    operator fun invoke(bookId: String, chapter: Int): Flow<ChapterCommentary?> =
        repository.observe(bookId, chapter)
}
