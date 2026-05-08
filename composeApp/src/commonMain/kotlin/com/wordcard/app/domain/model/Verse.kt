package com.wordcard.app.domain.model

data class Verse(
    val bookId: String,
    val chapter: Int,
    val number: Int,
    val text: String,
) {
    val reference: String get() = "$bookId $chapter:$number"
}
