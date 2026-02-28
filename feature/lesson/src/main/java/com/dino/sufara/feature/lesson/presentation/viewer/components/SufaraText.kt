package com.dino.sufara.feature.lesson.presentation.viewer.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dino.sufara.feature.lesson.domain.util.HarfHighlighter
import com.dino.sufara.feature.lesson.domain.util.HighlightAction
import com.dino.sufara.feature.lesson.domain.util.HighlightType
import com.dino.sufara.feature.lesson.domain.util.SufaraLogger
import com.dino.sufara.feature.lesson.presentation.settings.LocalSufaraSettings
import com.dino.sufara.feature.lesson.presentation.settings.SufaraFonts

sealed class TextBlock {
    data class Paragraph(val text: String) : TextBlock()
    data class ExampleGroup(val examples: List<String>) : TextBlock()
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SufaraText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    baseFontSize: TextUnit = 16.sp, 
    arabicFontSize: TextUnit = 32.sp,
    lineHeight: TextUnit = 28.sp,
    lessonId: String = "",
    symbol: String = ""
) {
    val settings = LocalSufaraSettings.current
    val cyrillicFont = SufaraFonts.getCyrillicFont(settings.cyrillicFont)
    val arabicFont = SufaraFonts.getArabicFont(settings.arabicFont)
    
    val finalBaseSize = baseFontSize * settings.cyrillicSizeMultiplier
    val finalArabicSize = arabicFontSize * settings.arabicSizeMultiplier
    val finalLineHeight = lineHeight * maxOf(settings.cyrillicSizeMultiplier, settings.arabicSizeMultiplier * 1.5f)

    val cyrillicLatinRegex = remember { Regex("[a-zA-Zа-яА-ЯђјљњћџЂЈЉЊЋЏ]") }
    val arabicRegex = remember { Regex("[\\u0600-\\u06FF\\u0750-\\u077F\\u08A0-\\u08FF]+") }
    val globalType = remember(lessonId) { HarfHighlighter.getLessonType(lessonId) }

    val blocks = remember(text) {
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

        text.lines().forEach { line ->
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

    var globalExampleCounter = 0 

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        blocks.forEach { block ->
            when (block) {
                is TextBlock.Paragraph -> {
                    val normalArabicColor = Color(0xFFFFD700) // Жута
                    val cCyrLayer1 = if (globalType == HighlightType.HARAKAH) Color.Transparent else color
                    val cCyrLayer2 = if (globalType == HighlightType.HARAKAH) color else Color.Transparent

                    Box {
                        // СЛОЈ 1: ПОЗАДИНА
                        Text(
                            lineHeight = finalLineHeight,
                            text = buildAnnotatedString {
                                val parts = block.text.split("**")
                                parts.forEachIndexed { index, part ->
                                    withStyle(SpanStyle(fontFamily = cyrillicFont, color = cCyrLayer1, fontSize = finalBaseSize, fontWeight = if (index % 2 != 0) FontWeight.Bold else FontWeight.Normal)) {
                                        var lastIndex = 0
                                        arabicRegex.findAll(part).forEach { matchResult ->
                                            append(part.substring(lastIndex, matchResult.range.first))
                                            val arabicWord = matchResult.value
                                            val action = HarfHighlighter.analyze(arabicWord, lessonId, symbol)
                                            
                                            withStyle(SpanStyle(fontFamily = arabicFont, fontSize = finalArabicSize)) {
                                                val wordStart = length
                                                append(arabicWord)
                                                for (i in arabicWord.indices) {
                                                    // HARAKAH: Црвена позадина. HARF: Жута позадина (све нормално).
                                                    val charColor = when (globalType) {
                                                        HighlightType.HARAKAH -> {
                                                            val isRed = action is HighlightAction.Harakah && (action.diacritics.contains(i) || action.supports.contains(i))
                                                            if (isRed) Color.Red else Color.Transparent
                                                        }
                                                        else -> normalArabicColor
                                                    }
                                                    addStyle(SpanStyle(color = charColor), wordStart + i, wordStart + i + 1)
                                                }
                                            }
                                            lastIndex = matchResult.range.last + 1
                                        }
                                        append(part.substring(lastIndex))
                                    }
                                }
                            }
                        )

                        // СЛОЈ 2: ПРЕДЊИ ПЛАН
                        Text(
                            lineHeight = finalLineHeight,
                            text = buildAnnotatedString {
                                val parts = block.text.split("**")
                                parts.forEachIndexed { index, part ->
                                    withStyle(SpanStyle(fontFamily = cyrillicFont, color = cCyrLayer2, fontSize = finalBaseSize, fontWeight = if (index % 2 != 0) FontWeight.Bold else FontWeight.Normal)) {
                                        var lastIndex = 0
                                        arabicRegex.findAll(part).forEach { matchResult ->
                                            append(part.substring(lastIndex, matchResult.range.first))
                                            val arabicWord = matchResult.value
                                            val action = HarfHighlighter.analyze(arabicWord, lessonId, symbol)
                                            
                                            withStyle(SpanStyle(fontFamily = arabicFont, fontSize = finalArabicSize)) {
                                                val wordStart = length
                                                append(arabicWord)
                                                for (i in arabicWord.indices) {
                                                    // HARAKAH: Жути текст, пробушена рупа за црвени харекет.
                                                    // HARF: Све провидно ОСИМ слова (харфа) које је црвено.
                                                    val charColor = when (globalType) {
                                                        HighlightType.HARAKAH -> {
                                                            val isHole = action is HighlightAction.Harakah && action.diacritics.contains(i)
                                                            if (isHole) Color.Transparent else normalArabicColor
                                                        }
                                                        else -> {
                                                            val isTargetHarf = action is HighlightAction.Harf && action.characters.contains(i)
                                                            if (isTargetHarf) Color.Red else Color.Transparent
                                                        }
                                                    }
                                                    addStyle(SpanStyle(color = charColor), wordStart + i, wordStart + i + 1)
                                                }
                                            }
                                            lastIndex = matchResult.range.last + 1
                                        }
                                        append(part.substring(lastIndex))
                                    }
                                }
                            }
                        )
                    }
                }
                
                is TextBlock.ExampleGroup -> {
                    val normalArabicColor = Color(0xFFE58F65) // Наранџаста

                    FlowRow(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        block.examples.forEach { exampleText ->
                            val currentIndex = globalExampleCounter++ 
                            val action = HarfHighlighter.analyze(exampleText, lessonId, symbol)
                            
                            Box {
                                // СЛОЈ 1: ПОЗАДИНА
                                Text(
                                    fontFamily = arabicFont,
                                    fontSize = finalArabicSize * 1.2f,
                                    text = buildAnnotatedString {
                                        val wordStart = length
                                        append(exampleText)
                                        for (i in exampleText.indices) {
                                            val charColor = when (globalType) {
                                                HighlightType.HARAKAH -> {
                                                    val isRed = action is HighlightAction.Harakah && (action.diacritics.contains(i) || action.supports.contains(i))
                                                    if (isRed) Color.Red else Color.Transparent
                                                }
                                                else -> normalArabicColor
                                            }
                                            addStyle(SpanStyle(color = charColor), wordStart + i, wordStart + i + 1)
                                        }
                                    }
                                )
                                // СЛОЈ 2: ПРЕДЊИ ПЛАН
                                Text(
                                    fontFamily = arabicFont,
                                    fontSize = finalArabicSize * 1.2f,
                                    text = buildAnnotatedString {
                                        val wordStart = length
                                        append(exampleText)
                                        for (i in exampleText.indices) {
                                            val charColor = when (globalType) {
                                                HighlightType.HARAKAH -> {
                                                    val isHole = action is HighlightAction.Harakah && action.diacritics.contains(i)
                                                    if (isHole) Color.Transparent else normalArabicColor
                                                }
                                                else -> {
                                                    val isTargetHarf = action is HighlightAction.Harf && action.characters.contains(i)
                                                    if (isTargetHarf) Color.Red else Color.Transparent
                                                }
                                            }
                                            addStyle(SpanStyle(color = charColor), wordStart + i, wordStart + i + 1)
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