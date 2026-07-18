package com.example.mozic.core.domain.player

import com.example.mozic.core.domain.model.PlayerState
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration

/**
 * Person A's public playback API. Every track — playlist "Play All", the chat
 * song-share card, the player UI — goes through this interface, never ExoPlayer.
 */
interface PlayerController {
    val state: StateFlow<PlayerState>

    /** Resolve the song and replace the queue with just it. */
    fun play(songId: String)

    fun playQueue(songIds: List<String>, startIndex: Int = 0, shuffle: Boolean = false)

    fun pause()

    fun resume()

    fun togglePlayPause()

    /** Stops playback and clears the queue — the mini player disappears once [state] reflects this. */
    fun stop()

    fun next()

    fun previous()

    fun seekTo(positionMs: Long)

    fun setSpeed(speed: Float)

    /** `null` cancels any running timer. */
    fun setSleepTimer(duration: Duration?)
}
