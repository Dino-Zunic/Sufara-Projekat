package com.dino.sufara.feature.lesson.presentation.viewer.components.renderers

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import com.dino.sufara.core.designsystem.GoldBase
import com.dino.sufara.feature.lesson.domain.util.HighlightAction
import com.dino.sufara.feature.lesson.domain.util.HighlightType

class EmptyExampleRenderer : ArabicExampleRenderer {
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
        Box(modifier = Modifier.wrapContentSize(), contentAlignment = Alignment.Center) {
            Text(
                text = text,
                fontFamily = arabicFont,
                fontSize = fontSize,
                color = GoldBase,
                style = androidx.compose.ui.text.TextStyle(shadow = textGlow),
                textAlign = TextAlign.Center,
                onTextLayout = onTextLayout
            )
        }
    }
}