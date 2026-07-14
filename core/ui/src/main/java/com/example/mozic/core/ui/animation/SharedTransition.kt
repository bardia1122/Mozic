package com.example.mozic.core.ui.animation

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
