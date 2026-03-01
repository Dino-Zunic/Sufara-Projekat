package com.dino.sufara.feature.lesson.presentation.viewer.components.renderers

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import com.dino.sufara.core.designsystem.GoldBase
import com.dino.sufara.core.designsystem.MoltenRed
import com.dino.sufara.feature.lesson.domain.util.HighlightAction
import com.dino.sufara.feature.lesson.domain.util.HighlightType

class SpanStyleExampleRenderer : ArabicExampleRenderer {
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
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                fontFamily = arabicFont,
                fontSize = fontSize,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                text = buildAnnotatedString {
                    val wordStart = length
                    append(text)
                    for (i in text.indices) {
                        val isRedLayer1 = when (globalType) {
                            HighlightType.HARAKAH -> action is HighlightAction.Harakah && (action.diacritics.contains(i) || action.supports.contains(i))
                            else -> false
                        }
                        if (isRedLayer1) {
                            addStyle(SpanStyle(color = MoltenRed, shadow = redGlow), wordStart + i, wordStart + i + 1)
                        } else {
                            val isGold = globalType == HighlightType.HARF
                            if (isGold) {
                                addStyle(SpanStyle(color = GoldBase, shadow = textGlow), wordStart + i, wordStart + i + 1)
                            } else {
                                addStyle(SpanStyle(color = Color.Transparent), wordStart + i, wordStart + i + 1)
                            }
                        }
                    }
                }
            )
            Text(
                fontFamily = arabicFont,
                fontSize = fontSize,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                onTextLayout = onTextLayout,
                text = buildAnnotatedString {
                    val wordStart = length
                    append(text)
                    for (i in text.indices) {
                        when (globalType) {
                            HighlightType.HARAKAH -> {
                                val isHole = action is HighlightAction.Harakah && action.diacritics.contains(i)
                                if (isHole) addStyle(SpanStyle(color = Color.Transparent), wordStart + i, wordStart + i + 1)
                                else addStyle(SpanStyle(color = GoldBase, shadow = textGlow), wordStart + i, wordStart + i + 1)
                            }
                            else -> {
                                val isTargetHarf = action is HighlightAction.Harf && action.characters.contains(i)
                                if (isTargetHarf) addStyle(SpanStyle(color = MoltenRed, shadow = redGlow), wordStart + i, wordStart + i + 1)
                                else addStyle(SpanStyle(color = Color.Transparent), wordStart + i, wordStart + i + 1)
                            }
                        }
                    }
                }
            )
        }
    }
}