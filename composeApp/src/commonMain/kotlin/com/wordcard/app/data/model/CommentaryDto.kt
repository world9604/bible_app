package com.wordcard.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Shape of the bundled `files/commentaries.json` file produced by the
 * offline generation script under `build-scripts/generate-commentaries/`.
 */
@Serializable
data class CommentaryFileDto(
    val version: String,
    val model: String,
    val entries: List<ChapterCommentaryDto>,
)

@Serializable
data class ChapterCommentaryDto(
    @SerialName("book") val bookId: String,
    val chapter: Int,
    val summary: String,
    val body: String,
    val qna: List<QnaDto>,
    @SerialName("generated_at") val generatedAt: String,
)

@Serializable
data class QnaDto(
    val q: String,
    val a: String,
)
