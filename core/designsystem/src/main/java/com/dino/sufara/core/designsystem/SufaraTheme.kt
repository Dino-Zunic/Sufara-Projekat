package com.dino.sufara.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = OrangeTerra,
    secondary = BlueTeal,
    background = BlueDeep,
    surface = BlueTeal,
    onPrimary = WhitePure,
    onSecondary = WhiteMint,
    onBackground = WhiteMint,
    onSurface = WhiteMint,
    error = RedCoral
)

@Composable
fun SufaraTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = SufaraTypography,
        content = content
    )
}