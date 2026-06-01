package com.wordcard.app.domain.model

/**
 * AI-generated commentary for a single chapter, generated offline once with
 * claude-opus-4-7 and shipped with the app via a bundled JSON file.
 */
data class ChapterCommentary(
    val bookId: String,
    val chapter: Int,
    val summary: String,
    val body: String,
    val qna: List<ChapterQna>,
    val model: String,
    val generatedAt: String,
)

data class ChapterQna(
    val ordinal: Int,
    val question: String,
    val answer: String,
)
