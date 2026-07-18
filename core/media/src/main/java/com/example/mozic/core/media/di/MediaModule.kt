package com.example.mozic.core.media.di

import com.example.mozic.core.domain.player.PlayerController
import com.example.mozic.core.media.Media3PlayerController
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

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
    }
}
