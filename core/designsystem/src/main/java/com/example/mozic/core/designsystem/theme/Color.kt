package com.example.mozic.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Mozic brand palette — per `doc/DESIGN.md` §1. One signature accent (Electric
 * Cyan) on near-black surfaces; token *names* are shared between dark and
 * light, only the values differ. UI code must never reference these raw
 * `Color(...)` values directly — always read `MaterialTheme.colorScheme` (for
 * roles that have a natural Material3 slot) or `MaterialTheme.mozicColors`
 * (for the two that don't, see [MozicExtendedColors]).
 */

// Dark set — DESIGN.md §1 table, values as specified.
val DarkBackground = Color(0xFF0A1113)
val DarkSurface = Color(0xFF121B1E)
val DarkSurfaceElevated = Color(0xFF172225)
val DarkSurfaceContainerHighest = Color(0xFF1D2A2E)
// Shimmer sweep's highlight step (MozicExtendedColors.shimmerHighlight) —
// noticeably lighter than DarkSurfaceElevated, since surfaceContainerHighest
// is itself a near-black and reads as barely-there against a near-black bg.
val DarkShimmerHighlight = Color(0xFF2A3B42)
val DarkBorder = Color(0xFF1E2A2E)
val DarkTextPrimary = Color(0xFFEAF3F5)
val DarkTextSecondary = Color(0xFF8FA5AA)
val DarkTextTertiary = Color(0xFF55686D)
val DarkAccent = Color(0xFF2FE0C4)
val DarkOnAccent = Color(0xFF06211D)
val DarkDestructive = Color(0xFFFF7A6B)
val DarkDestructiveBorder = Color(0xFF46282A)

// Light set — DESIGN.md §1 only gives background/surface/textPrimary/accent
// explicitly ("out of scope for these mocks; keep the same token contract").
// The rest are derived here following the same tonal relationships as the
// dark set (surface raised above background, elevated a further step up,
// secondary/tertiary text progressively muted, accent deepened for contrast).
val LightBackground = Color(0xFFF7F5F0)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceElevated = Color(0xFFF1EEE7)
val LightShimmerHighlight = Color(0xFFFFFFFF)
val LightBorder = Color(0xFFE3DED3)
val LightTextPrimary = Color(0xFF141A1B)
val LightTextSecondary = Color(0xFF5C6B6E)
val LightTextTertiary = Color(0xFF93A0A2)
val LightAccent = Color(0xFF1FB79E)
val LightOnAccent = Color(0xFFFFFFFF)
val LightDestructive = Color(0xFFC43D2E)
val LightDestructiveBorder = Color(0xFFE8C4BC)

val DarkColors: ColorScheme = darkColorScheme(
    primary = DarkAccent,
    onPrimary = DarkOnAccent,
    primaryContainer = DarkSurfaceElevated,
    onPrimaryContainer = DarkTextPrimary,
    inversePrimary = DarkOnAccent,
    secondary = DarkTextSecondary,
    onSecondary = DarkBackground,
    secondaryContainer = DarkSurfaceElevated,
    onSecondaryContainer = DarkTextPrimary,
    tertiary = DarkTextTertiary,
    onTertiary = DarkBackground,
    tertiaryContainer = DarkSurfaceElevated,
    onTertiaryContainer = DarkTextPrimary,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = DarkTextSecondary,
    surfaceTint = DarkSurface,
    inverseSurface = DarkTextPrimary,
    inverseOnSurface = DarkSurface,
    error = DarkDestructive,
    onError = DarkBackground,
    errorContainer = DarkSurfaceElevated,
    onErrorContainer = DarkDestructive,
    outline = DarkBorder,
    outlineVariant = DarkBorder,
    scrim = Color.Black,
    surfaceBright = DarkSurfaceElevated,
    surfaceContainer = DarkSurfaceElevated,
    surfaceContainerHigh = DarkSurfaceElevated,
    surfaceContainerHighest = DarkSurfaceContainerHighest,
    surfaceContainerLow = DarkSurface,
    surfaceContainerLowest = DarkBackground,
    surfaceDim = DarkBackground,
)

val LightColors: ColorScheme = lightColorScheme(
    primary = LightAccent,
    onPrimary = LightOnAccent,
    primaryContainer = LightSurfaceElevated,
    onPrimaryContainer = LightTextPrimary,
    inversePrimary = LightOnAccent,
    secondary = LightTextSecondary,
    onSecondary = LightBackground,
    secondaryContainer = LightSurfaceElevated,
    onSecondaryContainer = LightTextPrimary,
    tertiary = LightTextTertiary,
    onTertiary = LightBackground,
    tertiaryContainer = LightSurfaceElevated,
    onTertiaryContainer = LightTextPrimary,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = LightTextSecondary,
    surfaceTint = LightSurface,
    inverseSurface = LightTextPrimary,
    inverseOnSurface = LightSurface,
    error = LightDestructive,
    onError = LightBackground,
    errorContainer = LightSurfaceElevated,
    onErrorContainer = LightDestructive,
    outline = LightBorder,
    outlineVariant = LightBorder,
    scrim = Color.Black,
    surfaceBright = LightSurfaceElevated,
    surfaceContainer = LightSurfaceElevated,
    surfaceContainerHigh = LightSurfaceElevated,
    surfaceContainerHighest = LightSurface,
    surfaceContainerLow = LightSurface,
    surfaceContainerLowest = LightBackground,
    surfaceDim = LightBackground,
)
