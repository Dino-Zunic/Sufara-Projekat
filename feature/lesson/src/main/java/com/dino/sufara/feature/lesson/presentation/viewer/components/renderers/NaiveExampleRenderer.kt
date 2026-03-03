package com.dino.sufara.feature.lesson.presentation.viewer.components.renderers

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
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
        // 1. STRIKTNO ODVAJANJE
        val redIndices = mutableSetOf<Int>()
        when {
            globalType == HighlightType.HARAKAH && action is HighlightAction.Harakah -> {
                // Uzimamo SAMO herekete. Osnovno slovo (supports) ostaje zlatno.
                redIndices.addAll(action.diacritics)
            }
            globalType == HighlightType.HARF && action is HighlightAction.Harf -> {
                // Uzimamo SAMO harfove!
                redIndices.addAll(action.characters)
                // OBRISANO: redIndices.addAll(action.attachedDiacritics)
            }
        }

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                fontFamily = arabicFont,
                fontSize = fontSize,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                onTextLayout = onTextLayout,
                text = buildAnnotatedString {
                    
                    // 2. PAMETNO GRUPISANJE NIZOVA
                    var currentColorIsRed = text.isNotEmpty() && 0 in redIndices
                    var currentStartIndex = 0

                    for (i in text.indices) {
                        val isRed = i in redIndices
                        
                        // Kada se boja promeni, zatvaramo prethodni blok i upisujemo ga
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
                    
                    // Dodajemo poslednji preostali deo teksta
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