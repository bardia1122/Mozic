package com.example.mozic.core.data.fake

import com.example.mozic.core.domain.model.PlayerState
import com.example.mozic.core.domain.player.PlayerController
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.time.Duration

/**
 * In-memory PlayerController for building screens before the real Media3 engine
 * lands. It updates [PlayerState] synchronously but does not actually play audio
 * or tick position — enough to wire up player/mini-player UI and "Play All".
 */
@Singleton
class FakePlayerController @Inject constructor() : PlayerController {
    private val internalState = MutableStateFlow(PlayerState())
    override val state: StateFlow<PlayerState> = internalState.asStateFlow()

    override fun play(songId: String) {
        val song = SampleData.songs.find { it.id == songId } ?: return
        internalState.update {
            it.copy(
                currentSong = song,
                queue = listOf(song),
                queueIndex = 0,
                isPlaying = true,
                positionMs = 0L,
                durationMs = song.durationMs ?: 0L,
            )
        }
    }

    override fun playQueue(songIds: List<String>, startIndex: Int, shuffle: Boolean) {
        val resolved = songIds.mapNotNull { id -> SampleData.songs.find { it.id == id } }
        val queue = if (shuffle) resolved.shuffled() else resolved
        if (queue.isEmpty()) return
        val index = startIndex.coerceIn(0, queue.lastIndex)
        internalState.update {
            it.copy(
                queue = queue,
                queueIndex = index,
                currentSong = queue[index],
                isPlaying = true,
                positionMs = 0L,
                durationMs = queue[index].durationMs ?: 0L,
            )
        }
    }

    override fun pause() = internalState.update { it.copy(isPlaying = false) }

    override fun resume() = internalState.update { it.copy(isPlaying = true) }

    override fun togglePlayPause() =
        internalState.update { it.copy(isPlaying = !it.isPlaying) }

    override fun next() = moveBy(1)

    override fun previous() = moveBy(-1)

    override fun seekTo(positionMs: Long) =
        internalState.update { it.copy(positionMs = positionMs) }

    override fun setSpeed(speed: Float) = internalState.update { it.copy(speed = speed) }

    override fun setSleepTimer(duration: Duration?) =
        internalState.update { it.copy(sleepTimerRemainingMs = duration?.inWholeMilliseconds) }

    private fun moveBy(delta: Int) {
        internalState.update { current ->
            if (current.queue.isEmpty()) return@update current
            val newIndex = (current.queueIndex + delta).coerceIn(0, current.queue.lastIndex)
            current.copy(
                queueIndex = newIndex,
                currentSong = current.queue[newIndex],
                positionMs = 0L,
                durationMs = current.queue[newIndex].durationMs ?: 0L,
            )
        }
    }
}
