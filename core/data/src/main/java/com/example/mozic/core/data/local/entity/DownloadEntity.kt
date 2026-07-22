package com.example.mozic.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Backing store for B6's [com.example.mozic.core.domain.repository.DownloadRepository]
 * impl (WorkManager-backed). [state] mirrors
 * [com.example.mozic.core.domain.model.DownloadState]'s variant name
 * (`NOT_DOWNLOADED` / `QUEUED` / `DOWNLOADING` / `DOWNLOADED` / `FAILED`).
 * [downloadedAtEpochMs] is only set once [state] reaches `DOWNLOADED` — feeds
 * the Downloads tab's "sort by date" option.
 *
 * [title]/[artistName]/[coverImageUrl]/[audioUrl]/[durationMs] snapshot the
 * [com.example.mozic.core.domain.model.Song] at enqueue time so a downloaded
 * song can be displayed and played entirely offline — nothing here should
 * ever require a network round-trip through `SongRepository` to resolve.
 */
@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val songId: String,
    val filePath: String?,
    val state: String,
    val progress: Float,
    val title: String,
    val artistName: String,
    val coverImageUrl: String,
    val audioUrl: String,
    val durationMs: Long?,
    val failureReason: String? = null,
    val downloadedAtEpochMs: Long? = null,
)
