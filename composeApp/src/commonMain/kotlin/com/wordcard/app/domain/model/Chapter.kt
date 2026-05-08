package com.wordcard.app.domain.model

data class Chapter(
    val bookId: String,
    val number: Int,
    val verses: List<Verse>,
)
