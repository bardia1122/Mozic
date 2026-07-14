package com.example.mozic.feature.profile

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.mozic.core.designsystem.R as DesignSystemR
import com.example.mozic.core.ui.component.PlaceholderScreen

@Composable
fun ProfileScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        title = stringResource(DesignSystemR.string.nav_profile),
        subtitle = stringResource(DesignSystemR.string.placeholder_coming_soon),
        modifier = modifier,
    )
}
