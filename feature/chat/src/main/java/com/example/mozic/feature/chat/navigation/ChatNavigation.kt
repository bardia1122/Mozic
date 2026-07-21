package com.example.mozic.feature.chat.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.mozic.feature.chat.ChatThreadScreen
import com.example.mozic.feature.chat.ConversationListScreen
import com.example.mozic.feature.chat.ShareSongSheet

/**
 * Same plain-fade rationale as `LibraryNavigation`'s equivalent constant — a
 * slide/size transition into a chrome-hiding sub-destination visibly
 * re-anchors the caller's scroll position mid-transition.
 */
private const val CHAT_NAV_TRANSITION_MS = 220

fun NavGraphBuilder.chatScreens(navController: NavHostController, onNavigateToNowPlaying: () -> Unit) {
    composable<ConversationListRoute>(
        enterTransition = { fadeIn(animationSpec = tween(CHAT_NAV_TRANSITION_MS)) },
        popExitTransition = { fadeOut(animationSpec = tween(CHAT_NAV_TRANSITION_MS)) },
    ) {
        ConversationListScreen(
            onBackClick = { navController.popBackStack() },
            onConversationClick = { conversationId -> navController.navigateToChatThread(conversationId) },
        )
    }
    composable<ChatThreadRoute>(
        enterTransition = { fadeIn(animationSpec = tween(CHAT_NAV_TRANSITION_MS)) },
        popExitTransition = { fadeOut(animationSpec = tween(CHAT_NAV_TRANSITION_MS)) },
    ) {
        ChatThreadScreen(
            onBackClick = { navController.popBackStack() },
            onNavigateToNowPlaying = onNavigateToNowPlaying,
        )
    }
    composable<ShareSongRoute>(
        enterTransition = { fadeIn(animationSpec = tween(CHAT_NAV_TRANSITION_MS)) },
        popExitTransition = { fadeOut(animationSpec = tween(CHAT_NAV_TRANSITION_MS)) },
    ) {
        ShareSongSheet(onDismiss = { navController.popBackStack() })
    }
}

fun NavHostController.navigateToConversationList() {
    navigate(ConversationListRoute)
}

fun NavHostController.navigateToChatThread(conversationId: String) {
    navigate(ChatThreadRoute(conversationId))
}

fun NavHostController.navigateToShareSong(songId: String) {
    navigate(ShareSongRoute(songId))
}
