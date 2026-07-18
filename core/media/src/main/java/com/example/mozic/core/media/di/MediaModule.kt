package com.example.mozic.core.media.di

import android.content.Context
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import com.example.mozic.core.domain.player.PlayerController
import com.example.mozic.core.media.Media3PlayerController
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** LRU cap for [PlaybackService]'s on-disk stream cache (A4) — sensible per the plan's 200–500 MB range. */
private const val STREAM_CACHE_MAX_BYTES = 300L * 1024 * 1024

@Module
@InstallIn(SingletonComponent::class)
abstract class MediaModule {
    @Binds
    abstract fun bindPlayerController(impl: Media3PlayerController): PlayerController

    companion object {
        /**
         * MediaController calls must happen on the looper it was built with (the main looper by
         * default) — this scope stays on [Dispatchers.Main] so [Media3PlayerController] never has
         * to hop threads to talk to it. Repository suspend calls do their own dispatching.
         */
        @Provides
        @Singleton
        fun providePlayerControllerScope(): CoroutineScope =
            CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

        /**
         * Process-wide singleton, deliberately — two [SimpleCache] instances opened on the same
         * directory throw at runtime, the most common ExoPlayer-cache bug (A4). [PlaybackService]
         * wraps this in a `CacheDataSource.Factory` so every stream is written-through
         * transparently: replay/seek hits disk instead of re-downloading.
         */
        @Provides
        @Singleton
        fun provideStreamCache(@ApplicationContext context: Context): SimpleCache =
            SimpleCache(
                File(context.cacheDir, "media_stream_cache"),
                LeastRecentlyUsedCacheEvictor(STREAM_CACHE_MAX_BYTES),
                StandaloneDatabaseProvider(context),
            )
    }
}
