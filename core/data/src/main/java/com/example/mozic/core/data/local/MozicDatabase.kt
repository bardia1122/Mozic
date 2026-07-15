package com.example.mozic.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.mozic.core.data.local.dao.SearchHistoryDao
import com.example.mozic.core.data.local.entity.SearchHistoryEntity

/**
 * Grows one entity/version at a time as each local-data PR lands (search
 * history now in B3; liked/recents/downloads join in B5).
 */
@Database(entities = [SearchHistoryEntity::class], version = 1, exportSchema = true)
abstract class MozicDatabase : RoomDatabase() {
    abstract fun searchHistoryDao(): SearchHistoryDao
}
