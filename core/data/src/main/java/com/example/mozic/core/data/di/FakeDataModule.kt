package com.example.mozic.core.data.di

import com.example.mozic.core.data.fake.FakeChatRepository
import com.example.mozic.core.data.fake.FakeDownloadRepository
import com.example.mozic.core.data.fake.FakeLibraryRepository
import com.example.mozic.core.data.fake.FakePlayerController
import com.example.mozic.core.data.fake.FakePlaylistRepository
import com.example.mozic.core.data.fake.FakeSocialRepository
import com.example.mozic.core.data.fake.FakeSongRepository
import com.example.mozic.core.data.fake.FakeUserPreferencesRepository
import com.example.mozic.core.domain.player.PlayerController
import com.example.mozic.core.domain.repository.ChatRepository
import com.example.mozic.core.domain.repository.DownloadRepository
import com.example.mozic.core.domain.repository.LibraryRepository
import com.example.mozic.core.domain.repository.PlaylistRepository
import com.example.mozic.core.domain.repository.SocialRepository
import com.example.mozic.core.domain.repository.SongRepository
import com.example.mozic.core.domain.repository.UserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Binds the F3 fakes to the frozen-contract interfaces so every screen can be
 * built and demoed today. Real implementations (Room/Ktor/Media3) replace these
 * bindings one interface at a time with zero UI changes — that swap is the
 * acceptance test of the architecture. The fakes are `@Singleton`, so their
 * in-memory state (likes, downloads, prefs, chat) is shared app-wide.
 */
@Module
@InstallIn(SingletonComponent::class)
interface FakeDataModule {
    @Binds
    fun bindSongRepository(impl: FakeSongRepository): SongRepository

    @Binds
    fun bindPlaylistRepository(impl: FakePlaylistRepository): PlaylistRepository

    @Binds
    fun bindLibraryRepository(impl: FakeLibraryRepository): LibraryRepository

    @Binds
    fun bindDownloadRepository(impl: FakeDownloadRepository): DownloadRepository

    @Binds
    fun bindUserPreferencesRepository(impl: FakeUserPreferencesRepository): UserPreferencesRepository

    @Binds
    fun bindChatRepository(impl: FakeChatRepository): ChatRepository

    @Binds
    fun bindSocialRepository(impl: FakeSocialRepository): SocialRepository

    @Binds
    fun bindPlayerController(impl: FakePlayerController): PlayerController
}
