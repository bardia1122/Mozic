package com.example.mozic.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_plays")
data class RecentPlayEntity(
    @PrimaryKey val songId: String,
    val playedAtEpochMs: Long,
)
