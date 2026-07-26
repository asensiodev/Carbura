package com.asensiodev.carbura.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CarburaColorScheme =
    lightColorScheme(
        primary = CarburaColors.Blue20,
        onPrimary = Color.White,
        primaryContainer = CarburaColors.Blue90,
        onPrimaryContainer = CarburaColors.Blue10,
        secondary = CarburaColors.Blue30,
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFDCE6FF),
        onSecondaryContainer = Color(0xFF071B3D),
        tertiary = CarburaColors.Blue40,
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFD7E8FF),
        onTertiaryContainer = Color(0xFF001D35),
        error = Color(0xFFBA1A1A),
        onError = Color.White,
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF410002),
        background = CarburaColors.Blue95,
        onBackground = CarburaColors.Neutral10,
        surface = CarburaColors.Blue95,
        onSurface = CarburaColors.Neutral10,
        surfaceVariant = CarburaColors.Blue90,
        onSurfaceVariant = CarburaColors.Neutral30,
        outline = Color(0xFF74777F),
        outlineVariant = CarburaColors.Blue80,
        inverseSurface = CarburaColors.Neutral20,
        inverseOnSurface = CarburaColors.Neutral95,
        inversePrimary = CarburaColors.Blue80,
        surfaceTint = CarburaColors.Blue20,
        scrim = Color.Black,
        surfaceBright = Color.White,
        surfaceDim = CarburaColors.Blue90,
        surfaceContainerLowest = Color.White,
        surfaceContainerLow = CarburaColors.Blue99,
        surfaceContainer = Color(0xFFF6F9FC),
        surfaceContainerHigh = Color(0xFFF2F6FA),
        surfaceContainerHighest = Color.White,
    )

@Composable
fun CarburaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CarburaColorScheme,
        typography = CarburaTypography,
        content = content,
    )
}
