package com.wordcard.app.domain.model

data class ReadingPosition(
    val bookId: String,
    val chapter: Int,
    val verse: Int = 1,
) {
    companion object {
        val Default = ReadingPosition(bookId = "GEN", chapter = 1, verse = 1)
    }
}
