package com.wordcard.app.domain.usecase

import com.wordcard.app.domain.model.Book
import com.wordcard.app.domain.repository.BibleRepository

class GetBooksUseCase(private val repository: BibleRepository) {
    suspend operator fun invoke(): List<Book> = repository.books()
}
