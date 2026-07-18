package com.example.mozic.core.media

import androidx.media3.common.C
import androidx.media3.common.Player
import com.example.mozic.core.domain.model.PlayerState
import com.example.mozic.core.domain.model.Song

/**
 * Applies an ExoPlayer/[Player]'s transient playback facts onto an existing [PlayerState]
 * snapshot. [Media3PlayerController] owns the [Song] behind each media ID (from what it last
 * queued), so this only reads Player-native state — it never reconstructs a [Song] from
 * [androidx.media3.common.MediaMetadata].
 */
internal object PlayerStateMapper {
    fun apply(
        player: Player,
        current: PlayerState,
        songForMediaId: (String) -> Song?,
    ): PlayerState {
        val mediaId = player.currentMediaItem?.mediaId
        return current.copy(
            currentSong = mediaId?.let(songForMediaId) ?: current.currentSong,
            isPlaying = player.isPlaying,
            positionMs = player.currentPosition.coerceAtLeast(0L),
            durationMs = player.duration.takeIf { it != C.TIME_UNSET } ?: current.durationMs,
            speed = player.playbackParameters.speed,
            queueIndex = player.currentMediaItemIndex.coerceAtLeast(0),
            isBuffering = player.playbackState == Player.STATE_BUFFERING,
        )
    }
}
