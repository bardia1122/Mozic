package com.example.mozic.core.data.worker

import android.content.Context
import android.os.Environment
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.mozic.core.data.local.dao.DownloadDao
import com.example.mozic.core.data.local.entity.DownloadEntity
import com.example.mozic.core.domain.model.DownloadState
import java.io.File
import java.io.IOException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody

const val KEY_SONG_ID = "song_id"
const val KEY_AUDIO_URL = "audio_url"
const val KEY_PROGRESS_PERCENT = "progress_percent"

private const val MAX_RETRIES = 3
private const val PERCENT_SCALE = 100
private const val PROGRESS_REPORT_STEP_PERCENT = 5
private const val BUFFER_SIZE_BYTES = 8 * 1024

/**
 * Streams [KEY_AUDIO_URL] to app-specific storage (no storage permission
 * needed, the entire reason to use [Environment.DIRECTORY_MUSIC] under
 * [Context.getExternalFilesDir]) via a plain blocking OkHttp call, wrapped in
 * [ioDispatcher] per the project's thread-safety rule (no inline
 * `Dispatchers.IO`).
 *
 * Deliberately skips `setForeground`/a foreground-service notification: doing
 * that correctly on API 34+ needs a declared `foregroundServiceType` in both
 * the manifest and `ForegroundInfo`, plus a `POST_NOTIFICATIONS`
 * runtime-permission flow — real scope, and unverifiable without an
 * on-device/emulator session (none available this session, see
 * `doc/PROGRESS.md`). `setProgress` alone is enough for the Downloads/library
 * UI to show live progress; the OS killing a very long-running download
 * under Doze is an accepted risk for sample-sized audio files.
 *
 * Deliberately a plain constructor, not `@HiltWorker`/`@AssistedInject`:
 * androidx.hilt.work's generated `WorkerAssistedFactory` codegen fails under
 * this project's Kotlin 2.2.10 / KSP2 (AA-mode) toolchain with "assisted
 * factory's abstract method must return a type with an
 * @AssistedInject-annotated constructor" — a real generic-type-substitution
 * bug in that codegen path, not a mistake here. [DownloadWorkerFactory]
 * constructs this worker by hand instead, which needs no Hilt-Work codegen
 * at all and is the standard fallback for exactly this situation.
 */
class DownloadWorker(
    context: Context,
    params: WorkerParameters,
    private val downloadDao: DownloadDao,
    private val okHttpClient: OkHttpClient,
    private val ioDispatcher: CoroutineDispatcher,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val songId = inputData.getString(KEY_SONG_ID) ?: return Result.failure()
        val audioUrl = inputData.getString(KEY_AUDIO_URL) ?: return Result.failure()

        downloadDao.upsert(
            DownloadEntity(songId = songId, filePath = null, state = STATE_DOWNLOADING, progress = 0f),
        )

        return try {
            val filePath = withContext(ioDispatcher) { stream(songId, audioUrl) }
            downloadDao.upsert(
                DownloadEntity(
                    songId = songId,
                    filePath = filePath,
                    state = STATE_DOWNLOADED,
                    progress = 1f,
                    downloadedAtEpochMs = System.currentTimeMillis(),
                ),
            )
            Result.success()
        } catch (e: IOException) {
            onFailure(songId, e)
        }
    }

    private suspend fun onFailure(songId: String, cause: IOException): Result {
        if (runAttemptCount < MAX_RETRIES) return Result.retry()
        downloadDao.upsert(
            DownloadEntity(
                songId = songId,
                filePath = null,
                state = STATE_FAILED,
                progress = 0f,
                failureReason = cause.message,
            ),
        )
        return Result.failure()
    }

    /**
     * Streams to a `.part` file, renamed atomically on success so a killed
     * worker never leaves a half-written file mistaken for a real download.
     */
    private suspend fun stream(songId: String, audioUrl: String): String {
        val targetDir = applicationContext.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
            ?: error("No external files dir available")
        val targetFile = File(targetDir, "$songId.mp3")
        val partFile = File(targetDir, "$songId.mp3.part")

        val request = Request.Builder().url(audioUrl).build()
        okHttpClient.newCall(request).execute().use { response ->
            val body = requireSuccessfulBody(songId, response)
            copyToFile(songId, body, partFile)
        }

        if (!partFile.renameTo(targetFile)) throw IOException("Could not finalize download for $songId")
        return targetFile.absolutePath
    }

    private fun requireSuccessfulBody(songId: String, response: Response): ResponseBody {
        if (!response.isSuccessful) throw IOException("HTTP ${response.code} downloading $songId")
        return response.body ?: throw IOException("Empty response body for $songId")
    }

    private suspend fun copyToFile(songId: String, body: ResponseBody, partFile: File) {
        val totalBytes = body.contentLength()
        var readBytes = 0L
        var lastReportedPercent = -1

        body.byteStream().use { input ->
            partFile.outputStream().use { output ->
                val buffer = ByteArray(BUFFER_SIZE_BYTES)
                var read = input.read(buffer)
                while (read != -1) {
                    output.write(buffer, 0, read)
                    readBytes += read
                    lastReportedPercent = reportProgressIfDue(songId, totalBytes, readBytes, lastReportedPercent)
                    read = input.read(buffer)
                }
            }
        }
    }

    /** Returns the percent that was just reported, or [lastReportedPercent] unchanged if it's not yet due. */
    private suspend fun reportProgressIfDue(
        songId: String,
        totalBytes: Long,
        readBytes: Long,
        lastReportedPercent: Int,
    ): Int {
        if (totalBytes <= 0) return lastReportedPercent
        val percent = (readBytes * PERCENT_SCALE / totalBytes).toInt()
        if (percent < lastReportedPercent + PROGRESS_REPORT_STEP_PERCENT) return lastReportedPercent

        setProgress(workDataOf(KEY_PROGRESS_PERCENT to percent))
        downloadDao.upsert(
            DownloadEntity(
                songId = songId,
                filePath = null,
                state = STATE_DOWNLOADING,
                progress = percent / PERCENT_SCALE.toFloat(),
            ),
        )
        return percent
    }
}

/** Mirrors [DownloadState]'s variant names — the string [DownloadEntity.state] stores. */
const val STATE_NOT_DOWNLOADED = "NOT_DOWNLOADED"
const val STATE_QUEUED = "QUEUED"
const val STATE_DOWNLOADING = "DOWNLOADING"
const val STATE_DOWNLOADED = "DOWNLOADED"
const val STATE_FAILED = "FAILED"
