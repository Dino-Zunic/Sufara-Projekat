package com.dino.sufara.feature.lesson.presentation.viewer.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dino.sufara.core.designsystem.GoldBase
import com.dino.sufara.core.designsystem.IpaFontFamily
import com.dino.sufara.core.designsystem.MoltenRed
import com.dino.sufara.core.designsystem.TextSilver
import com.dino.sufara.feature.lesson.domain.model.LessonStep
import com.dino.sufara.feature.lesson.domain.util.*
import com.dino.sufara.feature.lesson.presentation.settings.GlowColorTheme
import com.dino.sufara.feature.lesson.presentation.settings.LocalSufaraSettings
import com.dino.sufara.feature.lesson.presentation.settings.SufaraFonts
import com.dino.sufara.feature.lesson.presentation.viewer.components.renderers.RendererFactory
import kotlinx.coroutines.launch

@Composable
fun ExampleStepScreen(step: LessonStep.Example, lessonId: String, symbol: String) {
    val haptic = LocalHapticFeedback.current
    val settings = LocalSufaraSettings.current
    val arabicFont = SufaraFonts.getArabicFont(settings.arabicFont)
    val exampleRenderer = remember(settings.arabicFont) { RendererFactory.getRenderer(settings.arabicFont) }
    val interactionSource = remember { MutableInteractionSource() }
    val scope = rememberCoroutineScope()
    
    var isPlaying by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(animation = tween(300, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse), label = "Scale"
    )

    val SpeakerIcon = remember {
        ImageVector.Builder(name = "Speaker", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
            path(fill = SolidColor(Color.Black)) { moveTo(3f, 9f); lineTo(7f, 9f); lineTo(12f, 4f); lineTo(12f, 20f); lineTo(7f, 15f); lineTo(3f, 15f); close() }
            path(fill = SolidColor(Color.Transparent), stroke = SolidColor(Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round) { moveTo(15.5f, 9.5f); quadTo(17f, 12f, 15.5f, 14.5f) }
            path(fill = SolidColor(Color.Transparent), stroke = SolidColor(Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round) { moveTo(18.5f, 6.5f); quadTo(21.5f, 12f, 18.5f, 17.5f) }
        }.build()
    }

    val glowColor = when(settings.glowColorTheme) {
        GlowColorTheme.GOLD -> GoldBase.copy(alpha = 0.5f)
        GlowColorTheme.AZURE -> Color(0xFF00BFFF).copy(alpha = 0.5f)
        GlowColorTheme.PURPLE -> Color(0xFF9D4EDD).copy(alpha = 0.5f)
        GlowColorTheme.NONE -> Color.Transparent
    }
    val textGlow = remember(glowColor) { androidx.compose.ui.graphics.Shadow(color = glowColor, blurRadius = 14f) }
    val redGlow = remember { androidx.compose.ui.graphics.Shadow(color = MoltenRed.copy(alpha = 0.6f), blurRadius = 16f) }
    val isHarf = (symbol.length == 1 && symbol != ".") || symbol == "لا"
    val globalType = remember(symbol) { if (isHarf) HighlightType.HARF else HighlightType.HARAKAH }

    val action = remember(step.text, lessonId, symbol) { HarfHighlighter.analyze(step.text, lessonId, symbol) }
    val ipaTranscription = remember(step.text) { ArabicIpaTranscriber.transcribe(step.text) }
    var currentFontSize by remember(step.text) { mutableStateOf(150.sp) }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Card(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).clickable(interactionSource = interactionSource, indication = null) { 
                if (!isPlaying) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    scope.launch { SoundSimulator.playMockSound(onStart = { isPlaying = true }, onEnd = { isPlaying = false }) }
                }
            },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Box(modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 350.dp)) {
                Icon(
                    imageVector = SpeakerIcon, contentDescription = null,
                    tint = if (isPlaying) GoldBase else TextSilver.copy(alpha = 0.4f),
                    modifier = Modifier.align(Alignment.TopEnd).padding(24.dp).size(28.dp).graphicsLayer { if (isPlaying) { scaleX = pulseScale; scaleY = pulseScale } }
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize().padding(start = 24.dp, end = 24.dp, top = 64.dp, bottom = 24.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().wrapContentHeight(unbounded = true), contentAlignment = Alignment.Center) {
                        exampleRenderer.Render(
                            text = step.text, action = action, globalType = globalType, arabicFont = arabicFont, fontSize = currentFontSize, textGlow = textGlow, redGlow = redGlow,
                            onTextLayout = { textLayoutResult ->
                                if (textLayoutResult.hasVisualOverflow || textLayoutResult.didOverflowWidth || textLayoutResult.lineCount > 1) {
                                    if (currentFontSize.value > 30f) currentFontSize *= 0.9f
                                }
                            }
                        )
                    }

                    if (settings.showSeparatedLetters) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = TextSilver.copy(alpha = 0.2f), modifier = Modifier.fillMaxWidth(0.5f))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = ArabicWordSplitter.splitWord(step.text), fontFamily = arabicFont, fontSize = 42.sp, color = TextSilver.copy(alpha = 0.8f), textAlign = TextAlign.Center)
                    }

                    if (settings.showIpa && ipaTranscription.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "[ $ipaTranscription ]", color = TextSilver.copy(alpha = 0.5f), fontFamily = IpaFontFamily, fontSize = 24.sp, fontWeight = FontWeight.Light, letterSpacing = 2.sp)
                    }
                }
            }
        }
    }
}