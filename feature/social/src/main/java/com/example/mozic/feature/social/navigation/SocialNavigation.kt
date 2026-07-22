package com.example.mozic.feature.social.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.mozic.core.domain.model.Playlist
import com.example.mozic.feature.social.FollowingListScreen
import com.example.mozic.feature.social.UserProfileScreen
import com.example.mozic.feature.social.UserSearchScreen

/**
 * Same plain-fade rationale as `ChatNavigation`'s own constant — and, like
 * that file's routes, self-contained on all four edges rather than leaning on
 * the caller's own exitTransition, since `UserSearchRoute` is reachable from
 * the persistent top bar's "find people" icon from any tab.
 */
private const val SOCIAL_NAV_TRANSITION_MS = 220

/**
 * Wires the three C6 screens among themselves via [navController]. Playlist
 * detail is a `:feature:playlists` destination, and features never import
 * features (CLAUDE_PERSON_C.md §2) — so [onPlaylistClick] is threaded in from
 * `:app`'s own nav host, which is the one place allowed to link the two.
 */
fun NavGraphBuilder.socialScreens(
    navController: NavHostController,
    onPlaylistClick: (Playlist) -> Unit,
    onNavigateToChatThread: (String) -> Unit,
) {
    composable<UserSearchRoute>(
        enterTransition = { fadeIn(animationSpec = tween(SOCIAL_NAV_TRANSITION_MS)) },
        exitTransition = { fadeOut(animationSpec = tween(SOCIAL_NAV_TRANSITION_MS)) },
        popEnterTransition = { fadeIn(animationSpec = tween(SOCIAL_NAV_TRANSITION_MS)) },
        popExitTransition = { fadeOut(animationSpec = tween(SOCIAL_NAV_TRANSITION_MS)) },
    ) {
        UserSearchScreen(
            onBackClick = { navController.popBackStack() },
            onUserClick = { userId -> navController.navigate(UserProfileRoute(userId)) },
            onFollowingClick = { navController.navigate(FollowingListRoute) },
        )
    }
    composable<UserProfileRoute>(
        enterTransition = { fadeIn(animationSpec = tween(SOCIAL_NAV_TRANSITION_MS)) },
        exitTransition = { fadeOut(animationSpec = tween(SOCIAL_NAV_TRANSITION_MS)) },
        popEnterTransition = { fadeIn(animationSpec = tween(SOCIAL_NAV_TRANSITION_MS)) },
        popExitTransition = { fadeOut(animationSpec = tween(SOCIAL_NAV_TRANSITION_MS)) },
    ) {
        UserProfileScreen(
            onBackClick = { navController.popBackStack() },
            onPlaylistClick = onPlaylistClick,
            onNavigateToChatThread = onNavigateToChatThread,
        )
    }
    composable<FollowingListRoute>(
        enterTransition = { fadeIn(animationSpec = tween(SOCIAL_NAV_TRANSITION_MS)) },
        exitTransition = { fadeOut(animationSpec = tween(SOCIAL_NAV_TRANSITION_MS)) },
        popEnterTransition = { fadeIn(animationSpec = tween(SOCIAL_NAV_TRANSITION_MS)) },
        popExitTransition = { fadeOut(animationSpec = tween(SOCIAL_NAV_TRANSITION_MS)) },
    ) {
        FollowingListScreen(
            onBackClick = { navController.popBackStack() },
            onUserClick = { userId -> navController.navigate(UserProfileRoute(userId)) },
        )
    }
}

fun NavHostController.navigateToUserSearch() {
    navigate(UserSearchRoute)
}
