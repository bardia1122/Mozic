package com.example.mozic.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Mozic brand palette — per `doc/DESIGN.md` §1. One signature accent
 * ("Gilded" metallic gold) on near-black surfaces; token *names* are shared
 * between dark and light, only the values differ. UI code must never
 * reference these raw `Color(...)` values directly — always read
 * `MaterialTheme.colorScheme` (for roles that have a natural Material3 slot)
 * or `MaterialTheme.mozicColors` (for the ones that don't, see
 * [MozicExtendedColors]).
 */

// Dark set — DESIGN.md §1 table, values as specified.
val DarkBackground = Color(0xFF120E09)
val DarkSurface = Color(0xFF1B140C)
val DarkSurfaceElevated = Color(0xFF241C12)
val DarkSurfaceContainerHighest = Color(0xFF2E2417)
// Shimmer sweep's highlight step (MozicExtendedColors.shimmerHighlight) —
// noticeably lighter than DarkSurfaceElevated, since surfaceContainerHighest
// is itself a near-black and reads as barely-there against a near-black bg.
val DarkShimmerHighlight = Color(0xFF37352F)
val DarkBorder = Color(0xFF2B2119)
val DarkTextPrimary = Color(0xFFF3E9DA)
val DarkTextSecondary = Color(0xFFB7A490)
val DarkTextTertiary = Color(0xFF7A6A58)
val DarkAccent = Color(0xFFC6952E)
val DarkAccentGradientStart = Color(0xFFF0C878)
val DarkAccentGradientEnd = Color(0xFF9A6E20)
val DarkOnAccent = Color(0xFF18120C)
val DarkDestructive = Color(0xFFFF7A6B)
val DarkDestructiveBorder = Color(0xFF462424)

// Light set — DESIGN.md §1 only gives background/surface/textPrimary/accent
// explicitly ("out of scope for these mocks; keep the same token contract").
// The rest are derived here following the same tonal relationships as the
// dark set (surface raised above background, elevated a further step up,
// secondary/tertiary text progressively muted), just in the same warm
// gold/brown hue family instead of the old teal one. Accent (and its
// gradient/onAccent pair) is identical to dark per the doc — the gold reads
// fine on a light cream background without deepening.
val LightBackground = Color(0xFFFBF6EC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceElevated = Color(0xFFF3EAD7)
val LightShimmerHighlight = Color(0xFFFFFFFF)
val LightBorder = Color(0xFFE4D9C5)
val LightTextPrimary = Color(0xFF1B140C)
val LightTextSecondary = Color(0xFF6B5C4A)
val LightTextTertiary = Color(0xFF9C8D78)
val LightAccent = DarkAccent
val LightAccentGradientStart = DarkAccentGradientStart
val LightAccentGradientEnd = DarkAccentGradientEnd
val LightOnAccent = DarkOnAccent
val LightDestructive = Color(0xFFC43D2E)
val LightDestructiveBorder = Color(0xFFE8C4BC)

/**
 * The "gilded" brass-to-gold fill — DESIGN.md §1's `accentGradient`. Used for
 * primary buttons, the active filter chip, and (once built) the Now Playing
 * play/pause button and progress fill. Identical in both themes, same as the
 * flat `accent` token it pairs with.
 */
val AccentGradient: Brush = Brush.linearGradient(
    colors = listOf(DarkAccentGradientStart, DarkAccentGradientEnd),
)

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
