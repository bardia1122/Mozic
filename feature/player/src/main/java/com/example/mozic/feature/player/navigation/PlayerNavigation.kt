package com.example.mozic.feature.player.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.mozic.feature.player.AddToPlaylistSheet
import com.example.mozic.feature.player.NowPlayingScreen

/**
 * Same plain-fade rationale as `PlaylistsNavigation`/`LibraryNavigation`'s
 * transitions: a slide/size-based transition into a chrome-hiding
 * sub-destination visibly re-anchors the caller's layout mid-transition; a
 * fade never touches layout.
 */
private const val NOW_PLAYING_NAV_TRANSITION_MS = 220

fun NavGraphBuilder.nowPlayingScreen(
    navController: NavHostController,
    onShareClick: (songId: String) -> Unit,
    onSongAddedToPlaylist: (playlistTitle: String) -> Unit,
) {
    composable<NowPlayingRoute>(
        enterTransition = { fadeIn(animationSpec = tween(NOW_PLAYING_NAV_TRANSITION_MS)) },
        exitTransition = { fadeOut(animationSpec = tween(NOW_PLAYING_NAV_TRANSITION_MS)) },
        popEnterTransition = { fadeIn(animationSpec = tween(NOW_PLAYING_NAV_TRANSITION_MS)) },
        popExitTransition = { fadeOut(animationSpec = tween(NOW_PLAYING_NAV_TRANSITION_MS)) },
    ) {
        NowPlayingScreen(
            onBackClick = { navController.popBackStack() },
            onShareClick = onShareClick,
            onAddToPlaylistClick = navController::navigateToAddToPlaylist,
            animatedVisibilityScope = this,
        )
    }
    composable<AddToPlaylistRoute>(
        enterTransition = { fadeIn(animationSpec = tween(NOW_PLAYING_NAV_TRANSITION_MS)) },
        exitTransition = { fadeOut(animationSpec = tween(NOW_PLAYING_NAV_TRANSITION_MS)) },
        popEnterTransition = { fadeIn(animationSpec = tween(NOW_PLAYING_NAV_TRANSITION_MS)) },
        popExitTransition = { fadeOut(animationSpec = tween(NOW_PLAYING_NAV_TRANSITION_MS)) },
    ) {
        AddToPlaylistSheet(
            onDismiss = { navController.popBackStack() },
            onSongAdded = { playlistTitle ->
                onSongAddedToPlaylist(playlistTitle)
                navController.popBackStack()
            },
        )
    }
}

fun NavHostController.navigateToNowPlaying() {
    navigate(NowPlayingRoute)
}

fun NavHostController.navigateToAddToPlaylist(songId: String) {
    navigate(AddToPlaylistRoute(songId))
}
