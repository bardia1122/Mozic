package com.example.mozic.feature.playlists

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.mozic.core.designsystem.R as DesignSystemR
import com.example.mozic.core.ui.component.PlaceholderScreen

@Composable
fun PlaylistsScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        title = stringResource(DesignSystemR.string.nav_playlists),
        subtitle = stringResource(DesignSystemR.string.placeholder_coming_soon),
        modifier = modifier,
    )
}
