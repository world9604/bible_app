package com.wordcard.app.presentation.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wordcard.app.domain.model.HighlightColor
import com.wordcard.app.domain.model.ReadingPosition
import com.wordcard.app.domain.repository.VerseAnnotationRepository
import com.wordcard.app.domain.usecase.GetBooksUseCase
import com.wordcard.app.domain.usecase.GetChapterUseCase
import com.wordcard.app.domain.usecase.ObserveReadingPositionUseCase
import com.wordcard.app.domain.usecase.SaveReadingPositionUseCase
import kotlinx.coroutines.Job
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
    private val annotations: VerseAnnotationRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ReaderUiState())
    val state: StateFlow<ReaderUiState> = _state.asStateFlow()

    private var annotationJob: Job? = null

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
            if (book != null && chapter != null) {
                observeAnnotations(book.id, chapter.number)
            }
        }
    }

    private fun observeAnnotations(bookId: String, chapter: Int) {
        annotationJob?.cancel()
        annotationJob = viewModelScope.launch {
            annotations.observe(bookId, chapter).collect { map ->
                _state.update { it.copy(annotations = map) }
            }
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
                    annotations = emptyMap(),
                    showBookPicker = false,
                    showChapterPicker = false,
                )
            }
            observeAnnotations(book.id, 1)
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
                    annotations = emptyMap(),
                    showChapterPicker = false,
                )
            }
            observeAnnotations(book.id, chapterNumber)
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
                    annotations = emptyMap(),
                    showBookPicker = false,
                    showChapterPicker = false,
                )
            }
            observeAnnotations(book.id, chapterNumber)
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
        _state.update {
            it.copy(
                selectedVerseNumbers = emptySet(),
                showHighlightPicker = false,
            )
        }
    }

    fun openBookPicker() = _state.update { it.copy(showBookPicker = true) }
    fun closeBookPicker() = _state.update { it.copy(showBookPicker = false) }
    fun openChapterPicker() = _state.update { it.copy(showChapterPicker = true) }
    fun closeChapterPicker() = _state.update { it.copy(showChapterPicker = false) }
    fun openShareCard() = _state.update { it.copy(showShareCard = true) }
    fun closeShareCard() = _state.update { it.copy(showShareCard = false) }

    /** Toggle bookmark on every selected verse. If all are already bookmarked, remove from all. */
    fun toggleBookmarkOnSelection() {
        val s = _state.value
        val book = s.currentBook ?: return
        val chapter = s.currentChapter ?: return
        val verses = s.selectedVerseNumbers
        if (verses.isEmpty()) return
        val targetBookmarked = !s.allSelectedBookmarked
        viewModelScope.launch {
            annotations.setBookmark(book.id, chapter.number, verses, targetBookmarked)
        }
        clearSelection()
    }

    fun toggleHighlightPicker() {
        _state.update { it.copy(showHighlightPicker = !it.showHighlightPicker) }
    }

    /** Apply a highlight color to all selected verses (or clear when [color] is null). */
    fun applyHighlight(color: HighlightColor?) {
        val s = _state.value
        val book = s.currentBook ?: return
        val chapter = s.currentChapter ?: return
        val verses = s.selectedVerseNumbers
        if (verses.isEmpty()) return
        viewModelScope.launch {
            annotations.setHighlight(book.id, chapter.number, verses, color)
        }
        clearSelection()
    }

    /** Open memo editor for the currently selected single verse, or a specific verse. */
    fun openMemoEditor(verseNumber: Int? = null) {
        val s = _state.value
        val target = verseNumber ?: s.selectedVerseNumbers.singleOrNull() ?: return
        val existing = s.annotations[target]?.memo.orEmpty()
        _state.update {
            it.copy(
                memoEditingVerse = target,
                memoDraft = existing,
                showHighlightPicker = false,
            )
        }
    }

    fun updateMemoDraft(text: String) {
        _state.update { it.copy(memoDraft = text) }
    }

    fun saveMemo() {
        val s = _state.value
        val book = s.currentBook ?: return
        val chapter = s.currentChapter ?: return
        val verse = s.memoEditingVerse ?: return
        val text = s.memoDraft
        viewModelScope.launch {
            annotations.setMemo(book.id, chapter.number, verse, text)
        }
        _state.update { it.copy(memoEditingVerse = null, memoDraft = "") }
        clearSelection()
    }

    fun deleteMemo() {
        val s = _state.value
        val book = s.currentBook ?: return
        val chapter = s.currentChapter ?: return
        val verse = s.memoEditingVerse ?: return
        viewModelScope.launch {
            annotations.setMemo(book.id, chapter.number, verse, null)
        }
        _state.update { it.copy(memoEditingVerse = null, memoDraft = "") }
        clearSelection()
    }

    fun dismissMemoEditor() {
        _state.update { it.copy(memoEditingVerse = null, memoDraft = "") }
    }

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
