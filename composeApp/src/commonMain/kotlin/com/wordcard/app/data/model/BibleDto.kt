package com.wordcard.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class BibleBookDto(
    val id: String,
    val name: String,
    val abbr: String,
    val testament: String,
    val chapters: List<ChapterDto>,
)

@Serializable
data class ChapterDto(
    val n: Int,
    val verses: List<VerseDto>,
)

@Serializable
data class VerseDto(
    val v: Int,
    val t: String,
)
