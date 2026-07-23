package com.example.mozic.core.domain.model

/** Immutable snapshot of playback; exposed as one StateFlow by PlayerController. */
data class PlayerState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val speed: Float = 1f,
    val queue: List<Song> = emptyList(),
    val queueIndex: Int = 0,
    val sleepTimerRemainingMs: Long? = null,
    val isBuffering: Boolean = false,
    val shuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
)
