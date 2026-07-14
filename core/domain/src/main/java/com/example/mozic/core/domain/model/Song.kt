package com.example.mozic.core.domain.model

data class Song(
    val id: String,
    val title: String,
    val artistName: String,
    val coverImageUrl: String,
    val audioUrl: String,
    val durationMs: Long?,
    val isLiked: Boolean = false,
)
