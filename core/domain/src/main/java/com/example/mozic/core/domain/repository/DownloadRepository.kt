package com.example.mozic.core.domain.repository

import com.example.mozic.core.domain.model.DownloadState
import com.example.mozic.core.domain.model.Song
import kotlinx.coroutines.flow.Flow

/** Person B's seam. A consumes [downloadState] for smart play; everyone reads it. */
interface DownloadRepository {
    fun downloadState(songId: String): Flow<DownloadState>

    fun allDownloads(): Flow<List<Song>>

    suspend fun enqueue(songId: String)

    suspend fun remove(songId: String)
}
