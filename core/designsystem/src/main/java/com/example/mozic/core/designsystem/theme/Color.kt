package com.example.mozic.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Mozic brand palette.
 *
 * The brand tones are defined once here and then mapped into [LightColors] and
 * [DarkColors]. UI code must never reference these raw tokens directly — always
 * read `MaterialTheme.colorScheme`. Material dynamic color is intentionally not
 * used: the spec calls for our own optimised palette in both light and dark.
 */

// Primary — Mozic violet.
val Violet10 = Color(0xFF23005C)
val Violet20 = Color(0xFF3A1D79)
val Violet30 = Color(0xFF523597)
val Violet40 = Color(0xFF6B4EB6)
val Violet80 = Color(0xFFD2BCFF)
val Violet90 = Color(0xFFEBDDFF)
val Violet100 = Color(0xFFFFFFFF)

// Secondary — muted violet-grey.
val SecViolet10 = Color(0xFF1D1A26)
val SecViolet20 = Color(0xFF322F3C)
val SecViolet30 = Color(0xFF494553)
val SecViolet40 = Color(0xFF615D6C)
val SecViolet80 = Color(0xFFCBC3DB)
val SecViolet90 = Color(0xFFE8DEF8)

// Tertiary — magenta accent for highlights and now-playing surfaces.
val Magenta10 = Color(0xFF3B0021)
val Magenta20 = Color(0xFF5D1138)
val Magenta30 = Color(0xFF7B2A4F)
val Magenta40 = Color(0xFF9B4167)
val Magenta80 = Color(0xFFFFB1C8)
val Magenta90 = Color(0xFFFFD9E2)

// Neutrals — backgrounds and surfaces.
val Neutral0 = Color(0xFF000000)
val Neutral10 = Color(0xFF1C1B1F)
val Neutral20 = Color(0xFF313033)
val Neutral90 = Color(0xFFE6E1E9)
val Neutral95 = Color(0xFFF4EFF7)
val Neutral99 = Color(0xFFFFFBFF)
val Neutral100 = Color(0xFFFFFFFF)

// Neutral variants — outlines and lower-emphasis text.
val NeutralVariant30 = Color(0xFF48454E)
val NeutralVariant50 = Color(0xFF79747E)
val NeutralVariant60 = Color(0xFF938F99)
val NeutralVariant80 = Color(0xFFCAC4D0)
val NeutralVariant90 = Color(0xFFE7E0EC)

// Error.
val Error10 = Color(0xFF410002)
val Error20 = Color(0xFF690005)
val Error30 = Color(0xFF93000A)
val Error40 = Color(0xFFBA1A1A)
val Error80 = Color(0xFFFFB4AB)
val Error90 = Color(0xFFFFDAD6)

val LightColors = lightColorScheme(
    primary = Violet40,
    onPrimary = Violet100,
    primaryContainer = Violet90,
    onPrimaryContainer = Violet10,
    secondary = SecViolet40,
    onSecondary = Neutral100,
    secondaryContainer = SecViolet90,
    onSecondaryContainer = SecViolet10,
    tertiary = Magenta40,
    onTertiary = Neutral100,
    tertiaryContainer = Magenta90,
    onTertiaryContainer = Magenta10,
    error = Error40,
    onError = Neutral100,
    errorContainer = Error90,
    onErrorContainer = Error10,
    background = Neutral99,
    onBackground = Neutral10,
    surface = Neutral99,
    onSurface = Neutral10,
    surfaceVariant = NeutralVariant90,
    onSurfaceVariant = NeutralVariant30,
    outline = NeutralVariant50,
    outlineVariant = NeutralVariant80,
    inverseSurface = Neutral20,
    inverseOnSurface = Neutral95,
    inversePrimary = Violet80,
    surfaceTint = Violet40,
    scrim = Neutral0,
)

val DarkColors = darkColorScheme(
    primary = Violet80,
    onPrimary = Violet20,
    primaryContainer = Violet30,
    onPrimaryContainer = Violet90,
    secondary = SecViolet80,
    onSecondary = SecViolet20,
    secondaryContainer = SecViolet30,
    onSecondaryContainer = SecViolet90,
    tertiary = Magenta80,
    onTertiary = Magenta20,
    tertiaryContainer = Magenta30,
    onTertiaryContainer = Magenta90,
    error = Error80,
    onError = Error20,
    errorContainer = Error30,
    onErrorContainer = Error90,
    background = Neutral10,
    onBackground = Neutral90,
    surface = Neutral10,
    onSurface = Neutral90,
    surfaceVariant = NeutralVariant30,
    onSurfaceVariant = NeutralVariant80,
    outline = NeutralVariant60,
    outlineVariant = NeutralVariant30,
    inverseSurface = Neutral90,
    inverseOnSurface = Neutral20,
    inversePrimary = Violet40,
    surfaceTint = Violet80,
    scrim = Neutral0,
)
