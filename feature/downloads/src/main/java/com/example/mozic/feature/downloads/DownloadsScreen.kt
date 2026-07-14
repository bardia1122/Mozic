package com.example.mozic.feature.downloads

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.mozic.core.designsystem.R as DesignSystemR
import com.example.mozic.core.ui.component.PlaceholderScreen

@Composable
fun DownloadsScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        title = stringResource(DesignSystemR.string.nav_downloads),
        subtitle = stringResource(DesignSystemR.string.placeholder_coming_soon),
        modifier = modifier,
    )
}
