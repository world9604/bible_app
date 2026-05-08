package com.wordcard.app.domain.usecase

import com.wordcard.app.domain.model.Verse

data class ShareCardContent(
    val title: String,
    val body: String,
    val reference: String,
)

class BuildShareCardUseCase {
    operator fun invoke(verses: List<Verse>, bookName: String): ShareCardContent {
        require(verses.isNotEmpty()) { "verses must not be empty" }
        val sorted = verses.sortedBy { it.number }
        val chapter = sorted.first().chapter
        val first = sorted.first().number
        val last = sorted.last().number
        val rangeText = if (first == last) "$first" else "$first-$last"
        val reference = "$bookName $chapter:$rangeText"
        val body = sorted.joinToString(separator = " ") { verse ->
            "${verse.number} ${verse.text}"
        }.trim()
        return ShareCardContent(
            title = bookName,
            body = body,
            reference = reference,
        )
    }
}
