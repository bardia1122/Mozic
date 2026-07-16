package com.example.mozic.feature.playlists.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.mozic.feature.playlists.PlaylistDetailScreen
import com.example.mozic.feature.playlists.PlaylistsScreen

/**
 * Duration for the fade between the grid and its detail screen. `MozicApp`'s
 * chrome-visibility delay (`CHROME_VISIBILITY_DELAY_MS`) is set comfortably
 * longer than this so the bottom/top bar never resizes the content area while
 * either screen is still visibly mid-transition — keep the two in the same
 * ballpark if either changes.
 */
private const val PLAYLISTS_NAV_TRANSITION_MS = 220

/**
 * The grid and its detail screen are both sub-destinations of this one tab,
 * so this extension owns navigating between them via [navController] —
 * distinct from the top-level tab switches [MozicNavHost] wires directly.
 *
 * Transitions are plain fades (no slide/scale) instead of the library
 * default: a slide/size-based transition was visibly snapping the grid's
 * scroll position to the top for an instant mid-transition — the bottom bar
 * also disappears at that exact moment (`MozicApp`'s chrome hides for any
 * non-top-level destination), and the combined resize + slide made the grid
 * briefly relayout. A pure fade doesn't touch layout at all, so there's
 * nothing to relayout — combined with `MozicApp` delaying the chrome flip
 * past this duration, the content area's own size never changes mid-fade.
 */
fun NavGraphBuilder.playlistsScreen(navController: NavHostController) {
    composable<PlaylistsRoute>(
        exitTransition = { fadeOut(animationSpec = tween(PLAYLISTS_NAV_TRANSITION_MS)) },
        popEnterTransition = { fadeIn(animationSpec = tween(PLAYLISTS_NAV_TRANSITION_MS)) },
    ) {
        PlaylistsScreen(
            onNavigateToDetail = { playlist ->
                navController.navigate(
                    PlaylistDetailRoute(
                        playlistId = playlist.id,
                        title = playlist.title,
                        coverImageUrl = playlist.coverImageUrl,
                        songCount = playlist.songCount,
                    ),
                )
            },
        )
    }
    composable<PlaylistDetailRoute>(
        enterTransition = { fadeIn(animationSpec = tween(PLAYLISTS_NAV_TRANSITION_MS)) },
        popExitTransition = { fadeOut(animationSpec = tween(PLAYLISTS_NAV_TRANSITION_MS)) },
    ) {
        PlaylistDetailScreen(onBackClick = { navController.popBackStack() })
    }
}
