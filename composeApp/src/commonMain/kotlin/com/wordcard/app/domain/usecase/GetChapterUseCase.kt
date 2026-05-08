package com.wordcard.app.domain.usecase

import com.wordcard.app.domain.model.Chapter
import com.wordcard.app.domain.repository.BibleRepository

class GetChapterUseCase(private val repository: BibleRepository) {
    suspend operator fun invoke(bookId: String, chapter: Int): Chapter? =
        repository.chapter(bookId, chapter)
}
