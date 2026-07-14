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
)

val LocalDimens = staticCompositionLocalOf { Dimens() }

/** Convenience accessor mirroring `MaterialTheme.colorScheme` / `.typography`. */
val MaterialTheme.dimens: Dimens
    @Composable
    @ReadOnlyComposable
    get() = LocalDimens.current
