package com.example.mozic.feature.home.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.mozic.feature.home.HomeScreen

/**
 * Same plain-fade duration/rationale as `PlaylistsNavigation`'s
 * `PLAYLISTS_NAV_TRANSITION_MS`. Applied unconditionally to every transition
 * leaving/entering `HomeRoute` (both the Home->Library edge and ordinary
 * top-level tab switches) since Home can't reference `:feature:library`'s
 * route type to scope it more narrowly (features never depend on features)
 * — harmless for tab switches, which never hide chrome so have no
 * resize-driven reanchor risk to begin with.
 */
private const val HOME_NAV_TRANSITION_MS = 220

fun NavGraphBuilder.homeScreen(
    onNavigateToPlaylists: () -> Unit,
    onNavigateToLiked: () -> Unit,
    onNavigateToRecentlyPlayed: () -> Unit,
) {
    composable<HomeRoute>(
        exitTransition = { fadeOut(animationSpec = tween(HOME_NAV_TRANSITION_MS)) },
        popEnterTransition = { fadeIn(animationSpec = tween(HOME_NAV_TRANSITION_MS)) },
    ) {
        HomeScreen(
            onNavigateToPlaylists = onNavigateToPlaylists,
            onNavigateToLiked = onNavigateToLiked,
            onNavigateToRecentlyPlayed = onNavigateToRecentlyPlayed,
        )
    }
}
