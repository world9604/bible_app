package com.wordcard.app.data.source

import com.wordcard.app.data.model.CommentaryFileDto
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi
import wordcard.composeapp.generated.resources.Res

interface CommentaryDataSource {
    suspend fun loadAll(): CommentaryFileDto?
}

class BundledCommentaryDataSource(
    private val json: Json = DefaultJson,
) : CommentaryDataSource {

    @OptIn(ExperimentalResourceApi::class)
    override suspend fun loadAll(): CommentaryFileDto? = runCatching {
        val bytes = Res.readBytes(RESOURCE_PATH)
        val text = bytes.decodeToString()
        json.decodeFromString(CommentaryFileDto.serializer(), text)
    }.getOrNull()

    companion object {
        private const val RESOURCE_PATH = "files/commentaries.json"
        private val DefaultJson = Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }
    }
}
