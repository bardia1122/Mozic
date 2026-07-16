package com.example.mozic.core.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.OkHttpClient

/**
 * Plain OkHttp for [com.example.mozic.core.data.worker.DownloadWorker]'s file
 * stream — Ktor stays Person C's, this never touches a screen.
 */
@Module
@InstallIn(SingletonComponent::class)
object OkHttpModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient()
}
