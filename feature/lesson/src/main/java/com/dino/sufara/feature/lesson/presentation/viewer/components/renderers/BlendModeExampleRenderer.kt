package com.dino.sufara.feature.lesson.presentation.viewer.components.renderers

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import com.dino.sufara.core.designsystem.GoldBase
import com.dino.sufara.core.designsystem.MoltenRed
import com.dino.sufara.feature.lesson.domain.util.HighlightAction
import com.dino.sufara.feature.lesson.domain.util.HighlightType

class BlendModeExampleRenderer : ArabicExampleRenderer {
    
    @Composable
    override fun Render(
        text: String,
        action: HighlightAction,
        globalType: HighlightType,
        arabicFont: FontFamily,
        fontSize: TextUnit,
        textGlow: Shadow,
        redGlow: Shadow,
        onTextLayout: (TextLayoutResult) -> Unit
    ) {
        var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

        val redIndices = remember(action, globalType) {
            val indices = mutableSetOf<Int>()
            when {
                globalType == HighlightType.HARAKAH && action is HighlightAction.Harakah -> {
                    // Узимамо само харекете, без основних слова (supports)
                    indices.addAll(action.diacritics)
                }
                globalType == HighlightType.HARF && action is HighlightAction.Harf -> {
                    indices.addAll(action.characters)
                    indices.addAll(action.attachedDiacritics)
                }
            }
            indices
        }

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            
            // СЛОЈ 1: Златна база (Amiri ради савршен Shaping овде)
            Text(
                text = text,
                fontFamily = arabicFont,
                fontSize = fontSize,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                style = TextStyle(color = GoldBase, shadow = textGlow),
                onTextLayout = { 
                    layoutResult = it
                    onTextLayout(it) 
                }
            )

            // СЛОЈ 2: Интелигентна геометријска маска
            if (layoutResult != null && redIndices.isNotEmpty()) {
                val fontSizePx = fontSize.value * 2.5f // Оквирна конверзија у пикселе за математику

                Text(
                    text = text,
                    fontFamily = arabicFont,
                    fontSize = fontSize,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawWithContent {
                            val maskPath = Path()
                            
                            redIndices.forEach { index ->
                                if (index in text.indices) {
                                    val rect = layoutResult!!.getBoundingBox(index)
                                    val char = text[index]

                                    if (isHarakah(char)) {
                                        // 1. ХОРИЗОНТАЛНО СУЖАВАЊЕ И ОБЛИК ПИЛУЛЕ (Елиминише хватање ћошкова суседних слова)
                                        val width = rect.right - rect.left
                                        val shrinkX = width * 0.20f // Сечемо 20% са леве и десне стране
                                        
                                        var left = rect.left + shrinkX
                                        var right = rect.right - shrinkX
                                        var top = rect.top
                                        var bottom = rect.bottom

                                        // 2. ВЕРТИКАЛНО ОДСЕЦАЊЕ НА ОСНОВУ ОСНОВНОГ СЛОВА (Елиминише хватање базе)
                                        val baseIndex = findBaseCharacterIndex(text, index)
                                        if (baseIndex != -1) {
                                            val baseRect = layoutResult!!.getBoundingBox(baseIndex)
                                            
                                            if (isTopHarakah(char)) {
                                                // Харекет је горе. Сечемо све што се спушта у основно слово.
                                                // Остављамо мали 'тампон' (0.15f) да не бисмо одсекли дно харекета
                                                bottom = minOf(bottom, baseRect.top + (fontSizePx * 0.15f))
                                            } else if (isBottomHarakah(char)) {
                                                // Харекет је доле. Сечемо све што се пење у основно слово.
                                                top = maxOf(top, baseRect.bottom - (fontSizePx * 0.15f))
                                            }
                                        }

                                        // Провера да нисмо сузили превише због неке чудне лигатуре
                                        if (right > left && bottom > top) {
                                            maskPath.addRoundRect(
                                                RoundRect(
                                                    left = left, top = top, right = right, bottom = bottom,
                                                    cornerRadius = CornerRadius(width, width) // Прави савршен овал/пилулу!
                                                )
                                            )
                                        }
                                    } else {
                                        // За основна слова (Harf), остављамо пун Bounding Box, 
                                        // али мало заоблимо ћошкове за сваки случај.
                                        maskPath.addRoundRect(
                                            RoundRect(rect, CornerRadius(fontSizePx * 0.1f, fontSizePx * 0.1f))
                                        )
                                    }
                                }
                            }
                            
                            // Аплицирамо нашу хирушки прецизну маску и цртамо црвени текст!
                            clipPath(maskPath) {
                                this@drawWithContent.drawContent()
                            }
                        },
                    style = TextStyle(color = MoltenRed, shadow = redGlow)
                )
            }
        }
    }

    // --- ПРИВАТНЕ ХЕУРИСТИЧКЕ ФУНКЦИЈЕ ---

    private fun isHarakah(char: Char): Boolean {
        return isTopHarakah(char) || isBottomHarakah(char)
    }

    private fun isTopHarakah(char: Char): Boolean {
        val topMarks = setOf('َ', 'ُ', 'ً', 'ٌ', 'ّ', 'ْ', 'ٰ', '\u0670', 'أ', 'ؤ') 
        // Напомена: 'أ' (Хемзе изнад елифа) се технички сматра словом, али за потребе позиционирања
        // његов BoundingBox често лебди горе, па га третирамо као горњи маркер.
        return char in topMarks
    }

    private fun isBottomHarakah(char: Char): Boolean {
        val bottomMarks = setOf('ِ', 'ٍ', 'إ') 
        return char in bottomMarks
    }

    /**
     * Иде уназад кроз стринг од позиције харекета док не нађе прво слово 
     * које НИЈЕ харекет. То нам даје тачне координате слова испод!
     */
    private fun findBaseCharacterIndex(text: String, harakahIndex: Int): Int {
        var i = harakahIndex - 1
        while (i >= 0) {
            if (!isHarakah(text[i]) && text[i] != ' ') {
                return i
            }
            i--
        }
        return -1
    }
}