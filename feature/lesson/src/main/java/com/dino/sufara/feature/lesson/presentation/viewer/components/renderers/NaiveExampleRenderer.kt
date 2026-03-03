package com.dino.sufara.feature.lesson.presentation.viewer.components.renderers

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import com.dino.sufara.core.designsystem.GoldBase
import com.dino.sufara.core.designsystem.MoltenRed
import com.dino.sufara.feature.lesson.domain.util.HighlightAction
import com.dino.sufara.feature.lesson.domain.util.HighlightType

class NaiveExampleRenderer : ArabicExampleRenderer {
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
        val redIndices = mutableSetOf<Int>()
        when {
            globalType == HighlightType.HARAKAH && action is HighlightAction.Harakah -> {
                redIndices.addAll(action.diacritics)
            }
            globalType == HighlightType.HARF && action is HighlightAction.Harf -> {
                redIndices.addAll(action.characters)
            }
        }

        Box(modifier = Modifier.wrapContentSize(), contentAlignment = Alignment.Center) {
            Text(
                fontFamily = arabicFont,
                fontSize = fontSize,
                textAlign = TextAlign.Center,
                onTextLayout = onTextLayout,
                text = buildAnnotatedString {
                    
                    var currentColorIsRed = text.isNotEmpty() && 0 in redIndices
                    var currentStartIndex = 0

                    for (i in text.indices) {
                        val isRed = i in redIndices
                        
                        if (isRed != currentColorIsRed) {
                            val color = if (currentColorIsRed) MoltenRed else GoldBase
                            val shadow = if (currentColorIsRed) redGlow else textGlow
                            
                            withStyle(SpanStyle(color = color, shadow = shadow)) {
                                append(text.substring(currentStartIndex, i))
                            }
                            
                            currentColorIsRed = isRed
                            currentStartIndex = i
                        }
                    }
                    
                    if (currentStartIndex < text.length) {
                        val color = if (currentColorIsRed) MoltenRed else GoldBase
                        val shadow = if (currentColorIsRed) redGlow else textGlow
                        withStyle(SpanStyle(color = color, shadow = shadow)) {
                            append(text.substring(currentStartIndex, text.length))
                        }
                    }
                }
            )
        }
    }
}