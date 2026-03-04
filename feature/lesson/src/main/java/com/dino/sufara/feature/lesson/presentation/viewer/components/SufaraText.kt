package com.dino.sufara.feature.lesson.presentation.viewer.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.dino.sufara.core.designsystem.*
import com.dino.sufara.feature.lesson.domain.util.HarfHighlighter
import com.dino.sufara.feature.lesson.domain.util.HighlightType
import com.dino.sufara.feature.lesson.domain.util.asScript
import com.dino.sufara.feature.lesson.presentation.settings.BodyTextColorTheme
import com.dino.sufara.feature.lesson.presentation.settings.GlowColorTheme
import com.dino.sufara.feature.lesson.presentation.settings.LocalSufaraSettings
import com.dino.sufara.feature.lesson.presentation.settings.SufaraFonts
import com.dino.sufara.feature.lesson.presentation.viewer.components.renderers.RendererFactory

sealed class TextBlock {
    data class Paragraph(val text: String) : TextBlock()
    data class ExampleGroup(val examples: List<String>) : TextBlock()
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SufaraText(
    text: String,
    modifier: Modifier = Modifier,
    baseFontSize: TextUnit = 18.sp, 
    arabicFontSize: TextUnit = 32.sp,
    lineHeight: TextUnit = 28.sp,
    lessonId: String = "",
    symbol: String = ""
) {
    val settings = LocalSufaraSettings.current
    val cyrillicFont = SufaraFonts.getCyrillicFont(settings.cyrillicFont)
    
    // Развајамо фонтове: Један за примере, други фиксно Noto Naskh за текст
    val exampleArabicFont = SufaraFonts.getArabicFont(settings.arabicFont)
    val inlineArabicFont = SufaraFonts.getArabicFont("Noto Naskh")
    
    val scriptText = text.asScript()
    
    val finalBaseSize = baseFontSize * settings.cyrillicSizeMultiplier
    val finalArabicSize = arabicFontSize * settings.arabicSizeMultiplier
    
    // ИСПРАВКА: Нормалан проред и величина за арапски унутар обичног текста
    val paragraphLineHeight = lineHeight * settings.cyrillicSizeMultiplier
    val inlineArabicSize = finalBaseSize * 1.4f 

    val bodyColor = when(settings.bodyTextColorTheme) {
        BodyTextColorTheme.PARCHMENT -> TextParchment
        BodyTextColorTheme.SILVER -> TextSilver
        BodyTextColorTheme.MUTED_GOLD -> TextMutedGold
    }

    val glowColor = when(settings.glowColorTheme) {
        GlowColorTheme.GOLD -> GoldBase.copy(alpha = 0.5f)
        GlowColorTheme.AZURE -> Color(0xFF00BFFF).copy(alpha = 0.5f)
        GlowColorTheme.PURPLE -> Color(0xFF9D4EDD).copy(alpha = 0.5f)
        GlowColorTheme.NONE -> Color.Transparent
    }
    
    val textGlow = remember(glowColor) { Shadow(color = glowColor, blurRadius = 14f) }
    val redGlow = remember { Shadow(color = MoltenRed.copy(alpha = 0.6f), blurRadius = 16f) }

    val cyrillicLatinRegex = remember { Regex("[a-zA-Zа-яА-ЯђјљњћџЂЈЉЊЋЏčćžšđČĆŽŠĐ]+") }
    val arabicRegex = remember { Regex("[\\u0600-\\u06FF\\u0750-\\u077F\\u08A0-\\u08FF]+") }
    
    // ИСПРАВКА: Додат изузетак за Лам-Елиф
    val isHarf = (symbol.length == 1 && symbol != ".") || symbol == "لا"
    
    val globalType: HighlightType = remember(symbol) {
        if (isHarf) HighlightType.HARF else HighlightType.HARAKAH
    }

    val blocks = remember(scriptText) {
        val result = mutableListOf<TextBlock>()
        val currentGroup = mutableListOf<String>()
        val currentParagraphLines = mutableListOf<String>()

        fun flushParagraph() {
            if (currentParagraphLines.isNotEmpty()) {
                result.add(TextBlock.Paragraph(currentParagraphLines.joinToString("\n")))
                currentParagraphLines.clear()
            }
        }
        fun flushGroup() {
            if (currentGroup.isNotEmpty()) {
                result.add(TextBlock.ExampleGroup(currentGroup.toList()))
                currentGroup.clear()
            }
        }

        scriptText.lines().forEach { line ->
            val isStandaloneArabic = line.isNotBlank() && arabicRegex.containsMatchIn(line) && !cyrillicLatinRegex.containsMatchIn(line)
            if (isStandaloneArabic) {
                flushParagraph()
                currentGroup.add(line.trim())
            } else {
                flushGroup()
                currentParagraphLines.add(line)
            }
        }
        flushParagraph()
        flushGroup()
        result
    }

    val exampleRenderer = remember(settings.arabicFont) {
        RendererFactory.getRenderer(settings.arabicFont)
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        blocks.forEach { block ->
            when (block) {
                is TextBlock.Paragraph -> {
                    Text(
                        lineHeight = paragraphLineHeight, // ИСПРАВКА ПРОРЕДА
                        text = buildAnnotatedString {
                            val parts = block.text.split("**")
                            parts.forEachIndexed { index, part ->
                                withStyle(SpanStyle(fontFamily = cyrillicFont, color = bodyColor, fontSize = finalBaseSize, fontWeight = if (index % 2 != 0) FontWeight.Bold else FontWeight.Normal)) {
                                    var lastIndex = 0
                                    arabicRegex.findAll(part).forEach { matchResult ->
                                        append(part.substring(lastIndex, matchResult.range.first))
                                        val arabicWord = matchResult.value
                                        
                                        // ИСПРАВКА: Користимо inlineArabicFont и inlineArabicSize
                                        withStyle(SpanStyle(fontFamily = inlineArabicFont, fontSize = inlineArabicSize, color = bodyColor)) {
                                            append(arabicWord)
                                        }
                                        lastIndex = matchResult.range.last + 1
                                    }
                                    append(part.substring(lastIndex))
                                }
                            }
                        }
                    )
                }
                
                is TextBlock.ExampleGroup -> {
                    BoxWithConstraints(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 16.dp)) {
                        val maxWidthPx = with(androidx.compose.ui.platform.LocalDensity.current) { maxWidth.toPx() }
                        var scaleMultiplier by remember(block.examples) { mutableFloatStateOf(1f) }
                        
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                // ИСПРАВКА: SpaceEvenly прелепо распоређује елементе и гарантује празан простор између њих
                                horizontalArrangement = Arrangement.SpaceEvenly, 
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                block.examples.forEach { exampleText ->
                                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                        key(exampleText) {
                                            val action = HarfHighlighter.analyze(exampleText, lessonId, symbol)
                                            // ИСПРАВКА: Фонт је сада знатно мањи (1.2f уместо 2.2f)
                                            val currentExampleSize = finalArabicSize * 1.2f * scaleMultiplier 
                                            
                                            // Додат падинг да се визуелно никада не споје
                                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 4.dp)) {
                                                exampleRenderer.Render(
                                                    text = exampleText,
                                                    action = action,
                                                    globalType = globalType,
                                                    arabicFont = exampleArabicFont,
                                                    fontSize = currentExampleSize,
                                                    textGlow = textGlow,
                                                    redGlow = redGlow,
                                                    onTextLayout = { textLayoutResult ->
                                                        // Ако пукне у више редова или изађе ван оквира, смањујемо!
                                                        if ((textLayoutResult.hasVisualOverflow || textLayoutResult.didOverflowWidth || textLayoutResult.lineCount > 1) && scaleMultiplier > 0.3f) {
                                                            scaleMultiplier *= 0.9f
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}