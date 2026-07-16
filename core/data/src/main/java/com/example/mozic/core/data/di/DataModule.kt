package com.example.mozic.core.data.di

import com.example.mozic.core.data.repository.LibraryRepositoryImpl
import com.example.mozic.core.data.repository.SearchRepositoryImpl
import com.example.mozic.core.domain.repository.LibraryRepository
import com.example.mozic.core.domain.repository.SearchRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Real (non-fake) repository bindings. Grows as `FakeDataModule`'s entries
 * get replaced one interface at a time — see that module for what's still
 * pending.
 */
@Module
@InstallIn(SingletonComponent::class)
interface DataModule {
    @Binds
    fun bindSearchRepository(impl: SearchRepositoryImpl): SearchRepository

    @Binds
    fun bindLibraryRepository(impl: LibraryRepositoryImpl): LibraryRepository
}
