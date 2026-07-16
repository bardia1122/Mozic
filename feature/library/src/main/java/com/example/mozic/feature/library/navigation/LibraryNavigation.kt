package com.example.mozic.feature.library.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.mozic.feature.library.LibraryListScreen

/**
 * Same plain-fade duration/rationale as `PlaylistsNavigation`'s
 * `PLAYLISTS_NAV_TRANSITION_MS` — a slide/size transition into a
 * chrome-hiding sub-destination visibly re-anchors the caller's scroll
 * position mid-transition; a fade never touches layout.
 */
private const val LIBRARY_NAV_TRANSITION_MS = 220

fun NavGraphBuilder.libraryScreen(navController: NavHostController) {
    composable<LibraryListRoute>(
        enterTransition = { fadeIn(animationSpec = tween(LIBRARY_NAV_TRANSITION_MS)) },
        popExitTransition = { fadeOut(animationSpec = tween(LIBRARY_NAV_TRANSITION_MS)) },
    ) {
        LibraryListScreen(onBackClick = { navController.popBackStack() })
    }
}

fun NavHostController.navigateToLibraryList(kind: LibraryListKind) {
    navigate(LibraryListRoute(kind))
}
