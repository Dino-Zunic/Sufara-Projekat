package com.dino.sufara.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val BlueQuranColorScheme = darkColorScheme(
    primary = BlueRoyal,
    secondary = GoldBase,
    background = BlueAbyss,
    surface = BlueMidnight,
    onPrimary = TextParchment,
    onSecondary = BlueAbyss,
    onBackground = TextParchment,
    onSurface = TextParchment,
    error = ErrorRed
)

@Composable
fun SufaraTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BlueQuranColorScheme,
        typography = SufaraTypography,
        content = content
    )
}