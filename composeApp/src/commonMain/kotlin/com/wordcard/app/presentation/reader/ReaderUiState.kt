package com.wordcard.app.presentation.reader

import com.wordcard.app.domain.model.Book
import com.wordcard.app.domain.model.Chapter
import com.wordcard.app.domain.model.Verse
import com.wordcard.app.domain.model.VerseAnnotation

data class ReaderUiState(
    val isLoading: Boolean = true,
    val books: List<Book> = emptyList(),
    val currentBook: Book? = null,
    val currentChapter: Chapter? = null,
    val selectedVerseNumbers: Set<Int> = emptySet(),
    val annotations: Map<Int, VerseAnnotation> = emptyMap(),
    val showBookPicker: Boolean = false,
    val showChapterPicker: Boolean = false,
    val showShareCard: Boolean = false,
    val showHighlightPicker: Boolean = false,
    val memoEditingVerse: Int? = null,
    val memoDraft: String = "",
) {
    val selectedVerses: List<Verse>
        get() = currentChapter?.verses?.filter { it.number in selectedVerseNumbers }.orEmpty()

    val hasSelection: Boolean get() = selectedVerseNumbers.isNotEmpty()

    val showMemoEditor: Boolean get() = memoEditingVerse != null

    /** True when every selected verse already has a bookmark — used to toggle the action. */
    val allSelectedBookmarked: Boolean
        get() = hasSelection && selectedVerseNumbers.all { annotations[it]?.bookmarked == true }
}
