package com.example.mozic.core.ui.animation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.compositionLocalOf

/**
 * Provided by `:app`'s root `SharedTransitionLayout` wrapping the NavHost, so
 * feature screens (e.g. Person A's now-playing shared-element transition) can
 * read the enclosing [SharedTransitionScope] without depending on `:app`.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

/**
 * The [AnimatedVisibilityScope] of the mini-player's own show/hide
 * `AnimatedVisibility` in `:app`'s bottom bar slot. The mini player lives
 * outside the NavHost (it's not a nav destination), so it has no
 * `AnimatedContentScope` of its own to pair with the Now Playing screen's
 * nav-transition scope for `Modifier.sharedElement` — this is that missing
 * half, provided by `:app` at the exact point the mini player composes.
 */
val LocalMiniPlayerAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }
