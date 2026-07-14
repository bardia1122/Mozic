package com.example.mozic.core.data.fake

import com.example.mozic.core.domain.model.DownloadState
import com.example.mozic.core.domain.model.Song
import com.example.mozic.core.domain.repository.DownloadRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * Person B's seam, faked. `enqueue` resolves straight to [DownloadState.Downloaded]
 * (no progress simulation) so downstream gates and the downloads tab have data.
 */
@Singleton
class FakeDownloadRepository @Inject constructor() : DownloadRepository {
    private val states = MutableStateFlow(
        mapOf<String, DownloadState>(
            SampleData.songs[0].id to DownloadState.Downloaded("/fake/${SampleData.songs[0].id}.mp3"),
        ),
    )

    override fun downloadState(songId: String): Flow<DownloadState> =
        states.map { it[songId] ?: DownloadState.NotDownloaded }

    override fun allDownloads(): Flow<List<Song>> = states.map { current ->
        SampleData.songs.filter { current[it.id] is DownloadState.Downloaded }
    }

    override suspend fun enqueue(songId: String) {
        states.update { it + (songId to DownloadState.Downloaded("/fake/$songId.mp3")) }
    }

    override suspend fun remove(songId: String) {
        states.update { it - songId }
    }
}
