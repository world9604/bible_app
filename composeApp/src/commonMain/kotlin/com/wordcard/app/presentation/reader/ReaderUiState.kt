package com.wordcard.app.presentation.reader

import com.wordcard.app.domain.model.Book
import com.wordcard.app.domain.model.Chapter
import com.wordcard.app.domain.model.Verse

data class ReaderUiState(
    val isLoading: Boolean = true,
    val books: List<Book> = emptyList(),
    val currentBook: Book? = null,
    val currentChapter: Chapter? = null,
    val selectedVerseNumbers: Set<Int> = emptySet(),
    val showBookPicker: Boolean = false,
    val showChapterPicker: Boolean = false,
    val showShareCard: Boolean = false,
) {
    val selectedVerses: List<Verse>
        get() = currentChapter?.verses?.filter { it.number in selectedVerseNumbers }.orEmpty()

    val hasSelection: Boolean get() = selectedVerseNumbers.isNotEmpty()
}
