package com.wordcard.app.presentation.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wordcard.app.domain.model.ReadingPosition
import com.wordcard.app.domain.usecase.GetBooksUseCase
import com.wordcard.app.domain.usecase.GetChapterUseCase
import com.wordcard.app.domain.usecase.ObserveReadingPositionUseCase
import com.wordcard.app.domain.usecase.SaveReadingPositionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReaderViewModel(
    private val getBooks: GetBooksUseCase,
    private val getChapter: GetChapterUseCase,
    private val observePosition: ObserveReadingPositionUseCase,
    private val savePosition: SaveReadingPositionUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(ReaderUiState())
    val state: StateFlow<ReaderUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val books = getBooks()
            val pos = observePosition().first()
            val book = books.firstOrNull { it.id == pos.bookId } ?: books.firstOrNull()
            val chapter = book?.let { getChapter(it.id, pos.chapter) }
            _state.value = ReaderUiState(
                isLoading = false,
                books = books,
                currentBook = book,
                currentChapter = chapter,
            )
        }
    }

    fun selectBook(bookId: String) {
        viewModelScope.launch {
            val book = _state.value.books.firstOrNull { it.id == bookId } ?: return@launch
            val chapter = getChapter(book.id, 1)
            savePosition(ReadingPosition(bookId = book.id, chapter = 1))
            _state.update {
                it.copy(
                    currentBook = book,
                    currentChapter = chapter,
                    selectedVerseNumbers = emptySet(),
                    showBookPicker = false,
                    showChapterPicker = false,
                )
            }
        }
    }

    fun selectChapter(chapterNumber: Int) {
        val book = _state.value.currentBook ?: return
        viewModelScope.launch {
            val chapter = getChapter(book.id, chapterNumber)
            savePosition(ReadingPosition(book.id, chapterNumber))
            _state.update {
                it.copy(
                    currentChapter = chapter,
                    selectedVerseNumbers = emptySet(),
                    showChapterPicker = false,
                )
            }
        }
    }

    fun selectBookAndChapter(bookId: String, chapterNumber: Int) {
        viewModelScope.launch {
            val book = _state.value.books.firstOrNull { it.id == bookId } ?: return@launch
            val chapter = getChapter(book.id, chapterNumber)
            savePosition(ReadingPosition(book.id, chapterNumber))
            _state.update {
                it.copy(
                    currentBook = book,
                    currentChapter = chapter,
                    selectedVerseNumbers = emptySet(),
                    showBookPicker = false,
                    showChapterPicker = false,
                )
            }
        }
    }

    fun toggleVerse(verseNumber: Int) {
        _state.update { state ->
            val newSelection = if (verseNumber in state.selectedVerseNumbers) {
                state.selectedVerseNumbers - verseNumber
            } else {
                state.selectedVerseNumbers + verseNumber
            }
            state.copy(selectedVerseNumbers = newSelection)
        }
    }

    fun clearSelection() {
        _state.update { it.copy(selectedVerseNumbers = emptySet()) }
    }

    fun openBookPicker() = _state.update { it.copy(showBookPicker = true) }
    fun closeBookPicker() = _state.update { it.copy(showBookPicker = false) }
    fun openChapterPicker() = _state.update { it.copy(showChapterPicker = true) }
    fun closeChapterPicker() = _state.update { it.copy(showChapterPicker = false) }
    fun openShareCard() = _state.update { it.copy(showShareCard = true) }
    fun closeShareCard() = _state.update { it.copy(showShareCard = false) }

    fun nextChapter() {
        val state = _state.value
        val book = state.currentBook ?: return
        val chapterNum = state.currentChapter?.number ?: return
        if (chapterNum < book.chapterCount) selectChapter(chapterNum + 1)
    }

    fun previousChapter() {
        val state = _state.value
        val chapterNum = state.currentChapter?.number ?: return
        if (chapterNum > 1) selectChapter(chapterNum - 1)
    }
}
