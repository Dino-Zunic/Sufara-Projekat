package com.dino.sufara.feature.lesson.presentation.viewer.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dino.sufara.core.designsystem.GoldBase
import com.dino.sufara.core.designsystem.TextMutedGold
import com.dino.sufara.core.designsystem.TextParchment
import com.dino.sufara.core.designsystem.TextSilver
import com.dino.sufara.feature.lesson.domain.util.asScript
import com.dino.sufara.feature.lesson.presentation.settings.BodyTextColorTheme
import com.dino.sufara.feature.lesson.presentation.settings.LocalSufaraSettings
import com.dino.sufara.feature.lesson.presentation.settings.SufaraFonts

@Composable
fun HarfShowcase(symbol: String) {
    val settings = LocalSufaraSettings.current
    val arabicFont = SufaraFonts.getArabicFont(settings.arabicFont)
    
    val bodyColor = when(settings.bodyTextColorTheme) {
        BodyTextColorTheme.PARCHMENT -> TextParchment
        BodyTextColorTheme.SILVER -> TextSilver
        BodyTextColorTheme.MUTED_GOLD -> TextMutedGold
    }
    
    val tatweel = "ـ"
    val nonConnectingLetters = setOf('ا', 'د', 'ذ', 'ر', 'ز', 'و', 'أ', 'إ', 'ؤ', 'ء', 'ة', 'ى')
    val isNonConnecting = symbol.firstOrNull() in nonConnectingLetters || symbol == "لا" || symbol == "ة"

    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp, top = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        if (isNonConnecting) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
                HarfFormItem(text = "$tatweel$symbol", label = "спојено", font = arabicFont, color = GoldBase, bodyColor = bodyColor, fontSize = 110.sp)
                HarfFormItem(text = symbol, label = "одвојено", font = arabicFont, color = GoldBase, bodyColor = bodyColor, fontSize = 110.sp)
            }
        } else {
            HarfFormItem(text = symbol, label = "самостално", font = arabicFont, color = GoldBase, bodyColor = bodyColor, fontSize = 140.sp, shadow = Shadow(color = GoldBase.copy(alpha = 0.5f), blurRadius = 20f))
            Spacer(modifier = Modifier.height(32.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
                HarfFormItem(text = "$tatweel$symbol", label = "на крају", font = arabicFont, color = GoldBase, bodyColor = bodyColor, fontSize = 65.sp)
                HarfFormItem(text = "$tatweel$symbol$tatweel", label = "у средини", font = arabicFont, color = GoldBase, bodyColor = bodyColor, fontSize = 65.sp)
                HarfFormItem(text = "$symbol$tatweel", label = "на почетку", font = arabicFont, color = GoldBase, bodyColor = bodyColor, fontSize = 65.sp)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Divider(color = bodyColor.copy(alpha = 0.2f), modifier = Modifier.fillMaxWidth(0.8f))
    }
}

@Composable
private fun HarfFormItem(text: String, label: String, font: FontFamily, color: Color, bodyColor: Color, fontSize: TextUnit, shadow: Shadow? = null) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
        TightHarfText(text = text, fontSize = fontSize, color = color, shadow = shadow)
        Text(text = label.asScript(), color = bodyColor.copy(alpha = 0.6f), fontSize = 12.sp, textAlign = TextAlign.Center, lineHeight = 16.sp, modifier = Modifier.padding(top = 12.dp))
    }
}

/**
 * ОВО ЈЕ ИМПЛЕМЕНТАЦИЈА ТВОЈЕ ИДЕЈЕ!
 * Потпуно заобилазимо Compose Text. Меримо мастило, правимо платно тачне величине,
 * и онда буквално "прецртавамо" вектор фонта на нове координате платна.
 */
@Composable
private fun TightHarfText(text: String, fontSize: TextUnit, color: Color, shadow: Shadow? = null) {
    val context = LocalContext.current
    val settings = LocalSufaraSettings.current
    val density = LocalDensity.current
    val fontSizePx = with(density) { fontSize.toPx() }
    
    val typeface = remember(settings.arabicFont) { SufaraFonts.getArabicTypeface(context, settings.arabicFont) }
    
    // 1. Меримо тачне пикселе и правимо "Paint" објекат са бојама и сенкама
    val (inkRect, paint) = remember(text, fontSizePx, typeface, color, shadow) {
        val p = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            this.typeface = typeface
            this.textSize = fontSizePx
            this.color = color.toArgb()
            shadow?.let {
                setShadowLayer(it.blurRadius, it.offset.x, it.offset.y, it.color.toArgb())
            }
        }
        val r = android.graphics.Rect()
        p.getTextBounds(text, 0, text.length, r)
        Pair(r, p)
    }

    // Падинг око самог мастила
    val padDp = 8.dp 
    val padPx = with(density) { padDp.toPx() }

    // 2. Дефинишемо величину нашег платна да буде тачно ширина мастила + падинг
    val canvasWidth = with(density) { (inkRect.width() + padPx * 2).toDp() }
    val canvasHeight = with(density) { (inkRect.height() + padPx * 2).toDp() }

    // 3. Цртамо чисто платно! Нема више невидљивих маргина.
    Canvas(
        modifier = Modifier
            .size(width = canvasWidth, height = canvasHeight)
    ) {
        drawIntoCanvas { canvas ->
            // Померамо координате. Font иначе црта од 0, али његово мастило креће од inkRect.left/top
            // Одузимањем тих вредности, ми "привлачимо" мастило тачно у горњи леви угао нашег Canvasa + падинг.
            val x = padPx - inkRect.left
            val y = padPx - inkRect.top
            
            // Наређујемо GPU да директно нацрта векторе слова!
            canvas.nativeCanvas.drawText(text, x, y, paint)
        }
    }
}