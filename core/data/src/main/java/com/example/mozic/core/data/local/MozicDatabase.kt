package com.example.mozic.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.mozic.core.data.local.dao.DownloadDao
import com.example.mozic.core.data.local.dao.LikedSongDao
import com.example.mozic.core.data.local.dao.RecentPlayDao
import com.example.mozic.core.data.local.dao.SearchHistoryDao
import com.example.mozic.core.data.local.entity.DownloadEntity
import com.example.mozic.core.data.local.entity.LikedSongEntity
import com.example.mozic.core.data.local.entity.RecentPlayEntity
import com.example.mozic.core.data.local.entity.SearchHistoryEntity

/**
 * Grows one entity/version at a time as each local-data PR lands (search
 * history from B3; liked/recents/downloads joined in B5; B6 added
 * `DownloadEntity.downloadedAtEpochMs` — no migration path yet, pre-release,
 * see `DatabaseModule`'s `fallbackToDestructiveMigration`).
 */
@Database(
    entities = [
        SearchHistoryEntity::class,
        LikedSongEntity::class,
        RecentPlayEntity::class,
        DownloadEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
abstract class MozicDatabase : RoomDatabase() {
    abstract fun searchHistoryDao(): SearchHistoryDao

    abstract fun likedSongDao(): LikedSongDao

    abstract fun recentPlayDao(): RecentPlayDao

    abstract fun downloadDao(): DownloadDao
}
