package com.dino.sufara.feature.lesson.presentation.settings

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.dino.sufara.feature.lesson.presentation.viewer.animations.CardAnimationType

enum class BodyTextColorTheme { PARCHMENT, SILVER, MUTED_GOLD }
enum class GlowColorTheme { GOLD, AZURE, PURPLE, NONE }

data class SufaraSettingsState(
    val cyrillicFont: String = "Lora",
    val arabicFont: String = "KFGQPC",
    val cyrillicSizeMultiplier: Float = 1.0f,
    val arabicSizeMultiplier: Float = 1.8f, 
    val bodyTextColorTheme: BodyTextColorTheme = BodyTextColorTheme.SILVER, 
    val glowColorTheme: GlowColorTheme = GlowColorTheme.GOLD, 
    val cardAnimation: CardAnimationType = CardAnimationType.TILT_SLIDE,
    val isCyrillic: Boolean = false,
    val showIpa: Boolean = true,
    val showSeparatedLetters: Boolean = true
)

interface SufaraSettingsActions {
    fun updateCyrillicFont(name: String)
    fun updateArabicFont(name: String)
    fun updateCyrillicSize(size: Float)
    fun updateScript(isCyrillic: Boolean)
    fun toggleIpa(enabled: Boolean)
    fun toggleSeparatedLetters(enabled: Boolean)
}

val LocalSufaraSettings = compositionLocalOf { SufaraSettingsState() }
val LocalSufaraSettingsActions = compositionLocalOf<SufaraSettingsActions> { error("Nema akcija") }

@Composable
fun SufaraSettingsProvider(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("sufara_prefs", Context.MODE_PRIVATE) }

    var cyrillicFont by remember { mutableStateOf(prefs.getString("cyr_font", "Lora") ?: "Lora") }
    var arabicFont by remember { mutableStateOf(prefs.getString("ar_font", "KFGQPC") ?: "KFGQPC") } 
    var cyrillicSize by remember { mutableFloatStateOf(prefs.getFloat("cyr_size", 1.0f).coerceIn(0.6f, 1.5f)) }
    var showIpa by remember { mutableStateOf(prefs.getBoolean("show_ipa", true)) }
    var showSeparatedLetters by remember { mutableStateOf(prefs.getBoolean("show_separated", true)) }
    var isCyrillic by remember { mutableStateOf(prefs.getBoolean("is_cyrillic", false)) }

    val settingsState = SufaraSettingsState(
        cyrillicFont = cyrillicFont, 
        arabicFont = arabicFont, 
        cyrillicSizeMultiplier = cyrillicSize, 
        isCyrillic = isCyrillic, 
        showIpa = showIpa, 
        showSeparatedLetters = showSeparatedLetters
    )

    val actions = object : SufaraSettingsActions {
        override fun updateCyrillicFont(name: String) { cyrillicFont = name; prefs.edit().putString("cyr_font", name).apply() }
        override fun updateArabicFont(name: String) { arabicFont = name; prefs.edit().putString("ar_font", name).apply() }
        override fun updateCyrillicSize(size: Float) { cyrillicSize = size; prefs.edit().putFloat("cyr_size", size).apply() }
        override fun updateScript(cyrillic: Boolean) { isCyrillic = cyrillic; prefs.edit().putBoolean("is_cyrillic", cyrillic).apply() }
        override fun toggleIpa(enabled: Boolean) { showIpa = enabled; prefs.edit().putBoolean("show_ipa", enabled).apply() }
        override fun toggleSeparatedLetters(enabled: Boolean) { showSeparatedLetters = enabled; prefs.edit().putBoolean("show_separated", enabled).apply() }
    }

    CompositionLocalProvider(
        LocalSufaraSettings provides settingsState,
        LocalSufaraSettingsActions provides actions
    ) {
        content()
    }
}