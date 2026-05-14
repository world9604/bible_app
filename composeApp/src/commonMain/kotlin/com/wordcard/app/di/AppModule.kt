package com.wordcard.app.di

import com.wordcard.app.data.local.DatabaseDriverFactory
import com.wordcard.app.data.repository.BibleRepositoryImpl
import com.wordcard.app.data.repository.InMemoryReadingPositionRepository
import com.wordcard.app.data.repository.InMemoryVerseAnnotationRepository
import com.wordcard.app.data.repository.SqlDelightReadingPositionRepository
import com.wordcard.app.data.repository.SqlDelightVerseAnnotationRepository
import com.wordcard.app.data.source.BibleDataSource
import com.wordcard.app.data.source.KrvBibleDataSource
import com.wordcard.app.database.BibleDatabase
import com.wordcard.app.domain.repository.BibleRepository
import com.wordcard.app.domain.repository.ReadingPositionRepository
import com.wordcard.app.domain.repository.VerseAnnotationRepository
import com.wordcard.app.domain.usecase.GetBooksUseCase
import com.wordcard.app.domain.usecase.GetChapterUseCase
import com.wordcard.app.domain.usecase.ObserveReadingPositionUseCase
import com.wordcard.app.domain.usecase.SaveReadingPositionUseCase
import com.wordcard.app.presentation.reader.ReaderViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

/** Holds the [BibleDatabase] when local persistence is available, or null on platforms without it. */
private class DatabaseHolder(val database: BibleDatabase?)

private val dataModule: Module = module {
    single<BibleDataSource> { KrvBibleDataSource() }
    single<BibleRepository> { BibleRepositoryImpl(get()) }

    // Persistence wiring: build the driver once, share the database across repositories,
    // and fall back to in-memory implementations when the platform has no driver (wasmJs).
    single { DatabaseDriverFactory() }
    single {
        DatabaseHolder(get<DatabaseDriverFactory>().createOrNull()?.let { BibleDatabase(it) })
    }
    single<ReadingPositionRepository> {
        get<DatabaseHolder>().database
            ?.let { SqlDelightReadingPositionRepository(it) }
            ?: InMemoryReadingPositionRepository()
    }
    single<VerseAnnotationRepository> {
        get<DatabaseHolder>().database
            ?.let { SqlDelightVerseAnnotationRepository(it) }
            ?: InMemoryVerseAnnotationRepository()
    }
}

private val domainModule: Module = module {
    factory { GetBooksUseCase(get()) }
    factory { GetChapterUseCase(get()) }
    factory { ObserveReadingPositionUseCase(get()) }
    factory { SaveReadingPositionUseCase(get()) }
}

private val presentationModule: Module = module {
    viewModelOf(::ReaderViewModel)
}

val appModules: List<Module> = listOf(dataModule, domainModule, presentationModule)

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(appModules)
    }
}
