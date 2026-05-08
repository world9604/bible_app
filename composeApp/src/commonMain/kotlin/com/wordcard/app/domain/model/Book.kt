package com.wordcard.app.domain.model

enum class Testament { OLD, NEW }

data class Book(
    val id: String,
    val name: String,
    val abbreviation: String,
    val testament: Testament,
    val chapterCount: Int,
)
