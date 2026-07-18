package com.example.mozic.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.NavHost
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
    }
}

/** `saveState`/`restoreState` so switching tabs doesn't reset each tab's scroll/search state. */
fun NavHostController.navigateToTopLevelDestination(destination: TopLevelDestination) {
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
