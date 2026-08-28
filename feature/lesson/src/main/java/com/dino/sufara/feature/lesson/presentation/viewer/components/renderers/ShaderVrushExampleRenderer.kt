package com.dino.sufara.feature.lesson.presentation.viewer.components.renderers

import android.graphics.BitmapShader
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import androidx.core.graphics.createBitmap
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import com.dino.sufara.core.designsystem.GoldBase
import com.dino.sufara.core.designsystem.MoltenRed
import com.dino.sufara.feature.lesson.domain.util.HighlightAction
import com.dino.sufara.feature.lesson.domain.util.HighlightType

class ShaderBrushExampleRenderer : ArabicExampleRenderer {
    
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

        // Издвајамо индексе за бојење
        val redIndices = remember(action, globalType) {
            val indices = mutableSetOf<Int>()
            when {
                globalType == HighlightType.HARAKAH && action is HighlightAction.Harakah -> {
                    indices.addAll(action.diacritics)
                }
                globalType == HighlightType.HARF && action is HighlightAction.Harf -> {
                    indices.addAll(action.characters)
                    indices.addAll(action.attachedDiacritics)
                }
            }
            indices
        }

        // Креирамо паметни Brush који се динамички генерише на основу координата текста
        val shaderBrush = remember(layoutResult, redIndices) {
            if (layoutResult == null || redIndices.isEmpty()) {
                androidx.compose.ui.graphics.SolidColor(GoldBase)
            } else {
                createArabicHighlightBrush(text, layoutResult!!, redIndices, GoldBase, MoltenRed)
            }
        }

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            // КРУЦИЈАЛНО: Прослеђујемо само један, неисецкан Text елемент. 
            // HarfBuzz га обликује перфектно јер нема SpanStyle(Color) прекида!
            Text(
                text = text,
                fontFamily = arabicFont,
                fontSize = fontSize,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                style = TextStyle(
                    brush = shaderBrush, // GPU наноси боје тек након што се слове перфектно позиционирају
                    shadow = textGlow
                ),
                onTextLayout = { 
                    layoutResult = it
                    onTextLayout(it) 
                }
            )
        }
    }

    // --- ЛОГИКА ЗА ПРАВЉЕЊЕ ТЕКСТУРЕ (АЛГОРИТАМ 2 ИЗ ИЗВЕШТАЈА) ---
    private fun createArabicHighlightBrush(
        text: String,
        layoutResult: TextLayoutResult,
        highlightIndices: Set<Int>,
        baseColor: Color,
        highlightColor: Color
    ): Brush {
        return object : ShaderBrush() {
            override fun createShader(size: Size): Shader {
                // Осигуравамо да димензије нису нула да спречимо crash
                val width = size.width.toInt().coerceAtLeast(1)
                val height = size.height.toInt().coerceAtLeast(1)

                // 1. Правимо радну меморију (Bitmap) исте величине као цео текст
                val bitmap = createBitmap(width, height)
                val canvas = android.graphics.Canvas(bitmap)
                val paint = Paint().apply { isAntiAlias = true }

                // 2. Фарбамо цео Bitmap у златну боју (ово ће бити основа свих слова)
                paint.color = baseColor.toArgb()
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

                // 3. Фарбамо црвене "флеке" тачно на местима где су харекети
                paint.color = highlightColor.toArgb()

                highlightIndices.forEach { index ->
                    if (index in text.indices) {
                        val composeRect = layoutResult.getBoundingBox(index)
                        val char = text[index]

                        val lineIndex = layoutResult.getLineForOffset(index)
                        val lineTop = layoutResult.getLineTop(lineIndex)
                        val lineBottom = layoutResult.getLineBottom(lineIndex)
                        val lineHeight = lineBottom - lineTop

                        var top = composeRect.top
                        var bottom = composeRect.bottom

                        // Хеуристика за хоризонтално сечење да не бисмо захватили слово испод/изнад
                        val topDiacritics = setOf('َ', 'ُ', 'ً', 'ٌ', 'ّ', 'ْ', 'ٰ', '\u0670', 'أ', 'ؤ')
                        val bottomDiacritics = setOf('ِ', 'ٍ', 'إ')

                        if (char in topDiacritics) {
                            bottom = minOf(bottom, lineTop + lineHeight * 0.45f)
                        } else if (char in bottomDiacritics) {
                            top = maxOf(top, lineTop + lineHeight * 0.65f)
                        }

                        // Сужавање по X оси да елиминишемо качење суседних карактера
                        val w = composeRect.right - composeRect.left
                        val shrinkX = w * 0.2f
                        val left = composeRect.left + shrinkX
                        val right = composeRect.right - shrinkX

                        // Цртање црвене пилуле на тој локацији
                        if (right > left && bottom > top) {
                            val radius = w * 0.4f
                            canvas.drawRoundRect(
                                RectF(left, top, right, bottom),
                                radius, radius, paint
                            )
                        }
                    }
                }

                // 4. Пакујемо Bitmap у Shader. Text компонента ће ово користити као мастило!
                return BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            }
        }
    }
}
