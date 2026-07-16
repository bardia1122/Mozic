package com.example.mozic.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Tokens with no natural `ColorScheme` role: DESIGN.md §1's `textTertiary`
 * (disabled state, placeholder icons) and the destructive button's tinted
 * outline (kept distinct from `colorScheme.error`'s solid text color), the
 * shimmer sweep's highlight step (needs more contrast against a near-black
 * theme than reusing a real `surfaceContainer*` role would give, see
 * `Shimmer.kt`), and `accentGradient` (the "gilded" fill for primary
 * buttons/active chips — `ColorScheme` roles are flat `Color`, not `Brush`,
 * so this can't live there). Read via `MaterialTheme.mozicColors`, same
 * pattern as `MaterialTheme.dimens` in [Dimens].
 */
@Immutable
data class MozicExtendedColors(
    val textTertiary: Color,
    val destructiveBorder: Color,
    val shimmerHighlight: Color,
    val accentGradient: Brush,
)

val LocalMozicColors = staticCompositionLocalOf {
    MozicExtendedColors(
        textTertiary = DarkTextTertiary,
        destructiveBorder = DarkDestructiveBorder,
        shimmerHighlight = DarkShimmerHighlight,
        accentGradient = AccentGradient,
    )
}

val MaterialTheme.mozicColors: MozicExtendedColors
    @Composable
    @ReadOnlyComposable
    get() = LocalMozicColors.current
