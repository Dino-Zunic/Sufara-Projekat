package com.dino.sufara.feature.lesson.presentation.settings

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import com.dino.sufara.core.designsystem.BackgroundLightMode
import com.dino.sufara.core.designsystem.BackgroundLightStrength
import com.dino.sufara.core.designsystem.BackgroundPatternStyle
import com.dino.sufara.core.designsystem.components.WireMotionStyle
import com.dino.sufara.feature.lesson.presentation.viewer.animations.CardAnimationType

enum class BodyTextColorTheme { PARCHMENT, SILVER, MUTED_GOLD }
enum class GlowColorTheme { GOLD, AZURE, PURPLE, NONE }
enum class LessonMapBiomeStyle { NAVY_BLUE, OCEAN, NIGHT_GARDEN }
enum class LessonMapResumeMode { LAST_POSITION, CURRENT_LESSON }
enum class SuccessBurstStyle(val count: Int) { OFF(0), SUBTLE(20), LIVELY(30) }
enum class MakhrajDiagramStyle { COMPACT, FEATURED, HIDDEN }
enum class MakhrajImageSource { CURATED, LESSON, BOTH }
enum class WritingStrictness(val passingScore: Int) {
    RELAXED(58), BALANCED(68), STRICT(78)
}

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
    val showSeparatedLetters: Boolean = true,
    val backgroundPatternStyle: BackgroundPatternStyle = BackgroundPatternStyle.RIDGE,
    val backgroundLightMode: BackgroundLightMode = BackgroundLightMode.SOFT_BLUE,
    val backgroundLightStrength: BackgroundLightStrength = BackgroundLightStrength.MEDIUM,
    val backgroundPatternVisibility: Float = 0.11f,
    val backgroundOrbitEnabled: Boolean = false,
    val backgroundBlobEnabled: Boolean = true,
    val backgroundParticlesEnabled: Boolean = true,
    val backgroundParticleVisibility: Float = 1.5f,
    val wireMotionStyle: WireMotionStyle = WireMotionStyle.ORGANIC,
    val lessonMapBiomeStyle: LessonMapBiomeStyle = LessonMapBiomeStyle.NAVY_BLUE,
    val lessonMapResumeMode: LessonMapResumeMode = LessonMapResumeMode.LAST_POSITION,
    val successBurstStyle: SuccessBurstStyle = SuccessBurstStyle.LIVELY,
    val makhrajDiagramStyle: MakhrajDiagramStyle = MakhrajDiagramStyle.COMPACT,
    val makhrajImageSource: MakhrajImageSource = MakhrajImageSource.BOTH,
    val writingStrictness: WritingStrictness = WritingStrictness.BALANCED
)

interface SufaraSettingsActions {
    fun updateCyrillicFont(name: String)
    fun updateArabicFont(name: String)
    fun updateCyrillicSize(size: Float)
    fun updateScript(isCyrillic: Boolean)
    fun toggleIpa(enabled: Boolean)
    fun toggleSeparatedLetters(enabled: Boolean)
    fun updateBackgroundPatternStyle(style: BackgroundPatternStyle)
    fun updateBackgroundLightMode(mode: BackgroundLightMode)
    fun updateBackgroundLightStrength(strength: BackgroundLightStrength)
    fun updateBackgroundPatternVisibility(visibility: Float)
    fun toggleBackgroundOrbit(enabled: Boolean)
    fun toggleBackgroundBlob(enabled: Boolean)
    fun toggleBackgroundParticles(enabled: Boolean)
    fun updateBackgroundParticleVisibility(visibility: Float)
    fun updateWireMotionStyle(style: WireMotionStyle)
    fun updateLessonMapBiomeStyle(style: LessonMapBiomeStyle)
    fun updateLessonMapResumeMode(mode: LessonMapResumeMode)
    fun updateSuccessBurstStyle(style: SuccessBurstStyle)
    fun updateMakhrajDiagramStyle(style: MakhrajDiagramStyle)
    fun updateMakhrajImageSource(source: MakhrajImageSource)
    fun updateWritingStrictness(strictness: WritingStrictness)
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
    var useCyrillic by remember { mutableStateOf(prefs.getBoolean("is_cyrillic", false)) }
    var patternStyle by remember {
        mutableStateOf(prefs.enumValue("bg_pattern_style", BackgroundPatternStyle.RIDGE))
    }
    var lightMode by remember {
        mutableStateOf(prefs.enumValue("bg_light_mode", BackgroundLightMode.SOFT_BLUE))
    }
    var lightStrength by remember {
        mutableStateOf(prefs.enumValue("bg_light_strength", BackgroundLightStrength.MEDIUM))
    }
    var patternVisibility by remember {
        mutableFloatStateOf(prefs.getFloat("bg_pattern_visibility", 0.11f).coerceIn(0.04f, 0.22f))
    }
    var orbitEnabled by remember { mutableStateOf(prefs.getBoolean("bg_orbit", false)) }
    var blobEnabled by remember { mutableStateOf(prefs.getBoolean("bg_blob", true)) }
    var particlesEnabled by remember { mutableStateOf(prefs.getBoolean("bg_particles", true)) }
    var particleVisibility by remember {
        mutableFloatStateOf(prefs.getFloat("bg_particle_visibility", 1.5f).coerceIn(0.5f, 2.2f))
    }
    var wireMotionStyle by remember {
        mutableStateOf(prefs.enumValue("wire_motion_style", WireMotionStyle.ORGANIC))
    }
    var lessonMapBiomeStyle by remember {
        mutableStateOf(prefs.enumValue("lesson_map_biome_style", LessonMapBiomeStyle.NAVY_BLUE))
    }
    var lessonMapResumeMode by remember {
        mutableStateOf(prefs.enumValue("lesson_map_resume_mode", LessonMapResumeMode.LAST_POSITION))
    }
    var successBurstStyle by remember {
        mutableStateOf(prefs.enumValue("success_burst_style", SuccessBurstStyle.LIVELY))
    }
    var makhrajDiagramStyle by remember {
        mutableStateOf(prefs.enumValue("makhraj_diagram_style", MakhrajDiagramStyle.COMPACT))
    }
    var makhrajImageSource by remember {
        mutableStateOf(prefs.enumValue("makhraj_image_source", MakhrajImageSource.BOTH))
    }
    var writingStrictness by remember {
        mutableStateOf(prefs.enumValue("writing_strictness", WritingStrictness.BALANCED))
    }

