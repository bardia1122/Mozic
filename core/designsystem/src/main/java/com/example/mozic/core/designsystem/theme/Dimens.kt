package com.example.mozic.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Mozic spacing scale (4 / 8 / 12 / 16 / 24 / 32). Layouts read padding and gaps
 * from here via `MaterialTheme.dimens` instead of hardcoding dp values, so the
 * rhythm stays uniform and is tunable in one place.
 */
@Immutable
data class Dimens(
    val spaceXxs: Dp = 4.dp,
    val spaceXs: Dp = 8.dp,
    val spaceSm: Dp = 12.dp,
    val spaceMd: Dp = 16.dp,
    val spaceLg: Dp = 24.dp,
    val spaceXl: Dp = 32.dp,
    /** Cover art side for song/playlist cards in horizontal rows. */
    val cardImageSize: Dp = 120.dp,
    /** Height of the Home carousel's big-imagery pages. */
    val carouselHeight: Dp = 200.dp,
    /** Pager page-indicator dot diameter. */
    val indicatorDotSize: Dp = 6.dp,
    /** Approximate single text-line height for skeleton placeholders. */
    val skeletonLineHeight: Dp = 14.dp,
    /** Leading thumbnail/avatar side for single-column list rows (search results, song lists). */
    val listRowImageSize: Dp = 48.dp,
)

val LocalDimens = staticCompositionLocalOf { Dimens() }

/** Convenience accessor mirroring `MaterialTheme.colorScheme` / `.typography`. */
val MaterialTheme.dimens: Dimens
    @Composable
    @ReadOnlyComposable
    get() = LocalDimens.current
