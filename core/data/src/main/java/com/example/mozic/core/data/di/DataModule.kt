package com.example.mozic.core.data.di

import com.example.mozic.core.data.repository.AuthRepositoryImpl
import com.example.mozic.core.data.repository.DownloadRepositoryImpl
import com.example.mozic.core.data.repository.LibraryRepositoryImpl
import com.example.mozic.core.data.repository.RealChatRepository
import com.example.mozic.core.data.repository.SearchRepositoryImpl
import com.example.mozic.core.data.repository.UserPreferencesRepositoryImpl
import com.example.mozic.core.domain.repository.AuthRepository
import com.example.mozic.core.domain.repository.ChatRepository
import com.example.mozic.core.domain.repository.DownloadRepository
import com.example.mozic.core.domain.repository.LibraryRepository
import com.example.mozic.core.domain.repository.PlaylistRepository
import com.example.mozic.core.domain.repository.SearchRepository
import com.example.mozic.core.domain.repository.SongRepository
import com.example.mozic.core.domain.repository.UserPreferencesRepository
import com.example.mozic.core.network.repository.NetworkPlaylistRepository
import com.example.mozic.core.network.repository.NetworkSongRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Real (non-fake) repository bindings. Grows as `FakeDataModule`'s entries
 * get replaced one interface at a time — see that module for what's still
 * pending. `SongRepository`/`PlaylistRepository` moved here in C2 (real
 * catalog, `:core:network`'s Supabase-backed impls); `FakeSongRepository`/
 * `FakePlaylistRepository` are left unbound but still exist for tests.
 * `ChatRepository`/`AuthRepository` moved here in C5 (real WS + Supabase
 * Auth) — `FakeChatRepository` is likewise left unbound, still used by tests.
 */
@Module
@InstallIn(SingletonComponent::class)
interface DataModule {
    @Binds
    fun bindSearchRepository(impl: SearchRepositoryImpl): SearchRepository

    @Binds
    fun bindLibraryRepository(impl: LibraryRepositoryImpl): LibraryRepository

    @Binds
    fun bindUserPreferencesRepository(impl: UserPreferencesRepositoryImpl): UserPreferencesRepository

    @Binds
    fun bindDownloadRepository(impl: DownloadRepositoryImpl): DownloadRepository

    @Binds
    fun bindSongRepository(impl: NetworkSongRepository): SongRepository

    @Binds
    fun bindPlaylistRepository(impl: NetworkPlaylistRepository): PlaylistRepository

    @Binds
    fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    fun bindChatRepository(impl: RealChatRepository): ChatRepository
}
