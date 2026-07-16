package com.example.mozic.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.example.mozic.core.designsystem.R

/**
 * Typography for Mozic — per `doc/DESIGN.md` §2.
 *
 * UI font is Poppins; mono-eyebrow role (timestamps, section eyebrows,
 * numeric counters) is IBM Plex Mono. Neither covers Persian glyphs, so the
 * Farsi locale keeps F2's Vazirmatn across every slot instead — resolved at
 * composition time in [mozicTypography] by reading [LocalConfiguration],
 * mirroring how [MozicTheme] reads `isSystemInDarkTheme()` internally rather
 * than taking a domain-typed parameter (`:core:designsystem` stays free of
 * domain types, see `ThemeSetting` note in `Theme.kt`).
 */
private val fontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

private val Vazirmatn = GoogleFont("Vazirmatn")
private val Poppins = GoogleFont("Poppins")
private val IBMPlexMono = GoogleFont("IBM Plex Mono")

private val VazirmatnFamily = FontFamily(
    Font(googleFont = Vazirmatn, fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = Vazirmatn, fontProvider = fontProvider, weight = FontWeight.Medium),
    Font(googleFont = Vazirmatn, fontProvider = fontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = Vazirmatn, fontProvider = fontProvider, weight = FontWeight.Bold),
    Font(googleFont = Vazirmatn, fontProvider = fontProvider, weight = FontWeight.ExtraBold),
)

private val PoppinsFamily = FontFamily(
    Font(googleFont = Poppins, fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = Poppins, fontProvider = fontProvider, weight = FontWeight.Medium),
    Font(googleFont = Poppins, fontProvider = fontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = Poppins, fontProvider = fontProvider, weight = FontWeight.Bold),
    Font(googleFont = Poppins, fontProvider = fontProvider, weight = FontWeight.ExtraBold),
)

private val PlexMonoFamily = FontFamily(
    Font(googleFont = IBMPlexMono, fontProvider = fontProvider, weight = FontWeight.Medium),
    Font(googleFont = IBMPlexMono, fontProvider = fontProvider, weight = FontWeight.Bold),
)

private const val FARSI_LANGUAGE_TAG = "fa"
private const val EYEBROW_LETTER_SPACING_EM = 0.05

/** Screen title (`MozicTopBar`, `SettingsScreen`'s `TopAppBar`). */
private fun titleLargeStyle(family: FontFamily) = TextStyle(
    fontFamily = family,
    fontWeight = FontWeight.ExtraBold,
    fontSize = 20.sp,
    lineHeight = 26.sp,
)

/** Section header (e.g. `SettingsSection`, Home row titles). */
private fun titleMediumStyle(family: FontFamily) = TextStyle(
    fontFamily = family,
    fontWeight = FontWeight.Bold,
    fontSize = 15.sp,
    lineHeight = 20.sp,
)

/** Card title (`SongCard`, `PlaylistCard`, `MediaListRow`). */
private fun titleSmallStyle(family: FontFamily) = TextStyle(
    fontFamily = family,
    fontWeight = FontWeight.Bold,
    fontSize = 14.sp,
    lineHeight = 18.sp,
)

private fun bodyLargeStyle(family: FontFamily) = TextStyle(
    fontFamily = family,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 22.sp,
    letterSpacing = 0.5.sp,
)

/** Body / labels. */
private fun bodyMediumStyle(family: FontFamily) = TextStyle(
    fontFamily = family,
    fontWeight = FontWeight.Medium,
    fontSize = 13.sp,
    lineHeight = 18.sp,
    letterSpacing = 0.25.sp,
)

private fun bodySmallStyle(family: FontFamily) = TextStyle(
    fontFamily = family,
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.4.sp,
)

private fun labelLargeStyle(family: FontFamily) = TextStyle(
    fontFamily = family,
    fontWeight = FontWeight.SemiBold,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.1.sp,
)

private fun labelMediumStyle(family: FontFamily) = TextStyle(
    fontFamily = family,
    fontWeight = FontWeight.SemiBold,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.5.sp,
)

/**
 * Caption / mono eyebrow (timestamps, uppercase section eyebrows like "THEME").
 * Callers are responsible for `.uppercase()`-ing eyebrow strings themselves —
 * this style only sets the face, weight and tracking.
 */
private fun labelSmallStyle(family: FontFamily) = TextStyle(
    fontFamily = family,
    fontWeight = FontWeight.Bold,
    fontSize = 11.sp,
    lineHeight = 14.sp,
    letterSpacing = (EYEBROW_LETTER_SPACING_EM * 11).sp,
)

private fun displayStyles(family: FontFamily) = Typography(
    displayLarge = TextStyle(
        fontFamily = family,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = family,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp,
    ),
    displaySmall = TextStyle(
        fontFamily = family,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = family,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = family,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = family,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
    ),
)

/** Resolves Mozic's [Typography] for the current locale — Farsi keeps Vazirmatn everywhere. */
@Composable
fun mozicTypography(): Typography {
    val isFarsi = LocalConfiguration.current.locales[0].language == FARSI_LANGUAGE_TAG
    val uiFamily = if (isFarsi) VazirmatnFamily else PoppinsFamily
    val eyebrowFamily = if (isFarsi) VazirmatnFamily else PlexMonoFamily

    val base = displayStyles(uiFamily)
    return base.copy(
        titleLarge = titleLargeStyle(uiFamily),
        titleMedium = titleMediumStyle(uiFamily),
        titleSmall = titleSmallStyle(uiFamily),
        bodyLarge = bodyLargeStyle(uiFamily),
        bodyMedium = bodyMediumStyle(uiFamily),
        bodySmall = bodySmallStyle(uiFamily),
        labelLarge = labelLargeStyle(uiFamily),
        labelMedium = labelMediumStyle(uiFamily),
        labelSmall = labelSmallStyle(eyebrowFamily),
    )
}
