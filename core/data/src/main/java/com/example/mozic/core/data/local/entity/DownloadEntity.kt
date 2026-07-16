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
 */
@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val songId: String,
    val filePath: String?,
    val state: String,
    val progress: Float,
    val failureReason: String? = null,
    val downloadedAtEpochMs: Long? = null,
)
