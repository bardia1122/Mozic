package com.example.mozic.core.data.di

import android.content.Context
import androidx.room.Room
import com.example.mozic.core.data.local.MozicDatabase
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
        Room.databaseBuilder(context, MozicDatabase::class.java, DATABASE_NAME).build()

    @Provides
    fun provideSearchHistoryDao(database: MozicDatabase): SearchHistoryDao = database.searchHistoryDao()
}
