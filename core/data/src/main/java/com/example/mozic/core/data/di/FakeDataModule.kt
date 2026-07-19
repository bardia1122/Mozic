package com.example.mozic.core.data.di

import com.example.mozic.core.data.fake.FakeChatRepository
import com.example.mozic.core.data.fake.FakeSocialRepository
import com.example.mozic.core.domain.repository.ChatRepository
import com.example.mozic.core.domain.repository.SocialRepository
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
 *
 * `PlayerController` was the last one still faked here; A1 replaced it with
 * `Media3PlayerController`, bound in `:core:media`'s `MediaModule` instead.
 * `SongRepository`/`PlaylistRepository` moved to `DataModule` in C2 (real
 * catalog) — `FakeSongRepository`/`FakePlaylistRepository` still exist,
 * unbound, for tests. `ChatRepository`/`SocialRepository` stay faked until
 * C4-C6.
 */
@Module
@InstallIn(SingletonComponent::class)
interface FakeDataModule {
    @Binds
    fun bindChatRepository(impl: FakeChatRepository): ChatRepository

    @Binds
    fun bindSocialRepository(impl: FakeSocialRepository): SocialRepository
}
