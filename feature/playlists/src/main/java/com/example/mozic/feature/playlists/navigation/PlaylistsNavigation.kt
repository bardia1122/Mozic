package com.example.mozic.feature.playlists.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.mozic.feature.playlists.PlaylistDetailScreen
import com.example.mozic.feature.playlists.PlaylistsScreen

/**
 * The grid and its detail screen are both sub-destinations of this one tab,
 * so this extension owns navigating between them via [navController] —
 * distinct from the top-level tab switches [MozicNavHost] wires directly.
 */
fun NavGraphBuilder.playlistsScreen(navController: NavHostController) {
    composable<PlaylistsRoute> {
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
    composable<PlaylistDetailRoute> {
        PlaylistDetailScreen(onBackClick = { navController.popBackStack() })
    }
}
