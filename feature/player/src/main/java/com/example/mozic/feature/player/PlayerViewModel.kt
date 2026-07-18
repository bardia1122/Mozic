package com.example.mozic.feature.player

import androidx.lifecycle.ViewModel
import com.example.mozic.core.domain.model.PlayerState
import com.example.mozic.core.domain.player.PlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

/**
 * Thin by design: forwards [PlayerController.state] as-is and proxies every
 * user action straight to the controller — no business logic lives here.
 * Shared by both [MiniPlayerBar] and `NowPlayingScreen`; each composable gets
 * its own instance via `hiltViewModel()` (different `ViewModelStoreOwner`s),
 * both observing the same singleton [PlayerController].
 */
@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerController: PlayerController,
) : ViewModel() {

    val state: StateFlow<PlayerState> = playerController.state

    fun togglePlayPause() = playerController.togglePlayPause()

    fun next() = playerController.next()

    fun previous() = playerController.previous()

    fun seekTo(positionMs: Long) = playerController.seekTo(positionMs)
}
