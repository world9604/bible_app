package com.wordcard.app.di

import com.wordcard.app.data.repository.BibleRepositoryImpl
import com.wordcard.app.data.repository.InMemoryReadingPositionRepository
import com.wordcard.app.data.source.BibleDataSource
import com.wordcard.app.data.source.SampleBibleDataSource
import com.wordcard.app.domain.repository.BibleRepository
import com.wordcard.app.domain.repository.ReadingPositionRepository
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

private val dataModule: Module = module {
    single<BibleDataSource> { SampleBibleDataSource() }
    single<BibleRepository> { BibleRepositoryImpl(get()) }
    single<ReadingPositionRepository> { InMemoryReadingPositionRepository() }
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
