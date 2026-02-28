package com.dino.sufara.feature.lesson.presentation.settings

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

data class SufaraSettingsState(
    val cyrillicFont: String = "Lora",
    val arabicFont: String = "Noto Naskh",
    val cyrillicSizeMultiplier: Float = 1.0f,
    val arabicSizeMultiplier: Float = 1.0f,
    val isDebugMode: Boolean = true
)

interface SufaraSettingsActions {
    fun updateCyrillicFont(name: String)
    fun updateArabicFont(name: String)
    fun updateCyrillicSize(size: Float)
    fun updateArabicSize(size: Float)
    fun toggleDebugMode(enabled: Boolean)
}

val LocalSufaraSettings = compositionLocalOf { SufaraSettingsState() }
val LocalSufaraSettingsActions = compositionLocalOf<SufaraSettingsActions> { error("Nema akcija") }

@Composable
fun SufaraSettingsProvider(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("sufara_prefs", Context.MODE_PRIVATE) }

    var cyrillicFont by remember { mutableStateOf(prefs.getString("cyr_font", "Lora") ?: "Lora") }
    var arabicFont by remember { mutableStateOf(prefs.getString("ar_font", "Noto Naskh") ?: "Noto Naskh") } 
    var cyrillicSize by remember { mutableFloatStateOf(prefs.getFloat("cyr_size", 1.0f).coerceIn(0.6f, 1.5f)) }
    var arabicSize by remember { mutableFloatStateOf(prefs.getFloat("ar_size", 1.0f).coerceIn(0.6f, 2.0f)) }
    var isDebugMode by remember { mutableStateOf(prefs.getBoolean("debug_mode", true)) }

    val settingsState = SufaraSettingsState(cyrillicFont, arabicFont, cyrillicSize, arabicSize, isDebugMode)

    val actions = object : SufaraSettingsActions {
        override fun updateCyrillicFont(name: String) {
            cyrillicFont = name
            prefs.edit().putString("cyr_font", name).apply()
        }
        override fun updateArabicFont(name: String) {
            arabicFont = name
            prefs.edit().putString("ar_font", name).apply()
        }
        override fun updateCyrillicSize(size: Float) {
            cyrillicSize = size
            prefs.edit().putFloat("cyr_size", size).apply()
        }
        override fun updateArabicSize(size: Float) {
            arabicSize = size
            prefs.edit().putFloat("ar_size", size).apply()
        }
        override fun toggleDebugMode(enabled: Boolean) {
            isDebugMode = enabled
            prefs.edit().putBoolean("debug_mode", enabled).apply()
        }
    }

    CompositionLocalProvider(
        LocalSufaraSettings provides settingsState,
        LocalSufaraSettingsActions provides actions
    ) {
        content()
    }
}