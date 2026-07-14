package com.example.mozic.feature.home.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.mozic.feature.home.HomeScreen

fun NavGraphBuilder.homeScreen(onNavigateToPlaylists: () -> Unit) {
    composable<HomeRoute> {
        HomeScreen(onNavigateToPlaylists = onNavigateToPlaylists)
    }
}
