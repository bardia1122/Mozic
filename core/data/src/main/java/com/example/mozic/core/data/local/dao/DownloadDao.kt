package com.example.mozic.core.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.mozic.core.data.local.entity.DownloadEntity
import kotlinx.coroutines.flow.Flow

/** Schema landed in B5; consumed by B6's WorkManager-backed `DownloadRepositoryImpl`. */
@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads WHERE songId = :songId")
    fun downloadState(songId: String): Flow<DownloadEntity?>

    @Query("SELECT * FROM downloads")
    fun allDownloads(): Flow<List<DownloadEntity>>

    @Upsert
    suspend fun upsert(entry: DownloadEntity)

    @Query("DELETE FROM downloads WHERE songId = :songId")
    suspend fun remove(songId: String)
}
