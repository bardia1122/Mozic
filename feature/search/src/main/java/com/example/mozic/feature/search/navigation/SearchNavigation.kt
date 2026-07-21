package com.example.mozic.feature.search.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.mozic.feature.search.SearchScreen

fun NavGraphBuilder.searchScreen(onShareClick: (String) -> Unit) {
    composable<SearchRoute> {
        SearchScreen(onShareClick = onShareClick)
    }
}
