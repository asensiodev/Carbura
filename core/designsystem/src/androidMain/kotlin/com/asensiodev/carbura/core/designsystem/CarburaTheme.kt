package com.asensiodev.carbura.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CarburaColorScheme =
    darkColorScheme(
        primary = CarburaColors.Gold,
        onPrimary = CarburaColors.DeepGreen,
        primaryContainer = CarburaColors.GoldContainer,
        onPrimaryContainer = CarburaColors.OnGoldContainer,
        inversePrimary = CarburaColors.GoldContainer,
        secondary = CarburaColors.Sage,
        onSecondary = CarburaColors.OnSage,
        secondaryContainer = CarburaColors.SageContainer,
        onSecondaryContainer = CarburaColors.OnSageContainer,
        tertiary = CarburaColors.Copper,
        onTertiary = CarburaColors.OnCopper,
        tertiaryContainer = CarburaColors.CopperContainer,
        onTertiaryContainer = CarburaColors.OnCopperContainer,
        background = CarburaColors.DeepGreen,
        onBackground = CarburaColors.Cream,
        surface = CarburaColors.DeepGreen,
        onSurface = CarburaColors.Cream,
        surfaceVariant = CarburaColors.HighGreen,
        onSurfaceVariant = CarburaColors.MutedCream,
        surfaceTint = CarburaColors.Gold,
        inverseSurface = CarburaColors.Cream,
        inverseOnSurface = CarburaColors.DeepGreen,
        error = CarburaColors.Error,
        onError = CarburaColors.OnError,
        errorContainer = CarburaColors.ErrorContainer,
        onErrorContainer = CarburaColors.OnErrorContainer,
        outline = CarburaColors.Outline,
        outlineVariant = CarburaColors.HighGreen,
        scrim = CarburaColors.DeepestGreen,
        surfaceBright = CarburaColors.HighestGreen,
        surfaceDim = CarburaColors.DeepestGreen,
        surfaceContainer = CarburaColors.RaisedGreen,
        surfaceContainerHigh = CarburaColors.HighGreen,
        surfaceContainerHighest = CarburaColors.HighestGreen,
        surfaceContainerLow = CarburaColors.DarkGreen,
        surfaceContainerLowest = CarburaColors.DeepestGreen,
    )

@Composable
fun CarburaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CarburaColorScheme,
        content = content,
    )
}
