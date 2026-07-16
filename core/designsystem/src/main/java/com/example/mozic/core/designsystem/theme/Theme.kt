package com.example.mozic.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

/**
 * Root theme for Mozic.
 *
 * @param darkTheme whether to use the dark [DarkColors] scheme. Callers resolve
 *   the user's [ThemeSetting] to this boolean at the app root — `SYSTEM` maps to
 *   [isSystemInDarkTheme], `LIGHT`/`DARK` to `false`/`true`. Keeping the enum out
 *   of `:core:designsystem` lets it stay a pure UI toolkit with no domain deps.
 * @param fontScale user-controlled text scaling, applied by wrapping
 *   [LocalDensity] so every `sp` value scales without touching call sites.
 *
 * Material dynamic color is deliberately not offered: Mozic ships its own
 * palette in both modes.
 */
@Composable
fun MozicTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    fontScale: Float = 1f,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val extendedColors = if (darkTheme) {
        MozicExtendedColors(
            textTertiary = DarkTextTertiary,
            destructiveBorder = DarkDestructiveBorder,
            shimmerHighlight = DarkShimmerHighlight,
            accentGradient = AccentGradient,
        )
    } else {
        MozicExtendedColors(
            textTertiary = LightTextTertiary,
            destructiveBorder = LightDestructiveBorder,
            shimmerHighlight = LightShimmerHighlight,
            accentGradient = AccentGradient,
        )
    }

    val density = LocalDensity.current
    val scaledDensity = Density(
        density = density.density,
        fontScale = density.fontScale * fontScale,
    )

    CompositionLocalProvider(
        LocalDimens provides Dimens(),
        LocalMozicColors provides extendedColors,
        LocalDensity provides scaledDensity,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = mozicTypography(),
            shapes = MozicShapes,
            content = content,
        )
    }
}
