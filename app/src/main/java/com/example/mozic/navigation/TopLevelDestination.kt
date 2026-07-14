package com.example.mozic.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.mozic.core.designsystem.R as DesignSystemR
import com.example.mozic.feature.downloads.navigation.DownloadsRoute
import com.example.mozic.feature.home.navigation.HomeRoute
import com.example.mozic.feature.playlists.navigation.PlaylistsRoute
import com.example.mozic.feature.profile.navigation.ProfileRoute
import com.example.mozic.feature.search.navigation.SearchRoute
import kotlin.reflect.KClass

/** The five bottom-nav tabs (`saveState`/`restoreState` on switch — see [navigateToTopLevelDestination]). */
enum class TopLevelDestination(
    val route: Any,
    val routeClass: KClass<*>,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val labelRes: Int,
) {
    HOME(
        route = HomeRoute,
        routeClass = HomeRoute::class,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        labelRes = DesignSystemR.string.nav_home,
    ),
    SEARCH(
        route = SearchRoute,
        routeClass = SearchRoute::class,
        selectedIcon = Icons.Filled.Search,
        unselectedIcon = Icons.Outlined.Search,
        labelRes = DesignSystemR.string.nav_search,
    ),
    DOWNLOADS(
        route = DownloadsRoute,
        routeClass = DownloadsRoute::class,
        selectedIcon = Icons.Filled.Download,
        unselectedIcon = Icons.Outlined.Download,
        labelRes = DesignSystemR.string.nav_downloads,
    ),
    PLAYLISTS(
        route = PlaylistsRoute,
        routeClass = PlaylistsRoute::class,
        selectedIcon = Icons.Filled.LibraryMusic,
        unselectedIcon = Icons.Outlined.LibraryMusic,
        labelRes = DesignSystemR.string.nav_playlists,
    ),
    PROFILE(
        route = ProfileRoute,
        routeClass = ProfileRoute::class,
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person,
        labelRes = DesignSystemR.string.nav_profile,
    ),
}
