package com.wordcard.app.domain.model

enum class HighlightColor { Yellow, Green, Blue, Pink, Lavender }

data class VerseAnnotation(
    val bookId: String,
    val chapter: Int,
    val verseNumber: Int,
    val bookmarked: Boolean = false,
    val highlight: HighlightColor? = null,
    val memo: String? = null,
) {
    val isEmpty: Boolean
        get() = !bookmarked && highlight == null && memo.isNullOrBlank()
}
