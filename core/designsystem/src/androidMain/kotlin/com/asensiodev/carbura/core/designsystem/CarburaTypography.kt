package com.asensiodev.carbura.core.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.asensiodev.carbura.coredesignsystem.R

private val GoogleFontsProvider =
    GoogleFont.Provider(
        providerAuthority = "com.google.android.gms.fonts",
        providerPackage = "com.google.android.gms",
        certificates = R.array.com_google_android_gms_fonts_certs,
    )

private fun googleFontFamily(name: String): FontFamily =
    FontFamily(
        Font(GoogleFont(name), GoogleFontsProvider, FontWeight.Normal),
        Font(GoogleFont(name), GoogleFontsProvider, FontWeight.Medium),
        Font(GoogleFont(name), GoogleFontsProvider, FontWeight.SemiBold),
        Font(GoogleFont(name), GoogleFontsProvider, FontWeight.Bold),
    )

private val DisplayFontFamily = googleFontFamily("Inter")
private val BodyFontFamily = googleFontFamily("Poppins")
private val DefaultTypography = Typography()

internal val CarburaTypography =
    Typography(
        displayLarge = DefaultTypography.displayLarge.copy(fontFamily = DisplayFontFamily),
        displayMedium = DefaultTypography.displayMedium.copy(fontFamily = DisplayFontFamily),
        displaySmall = DefaultTypography.displaySmall.copy(fontFamily = DisplayFontFamily),
        headlineLarge = DefaultTypography.headlineLarge.copy(fontFamily = DisplayFontFamily),
        headlineMedium = DefaultTypography.headlineMedium.copy(fontFamily = DisplayFontFamily),
        headlineSmall = DefaultTypography.headlineSmall.copy(fontFamily = DisplayFontFamily),
        titleLarge = DefaultTypography.titleLarge.copy(fontFamily = DisplayFontFamily),
        titleMedium = DefaultTypography.titleMedium.copy(fontFamily = DisplayFontFamily),
        titleSmall = DefaultTypography.titleSmall.copy(fontFamily = DisplayFontFamily),
        bodyLarge = DefaultTypography.bodyLarge.copy(fontFamily = BodyFontFamily),
        bodyMedium = DefaultTypography.bodyMedium.copy(fontFamily = BodyFontFamily),
        bodySmall = DefaultTypography.bodySmall.copy(fontFamily = BodyFontFamily),
        labelLarge = DefaultTypography.labelLarge.copy(fontFamily = BodyFontFamily),
        labelMedium = DefaultTypography.labelMedium.copy(fontFamily = BodyFontFamily),
        labelSmall = DefaultTypography.labelSmall.copy(fontFamily = BodyFontFamily),
    )
