package com.dino.sufara.core.designsystem

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

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
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Бојимо горњу траку (сат) и доњу (навигација) у дубоку плаву позадину!
            window.statusBarColor = BlueAbyss.toArgb()
            window.navigationBarColor = BlueAbyss.toArgb()
            // Стављамо беле иконице (сат, батерија)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = BlueQuranColorScheme,
        typography = SufaraTypography,
        content = content
    )
}