package com.example.mozic

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mozic.core.designsystem.R
import com.example.mozic.core.ui.animation.LocalSharedTransitionScope
import com.example.mozic.navigation.MozicNavHost
import com.example.mozic.navigation.TopLevelDestination
import com.example.mozic.navigation.navigateToSettings
import com.example.mozic.navigation.navigateToTopLevelDestination
import com.example.mozic.ui.MiniPlayerPlaceholder
import com.example.mozic.ui.MozicBottomBar
import com.example.mozic.ui.MozicTopBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Comfortably longer than the 220ms fade used by chrome-hiding sub-destination
 * transitions (e.g. `PlaylistDetailRoute` — see `PlaylistsNavigation.kt`'s
 * `PLAYLISTS_NAV_TRANSITION_MS`), so [showChrome] never flips until the
 * outgoing screen has actually finished transitioning off-screen — see the
 * comment above its declaration. Keep the two in the same ballpark if either
 * changes.
 */
private const val CHROME_VISIBILITY_DELAY_MS = 320L

/**
 * Root app composable. Layout order per spec: content -> mini-player slot ->
 * bottom bar. [miniPlayer] defaults to a placeholder until Person A supplies
 * `MiniPlayerBar` from `:feature:player`.
 */
@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MozicApp(
    modifier: Modifier = Modifier,
    miniPlayer: @Composable () -> Unit = { MiniPlayerPlaceholder() },
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val comingSoonMessage = stringResource(R.string.placeholder_coming_soon)

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentTopLevelDestination = TopLevelDestination.entries.firstOrNull { destination ->
        currentDestination?.hierarchy?.any {
            it.hasRoute(destination.routeClass)
        } == true
    }

    // currentTopLevelDestination flips the instant navigate() is called — well
    // before the NavHost's own crossfade finishes showing the new destination.
    // Applying that flip to Scaffold's bottomBar/topBar slots immediately grows
    // or shrinks the content area mid-transition, while the outgoing screen is
    // still visibly fading out — on a short list, that resize is enough to
    // trigger LazyColumn/Grid's "don't leave empty space below the last item"
    // re-anchoring, seen as the list snapping to a different scroll position
    // for an instant. Delaying the chrome flip until the transition has had
    // time to finish means the outgoing screen keeps its exact prior size for
    // its entire exit, and only the already-settled incoming screen is affected.
    var showChrome by remember { mutableStateOf(currentTopLevelDestination != null) }
    LaunchedEffect(currentTopLevelDestination != null) {
        delay(CHROME_VISIBILITY_DELAY_MS)
        showChrome = currentTopLevelDestination != null
    }

    SharedTransitionLayout {
        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
            Scaffold(
                modifier = modifier,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    if (showChrome) {
                        MozicTopBar(
                            onAvatarClick = {
                                navController.navigateToTopLevelDestination(TopLevelDestination.PROFILE)
                            },
                            onNotificationsClick = {
                                coroutineScope.launch { snackbarHostState.showSnackbar(comingSoonMessage) }
                            },
                            onSettingsClick = navController::navigateToSettings,
                        )
                    }
                },
                bottomBar = {
                    if (showChrome) {
                        Column {
                            miniPlayer()
                            MozicBottomBar(
                                destinations = TopLevelDestination.entries,
                                currentDestination = currentDestination,
                                onNavigateToDestination = navController::navigateToTopLevelDestination,
                            )
                        }
                    }
                },
            ) { innerPadding ->
                MozicNavHost(
                    navController = navController,
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }
}
