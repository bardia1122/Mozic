package com.example.mozic.feature.settings.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.mozic.feature.settings.SettingsScreen

fun NavGraphBuilder.settingsScreen(onBackClick: () -> Unit, onLoggedOut: () -> Unit) {
    composable<SettingsRoute> {
        SettingsScreen(onBackClick = onBackClick, onLoggedOut = onLoggedOut)
    }
}
