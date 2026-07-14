package com.example.mozic.feature.playlists.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.mozic.feature.playlists.PlaylistsScreen

fun NavGraphBuilder.playlistsScreen() {
    composable<PlaylistsRoute> {
        PlaylistsScreen()
    }
}
