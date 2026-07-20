package com.example.mozic.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.NavHost
import com.example.mozic.feature.chat.navigation.chatScreens
import com.example.mozic.feature.downloads.navigation.downloadsScreen
import com.example.mozic.feature.home.navigation.HomeRoute
import com.example.mozic.feature.home.navigation.homeScreen
import com.example.mozic.feature.library.navigation.LibraryListKind
import com.example.mozic.feature.library.navigation.libraryScreen
import com.example.mozic.feature.library.navigation.navigateToLibraryList
import com.example.mozic.feature.player.navigation.nowPlayingScreen
import com.example.mozic.feature.playlists.navigation.playlistsScreen
import com.example.mozic.feature.profile.navigation.profileScreen
import com.example.mozic.feature.search.navigation.searchScreen
import com.example.mozic.feature.settings.navigation.SettingsRoute
import com.example.mozic.feature.settings.navigation.settingsScreen

@Composable
fun MozicNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier,
    ) {
        homeScreen(
            onNavigateToPlaylists = {
                navController.navigateToTopLevelDestination(TopLevelDestination.PLAYLISTS)
            },
            onNavigateToLiked = { navController.navigateToLibraryList(LibraryListKind.LIKED) },
            onNavigateToRecentlyPlayed = {
                navController.navigateToLibraryList(LibraryListKind.RECENTLY_PLAYED)
            },
        )
        searchScreen()
        downloadsScreen()
        playlistsScreen(navController)
        libraryScreen(navController)
        nowPlayingScreen(navController)
        profileScreen(onNavigateToSettings = navController::navigateToSettings)
        settingsScreen(onBackClick = { navController.popBackStack() })
        chatScreens(navController)
    }
}

/**
 * If [destination] is already on the back stack — either because it's the
 * tab currently showing, or because a screen was pushed on top of it (e.g.
 * Liked Songs above Home) — collapse straight back to it via a real
 * `popBackStack`, so re-tapping a tab always lands on that tab's root screen
 * instead of leaving a drill-in screen on top. Only falls through to a full
 * cross-tab switch (`saveState`/`restoreState` so switching tabs doesn't
 * reset each tab's scroll/search state) when the target tab isn't on the
 * stack yet.
 */
fun NavHostController.navigateToTopLevelDestination(destination: TopLevelDestination) {
    val poppedToExistingTab = popBackStack(route = destination.route, inclusive = false)
    if (poppedToExistingTab) return

    val topLevelNavOptions = NavOptions.Builder()
        .setPopUpTo(graph.findStartDestination().id, inclusive = false, saveState = true)
        .setLaunchSingleTop(true)
        .setRestoreState(true)
        .build()
    navigate(destination.route, topLevelNavOptions)
}

fun NavHostController.navigateToSettings() {
    navigate(SettingsRoute)
}
