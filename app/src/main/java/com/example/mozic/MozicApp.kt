package com.example.mozic

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mozic.core.designsystem.R
import com.example.mozic.core.ui.animation.LocalMiniPlayerAnimatedVisibilityScope
import com.example.mozic.core.ui.animation.LocalSharedTransitionScope
import com.example.mozic.feature.player.MiniPlayerBar
import com.example.mozic.feature.player.navigation.navigateToNowPlaying
import com.example.mozic.navigation.MozicNavHost
import com.example.mozic.navigation.TopLevelDestination
import com.example.mozic.navigation.navigateToSettings
import com.example.mozic.navigation.navigateToTopLevelDestination
import com.example.mozic.ui.MozicBottomBar
import com.example.mozic.ui.MozicTopBar
import kotlinx.coroutines.launch

/**
 * Duration for the chrome's own slide+fade, independent of whatever
 * transition the destination content underneath happens to use. Previously
 * chrome was an instant on/off gated behind a timer tuned to trail the
 * content transition — see git history for that approach. Animating the
 * chrome itself (rather than snapping it) means the Scaffold's content-area
 * inset changes gradually over this same span instead of jumping in one
 * frame, which is what was actually causing the "not smooth" feel: even
 * once the timer's *timing* was correct, an instant appear/disappear reads
 * as abrupt no matter how well-synced. A gradual inset change also
 * incidentally defuses the `LazyColumn`/`Grid` "don't leave empty space"
 * re-anchor bug B4 fought (small per-frame corrections instead of one
 * visible jump), without needing timer synchronization with the content
 * transition at all.
 */
private const val CHROME_TRANSITION_MS = 220

/**
 * Root app composable. Layout order per spec: content -> mini-player slot ->
 * bottom bar. [miniPlayer] defaults to `:feature:player`'s real
 * `MiniPlayerBar`, wired to expand into the Now Playing screen via this
 * composable's own [navController] (hence the `onExpand` param rather than a
 * zero-arg default) — override only for tests/previews.
 */
@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MozicApp(
    modifier: Modifier = Modifier,
    openNowPlayingSignal: Int = 0,
    miniPlayer: @Composable (onExpand: () -> Unit) -> Unit = { onExpand -> MiniPlayerBar(onExpand = onExpand) },
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val comingSoonMessage = stringResource(R.string.placeholder_coming_soon)

    // `openNowPlayingSignal` increments once per tap on the media notification (see
    // MainActivity) — keyed on its value, not Unit, so a second tap while already on Now
    // Playing still re-fires the navigation call (harmless no-op via launchSingleTop upstream).
    LaunchedEffect(openNowPlayingSignal) {
        if (openNowPlayingSignal > 0) navController.navigateToNowPlaying()
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentTopLevelDestination = TopLevelDestination.entries.firstOrNull { destination ->
        currentDestination?.hierarchy?.any {
            it.hasRoute(destination.routeClass)
        } == true
    }

    // `currentTopLevelDestination` starts `null` on the very first composition
    // (the NavHost hasn't attached its graph to `currentBackStackEntryAsState()`
    // yet), which would otherwise animate chrome in from hidden on cold launch —
    // `MozicNavHost`'s start destination is always a top-level one (`HomeRoute`),
    // so treat the not-yet-attached frame as chrome-visible too.
    val showChrome = currentDestination == null || currentTopLevelDestination != null

    SharedTransitionLayout {
        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
            Scaffold(
                modifier = modifier,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    AnimatedVisibility(
                        visible = showChrome,
                        enter = fadeIn(tween(CHROME_TRANSITION_MS)) +
                            slideInVertically(tween(CHROME_TRANSITION_MS)) { fullHeight -> -fullHeight },
                        exit = fadeOut(tween(CHROME_TRANSITION_MS)) +
                            slideOutVertically(tween(CHROME_TRANSITION_MS)) { fullHeight -> -fullHeight },
                    ) {
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
                    AnimatedVisibility(
                        visible = showChrome,
                        enter = fadeIn(tween(CHROME_TRANSITION_MS)) +
                            slideInVertically(tween(CHROME_TRANSITION_MS)) { fullHeight -> fullHeight },
                        exit = fadeOut(tween(CHROME_TRANSITION_MS)) +
                            slideOutVertically(tween(CHROME_TRANSITION_MS)) { fullHeight -> fullHeight },
                    ) {
                        CompositionLocalProvider(LocalMiniPlayerAnimatedVisibilityScope provides this) {
                            Column {
                                miniPlayer(navController::navigateToNowPlaying)
                                MozicBottomBar(
                                    destinations = TopLevelDestination.entries,
                                    currentDestination = currentDestination,
                                    onNavigateToDestination = navController::navigateToTopLevelDestination,
                                )
                            }
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
