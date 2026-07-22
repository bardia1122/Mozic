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
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mozic.core.ui.animation.LocalMiniPlayerAnimatedVisibilityScope
import com.example.mozic.core.ui.animation.LocalSharedTransitionScope
import com.example.mozic.feature.chat.navigation.ChatThreadRoute
import com.example.mozic.feature.chat.navigation.navigateToConversationList
import com.example.mozic.feature.player.MiniPlayerBar
import com.example.mozic.feature.player.navigation.NowPlayingRoute
import com.example.mozic.feature.player.navigation.navigateToNowPlaying
import com.example.mozic.feature.social.navigation.navigateToUserSearch
import com.example.mozic.navigation.MozicNavHost
import com.example.mozic.navigation.TopLevelDestination
import com.example.mozic.navigation.navigateToSettings
import com.example.mozic.navigation.navigateToTopLevelDestination
import com.example.mozic.ui.MozicBottomBar
import com.example.mozic.ui.MozicTopBar

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
    // Same activity-scoped `AppViewModel` instance `MainActivity` already holds for theme prefs —
    // `hiltViewModel()` with no NavBackStackEntry resolves to the nearest ViewModelStoreOwner.
    val appViewModel = hiltViewModel<AppViewModel>()
    val avatarUrl by appViewModel.avatarUrl.collectAsStateWithLifecycle()
    val isLoggedIn by appViewModel.isLoggedIn.collectAsStateWithLifecycle()

    // `openNowPlayingSignal` increments once per tap on the media notification (see
    // MainActivity) — keyed on its value, not Unit, so a second tap while already on Now
    // Playing still re-fires the navigation call (harmless no-op via launchSingleTop upstream).
    LaunchedEffect(openNowPlayingSignal) {
        if (openNowPlayingSignal > 0) navController.navigateToNowPlaying()
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Deny-list, not allow-list: only these are genuinely full-screen
    // experiences with their own back affordance. Everything else — playlist
    // detail, liked/recently-played, Settings, and any future drill-down
    // screen — is just deeper content reached from a tab (or the top bar) and
    // should keep the top bar, bottom nav, and mini player visible (previously
    // this was an allow-list keyed on `TopLevelDestination`, which incorrectly
    // hid the mini player on every non-tab screen, including playlist detail).
    val isFullScreenDestination = currentDestination?.hierarchy?.any {
        it.hasRoute(NowPlayingRoute::class) || it.hasRoute(ChatThreadRoute::class)
    } == true

    // `currentDestination` starts `null` on the very first composition (the
    // NavHost hasn't attached its graph to `currentBackStackEntryAsState()`
    // yet), which would otherwise animate chrome in from hidden on cold
    // launch — treat the not-yet-attached frame as chrome-visible too.
    val showChrome = currentDestination == null || !isFullScreenDestination

    SharedTransitionLayout {
        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
            Scaffold(
                modifier = modifier,
                topBar = {
                    AnimatedVisibility(
                        visible = showChrome,
                        enter = fadeIn(tween(CHROME_TRANSITION_MS)) +
                            slideInVertically(tween(CHROME_TRANSITION_MS)) { fullHeight -> -fullHeight },
                        exit = fadeOut(tween(CHROME_TRANSITION_MS)) +
                            slideOutVertically(tween(CHROME_TRANSITION_MS)) { fullHeight -> -fullHeight },
                    ) {
                        MozicTopBar(
                            avatarUrl = avatarUrl,
                            onAvatarClick = {
                                if (isLoggedIn) {
                                    navController.navigateToTopLevelDestination(TopLevelDestination.PROFILE)
                                } else {
                                    // No session yet: Profile works logged-out too, but the user asked to be
                                    // routed straight to sign-in instead. Chat's login form doubles as the
                                    // app's only sign-in/sign-up screen (see ConversationListScreen).
                                    navController.navigateToConversationList()
                                }
                            },
                            onSocialClick = navController::navigateToUserSearch,
                            onChatClick = navController::navigateToConversationList,
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
                                    // Profile is reachable via the top bar's avatar instead (see
                                    // MozicTopBar) — not a bottom-nav tab.
                                    destinations = TopLevelDestination.entries
                                        .filter { it != TopLevelDestination.PROFILE },
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
                    modifier = Modifier
                        .padding(innerPadding)
                        .consumeWindowInsets(innerPadding),
                )
            }
        }
    }
}
