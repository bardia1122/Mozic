package com.example.mozic.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Schema for B6's [com.example.mozic.core.domain.repository.DownloadRepository]
 * impl (WorkManager-backed). Added now so the DB doesn't need another version
 * bump when B6 lands; unused until then. [state] mirrors
 * [com.example.mozic.core.domain.model.DownloadState]'s variant name
 * (`NOT_DOWNLOADED` / `QUEUED` / `DOWNLOADING` / `DOWNLOADED` / `FAILED`).
 */
@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val songId: String,
    val filePath: String?,
    val state: String,
    val progress: Float,
    val failureReason: String? = null,
)