    val settingsState = SufaraSettingsState(
        cyrillicFont = cyrillicFont,
        arabicFont = arabicFont,
        cyrillicSizeMultiplier = cyrillicSize,
        isCyrillic = useCyrillic,
        showIpa = showIpa,
        showSeparatedLetters = showSeparatedLetters,
        backgroundPatternStyle = patternStyle,
        backgroundLightMode = lightMode,
        backgroundLightStrength = lightStrength,
        backgroundPatternVisibility = patternVisibility,
        backgroundOrbitEnabled = orbitEnabled,
        backgroundBlobEnabled = blobEnabled,
        backgroundParticlesEnabled = particlesEnabled,
        backgroundParticleVisibility = particleVisibility,
        wireMotionStyle = wireMotionStyle,
        lessonMapBiomeStyle = lessonMapBiomeStyle,
        lessonMapResumeMode = lessonMapResumeMode,
        successBurstStyle = successBurstStyle,
        makhrajDiagramStyle = makhrajDiagramStyle,
        makhrajImageSource = makhrajImageSource,
        writingStrictness = writingStrictness
    )

    val actions = remember(prefs) {
        object : SufaraSettingsActions {
            override fun updateCyrillicFont(name: String) { cyrillicFont = name; prefs.edit { putString("cyr_font", name) } }
            override fun updateArabicFont(name: String) { arabicFont = name; prefs.edit { putString("ar_font", name) } }
            override fun updateCyrillicSize(size: Float) { cyrillicSize = size; prefs.edit { putFloat("cyr_size", size) } }
            override fun updateScript(isCyrillic: Boolean) { useCyrillic = isCyrillic; prefs.edit { putBoolean("is_cyrillic", isCyrillic) } }
            override fun toggleIpa(enabled: Boolean) { showIpa = enabled; prefs.edit { putBoolean("show_ipa", enabled) } }
            override fun toggleSeparatedLetters(enabled: Boolean) { showSeparatedLetters = enabled; prefs.edit { putBoolean("show_separated", enabled) } }
            override fun updateBackgroundPatternStyle(style: BackgroundPatternStyle) { patternStyle = style; prefs.edit { putString("bg_pattern_style", style.name) } }
            override fun updateBackgroundLightMode(mode: BackgroundLightMode) { lightMode = mode; prefs.edit { putString("bg_light_mode", mode.name) } }
            override fun updateBackgroundLightStrength(strength: BackgroundLightStrength) { lightStrength = strength; prefs.edit { putString("bg_light_strength", strength.name) } }
            override fun updateBackgroundPatternVisibility(visibility: Float) {
                patternVisibility = visibility.coerceIn(0.04f, 0.22f)
                prefs.edit { putFloat("bg_pattern_visibility", patternVisibility) }
            }
            override fun toggleBackgroundOrbit(enabled: Boolean) { orbitEnabled = enabled; prefs.edit { putBoolean("bg_orbit", enabled) } }
            override fun toggleBackgroundBlob(enabled: Boolean) { blobEnabled = enabled; prefs.edit { putBoolean("bg_blob", enabled) } }
            override fun toggleBackgroundParticles(enabled: Boolean) { particlesEnabled = enabled; prefs.edit { putBoolean("bg_particles", enabled) } }
            override fun updateBackgroundParticleVisibility(visibility: Float) {
                particleVisibility = visibility.coerceIn(0.5f, 2.2f)
                prefs.edit { putFloat("bg_particle_visibility", particleVisibility) }
            }
            override fun updateWireMotionStyle(style: WireMotionStyle) { wireMotionStyle = style; prefs.edit { putString("wire_motion_style", style.name) } }
            override fun updateLessonMapBiomeStyle(style: LessonMapBiomeStyle) { lessonMapBiomeStyle = style; prefs.edit { putString("lesson_map_biome_style", style.name) } }
            override fun updateLessonMapResumeMode(mode: LessonMapResumeMode) { lessonMapResumeMode = mode; prefs.edit { putString("lesson_map_resume_mode", mode.name) } }
            override fun updateSuccessBurstStyle(style: SuccessBurstStyle) { successBurstStyle = style; prefs.edit { putString("success_burst_style", style.name) } }
            override fun updateMakhrajDiagramStyle(style: MakhrajDiagramStyle) { makhrajDiagramStyle = style; prefs.edit { putString("makhraj_diagram_style", style.name) } }
            override fun updateMakhrajImageSource(source: MakhrajImageSource) { makhrajImageSource = source; prefs.edit { putString("makhraj_image_source", source.name) } }
            override fun updateWritingStrictness(strictness: WritingStrictness) { writingStrictness = strictness; prefs.edit { putString("writing_strictness", strictness.name) } }
        }
    }

    CompositionLocalProvider(
        LocalSufaraSettings provides settingsState,
        LocalSufaraSettingsActions provides actions,
        content = content
    )
}

private inline fun <reified T : Enum<T>> android.content.SharedPreferences.enumValue(
    key: String,
    default: T
): T = runCatching { enumValueOf<T>(getString(key, default.name) ?: default.name) }.getOrDefault(default)
