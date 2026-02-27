package com.dino.sufara.feature.lesson.presentation.viewer.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
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
import com.dino.sufara.feature.lesson.presentation.settings.LocalSufaraSettings
import com.dino.sufara.feature.lesson.presentation.settings.SufaraFonts

// 1. Структура за одвајање пасуса од груписаних примера
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
    baseFontSize: TextUnit = 16.sp, // СМАЊЕН почетни фонт ћирилице
    arabicFontSize: TextUnit = 32.sp,
    lineHeight: TextUnit = 28.sp // Прилагођен проред
) {
    val settings = LocalSufaraSettings.current
    
    val cyrillicFont = SufaraFonts.getCyrillicFont(settings.cyrillicFont)
    val arabicFont = SufaraFonts.getArabicFont(settings.arabicFont)
    
    val finalBaseSize = baseFontSize * settings.cyrillicSizeMultiplier
    val finalArabicSize = arabicFontSize * settings.arabicSizeMultiplier
    val finalLineHeight = lineHeight * maxOf(settings.cyrillicSizeMultiplier, settings.arabicSizeMultiplier)

    // Regex детекција:
    // cyrillicLatinRegex тражи БИЛО КОЈЕ слово српског/енглеског алфабета
    val cyrillicLatinRegex = remember { Regex("[a-zA-Zа-яА-ЯђјљњћџЂЈЉЊЋЏ]") }
    // arabicRegex тражи арапска слова (ово обухвата и ташдид, сукун, итд.)
    val arabicRegex = remember { Regex("[\\u0600-\\u06FF\\u0750-\\u077F\\u08A0-\\u08FF]+") }

    // 2. Парсирање сировог текста у блокове
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
            // ЛОГИКА: Линија је засебан пример ако ИМА арапски, а НЕМА ћирилицу/латиницу
            // (Ово значи да ће цртице, зарези и бројеви у тој линији бити уредно прихваћени)
            val isStandaloneArabic = line.isNotBlank() && 
                                     arabicRegex.containsMatchIn(line) && 
                                     !cyrillicLatinRegex.containsMatchIn(line)

            if (isStandaloneArabic) {
                flushParagraph() // Затвори обичан текст ако смо га читали
                currentGroup.add(line.trim())
            } else {
                flushGroup() // Затвори групу примера ако смо их сакупљали
                currentParagraphLines.add(line)
            }
        }
        flushParagraph()
        flushGroup()
        result
    }

    var globalExampleCounter = 0 // Памти тачан редни број примера у целом фајлу

    // 3. Искртавање блокова на екран
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        blocks.forEach { block ->
            when (block) {
                is TextBlock.Paragraph -> {
                    // А. ОБИЧАН ТЕКСТ СА INLINE АРАПСКИМ (Жута боја)
                    val annotatedString = buildAnnotatedString {
                        val parts = block.text.split("**")
                        parts.forEachIndexed { index, part ->
                            val isBold = index % 2 != 0
                            val baseStyle = SpanStyle(
                                fontFamily = cyrillicFont,
                                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                                color = color,
                                fontSize = finalBaseSize
                            )

                            withStyle(baseStyle) {
                                var lastIndex = 0
                                arabicRegex.findAll(part).forEach { matchResult ->
                                    append(part.substring(lastIndex, matchResult.range.first))
                                    
                                    withStyle(SpanStyle(
                                        fontFamily = arabicFont,
                                        fontSize = finalArabicSize, 
                                        fontWeight = FontWeight.Normal, 
                                        color = Color(0xFFFFD700) // ЖУТА (Hack)
                                    )) {
                                        append(matchResult.value)
                                    }
                                    lastIndex = matchResult.range.last + 1
                                }
                                append(part.substring(lastIndex))
                            }
                        }
                    }
                    Text(text = annotatedString, lineHeight = finalLineHeight)
                }
                
                is TextBlock.ExampleGroup -> {
                    // Б. ИЗДВОЈЕНИ ПРИМЕРИ У НИЗУ (Наранџаста боја)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        block.examples.forEach { exampleText ->
                            val currentIndex = globalExampleCounter++ // ОВДЕ ЈЕ САЧУВАН ЊЕГОВ РЕДНИ БРОЈ ЗА ЗВУК!
                            
                            Text(
                                text = exampleText,
                                fontFamily = arabicFont,
                                fontSize = finalArabicSize * 1.2f, // Мало већи од inline текста
                                color = Color(0xFFE58F65) // НАРАНЏАСТА (Hack)
                            )
                        }
                    }
                }
            }
        }
    }
}