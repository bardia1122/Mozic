package com.example.mozic.core.data.di

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
 * catalog); `ChatRepository`/`AuthRepository` moved there in C5 (real WS +
 * Supabase Auth); `SocialRepository` moved there in C6 (real follow graph) —
 * `FakeSongRepository`/`FakePlaylistRepository`/`FakeChatRepository`/
 * `FakeSocialRepository` all still exist, unbound, for tests. Every F3 fake is
 * now real; this module is left in place (empty) as the documented seam for
 * whatever's faked next, rather than deleted.
 */
@Module
@InstallIn(SingletonComponent::class)
interface FakeDataModule
