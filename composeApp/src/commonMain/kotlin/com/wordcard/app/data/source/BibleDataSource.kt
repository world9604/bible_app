package com.wordcard.app.data.source

import com.wordcard.app.data.model.BibleBookDto

interface BibleDataSource {
    suspend fun loadAll(): List<BibleBookDto>
}
