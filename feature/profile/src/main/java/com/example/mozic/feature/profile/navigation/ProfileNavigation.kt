package com.example.mozic.feature.profile.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.mozic.feature.profile.ProfileScreen

fun NavGraphBuilder.profileScreen(onNavigateToSettings: () -> Unit) {
    composable<ProfileRoute> {
        ProfileScreen(onNavigateToSettings = onNavigateToSettings)
    }
}
