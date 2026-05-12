package com.wordcard.app.data.source

import com.wordcard.app.data.model.BibleBookDto
import com.wordcard.app.data.model.BibleFileDto
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi
import wordcard.composeapp.generated.resources.Res

class KrvBibleDataSource(
    private val json: Json = DefaultJson,
) : BibleDataSource {

    @OptIn(ExperimentalResourceApi::class)
    override suspend fun loadAll(): List<BibleBookDto> {
        val bytes = Res.readBytes(RESOURCE_PATH)
        val text = bytes.decodeToString()
        return json.decodeFromString(BibleFileDto.serializer(), text).books
    }

    companion object {
        private const val RESOURCE_PATH = "files/krv.json"
        private val DefaultJson = Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }
    }
}
