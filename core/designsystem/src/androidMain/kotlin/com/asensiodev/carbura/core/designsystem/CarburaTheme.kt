package com.asensiodev.carbura.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CarburaColorScheme =
    darkColorScheme(
        primary = CarburaColors.Gold,
        background = CarburaColors.DeepGreen,
        surface = CarburaColors.DeepGreen,
        onPrimary = CarburaColors.DeepGreen,
        onBackground = CarburaColors.Cream,
        onSurface = CarburaColors.Cream,
    )

@Composable
fun CarburaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CarburaColorScheme,
        content = content,
    )
}
