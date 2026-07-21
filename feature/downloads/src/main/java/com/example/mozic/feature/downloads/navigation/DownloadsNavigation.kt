package com.example.mozic.feature.downloads.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.mozic.feature.downloads.DownloadsScreen

fun NavGraphBuilder.downloadsScreen(onShareClick: (String) -> Unit) {
    composable<DownloadsRoute> {
        DownloadsScreen(onShareClick = onShareClick)
    }
}
