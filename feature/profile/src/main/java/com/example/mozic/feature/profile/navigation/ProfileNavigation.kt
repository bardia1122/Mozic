package com.example.mozic.feature.profile.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.mozic.feature.profile.ProfileScreen

/**
 * Same plain-fade rationale as `PlaylistsNavigation`'s equivalent constant.
 * Defined on all four edges (unlike `LibraryNavigation`'s enter+popExit-only
 * pairing, which leans on `HomeRoute`'s own exit+popEnter since Library is
 * only ever reached from Home) because Profile is reachable from the
 * persistent top bar's avatar from *any* tab, most of which don't define a
 * matching exitTransition of their own — so this has to be self-contained to
 * fade reliably regardless of where it was entered from.
 */
private const val PROFILE_NAV_TRANSITION_MS = 220

fun NavGraphBuilder.profileScreen(onNavigateToSettings: () -> Unit) {
    composable<ProfileRoute>(
        enterTransition = { fadeIn(animationSpec = tween(PROFILE_NAV_TRANSITION_MS)) },
        exitTransition = { fadeOut(animationSpec = tween(PROFILE_NAV_TRANSITION_MS)) },
        popEnterTransition = { fadeIn(animationSpec = tween(PROFILE_NAV_TRANSITION_MS)) },
        popExitTransition = { fadeOut(animationSpec = tween(PROFILE_NAV_TRANSITION_MS)) },
    ) {
        ProfileScreen(onNavigateToSettings = onNavigateToSettings)
    }
}
