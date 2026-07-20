package com.example.mozic.core.data.di

import android.content.Context
import androidx.room.Room
import com.example.mozic.core.data.local.MozicDatabase
import com.example.mozic.core.data.local.dao.ConversationDao
import com.example.mozic.core.data.local.dao.DownloadDao
import com.example.mozic.core.data.local.dao.LikedSongDao
import com.example.mozic.core.data.local.dao.MessageDao
import com.example.mozic.core.data.local.dao.RecentPlayDao
import com.example.mozic.core.data.local.dao.SearchHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val DATABASE_NAME = "mozic.db"

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideMozicDatabase(@ApplicationContext context: Context): MozicDatabase =
        Room.databaseBuilder(context, MozicDatabase::class.java, DATABASE_NAME)
            // Pre-release, no installed base to preserve — no Migration objects
            // written yet, so a version bump without this would just crash on
            // open for anyone with an older local DB file.
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun provideSearchHistoryDao(database: MozicDatabase): SearchHistoryDao = database.searchHistoryDao()

    @Provides
    fun provideLikedSongDao(database: MozicDatabase): LikedSongDao = database.likedSongDao()

    @Provides
    fun provideRecentPlayDao(database: MozicDatabase): RecentPlayDao = database.recentPlayDao()

    @Provides
    fun provideDownloadDao(database: MozicDatabase): DownloadDao = database.downloadDao()

    @Provides
    fun provideMessageDao(database: MozicDatabase): MessageDao = database.messageDao()

    @Provides
    fun provideConversationDao(database: MozicDatabase): ConversationDao = database.conversationDao()
}
