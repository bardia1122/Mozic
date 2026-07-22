package com.example.mozic.core.data.repository

import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.mozic.core.common.result.getOrNull
import com.example.mozic.core.data.local.dao.DownloadDao
import com.example.mozic.core.data.local.entity.DownloadEntity
import com.example.mozic.core.data.worker.DownloadWorker
import com.example.mozic.core.data.worker.KEY_AUDIO_URL
import com.example.mozic.core.data.worker.KEY_PROGRESS_PERCENT
import com.example.mozic.core.data.worker.KEY_SONG_ID
import com.example.mozic.core.data.worker.STATE_DOWNLOADED
import com.example.mozic.core.data.worker.STATE_DOWNLOADING
import com.example.mozic.core.data.worker.STATE_FAILED
import com.example.mozic.core.data.worker.STATE_QUEUED
import com.example.mozic.core.domain.model.DownloadState
import com.example.mozic.core.domain.model.Song
import com.example.mozic.core.domain.repository.DownloadRepository
import com.example.mozic.core.domain.repository.SongRepository
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

private const val PERCENT_SCALE = 100

fun downloadWorkName(songId: String): String = "download-$songId"

/**
 * Person B's seam, now real. [downloadState] merges the persisted Room row
 * with WorkManager's live [WorkInfo] for the same unique work name so
 * in-progress percent updates reach the UI without waiting for the worker's
 * own (throttled) Room writes. Person A's smart play (his A4, built against
 * the F3 fake) only ever reads [downloadState]/[Downloaded.localFilePath] —
 * this swap needed zero changes on his side, the whole point of the seam.
 */
@Singleton
class DownloadRepositoryImpl @Inject constructor(
    private val downloadDao: DownloadDao,
    private val workManager: WorkManager,
    private val songRepository: SongRepository,
) : DownloadRepository {

    override fun downloadState(songId: String): Flow<DownloadState> = combine(
        downloadDao.downloadState(songId),
        workManager.getWorkInfosForUniqueWorkFlow(downloadWorkName(songId)),
    ) { entity, workInfos ->
        val activeWork = workInfos.firstOrNull { !it.state.isFinished }
        when (activeWork?.state) {
            WorkInfo.State.RUNNING -> {
                val fallbackPercent = ((entity?.progress ?: 0f) * PERCENT_SCALE).toInt()
                val percent = activeWork.progress.getInt(KEY_PROGRESS_PERCENT, fallbackPercent)
                DownloadState.Downloading(percent / PERCENT_SCALE.toFloat())
            }
            WorkInfo.State.ENQUEUED, WorkInfo.State.BLOCKED -> DownloadState.Queued
            else -> entity.toDownloadState()
        }
    }

    override fun allDownloads(): Flow<List<Song>> = downloadDao.allDownloads().map { entities ->
        entities
            .filter { it.state == STATE_DOWNLOADED }
            .mapNotNull { entity -> songRepository.song(entity.songId).getOrNull() }
    }

    override suspend fun enqueue(songId: String) {
        val song = songRepository.song(songId).getOrNull() ?: return
        downloadDao.upsert(
            DownloadEntity(
                songId = songId,
                filePath = null,
                state = STATE_QUEUED,
                progress = 0f,
                title = song.title,
                artistName = song.artistName,
                coverImageUrl = song.coverImageUrl,
                audioUrl = song.audioUrl,
                durationMs = song.durationMs,
            ),
        )

        val request = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setInputData(workDataOf(KEY_SONG_ID to songId, KEY_AUDIO_URL to song.audioUrl))
            .build()
        // KEEP: a double-tap while a download is already queued/running must not double-download.
        workManager.enqueueUniqueWork(downloadWorkName(songId), ExistingWorkPolicy.KEEP, request)
    }

    override suspend fun remove(songId: String) {
        workManager.cancelUniqueWork(downloadWorkName(songId))
        downloadDao.get(songId)?.filePath?.let { path -> File(path).delete() }
        downloadDao.remove(songId)
    }
}

private fun DownloadEntity?.toDownloadState(): DownloadState = when (this?.state) {
    STATE_DOWNLOADED -> filePath?.let(DownloadState::Downloaded) ?: DownloadState.NotDownloaded
    STATE_DOWNLOADING -> DownloadState.Downloading(progress)
    STATE_QUEUED -> DownloadState.Queued
    STATE_FAILED -> DownloadState.Failed(failureReason)
    else -> DownloadState.NotDownloaded
}
