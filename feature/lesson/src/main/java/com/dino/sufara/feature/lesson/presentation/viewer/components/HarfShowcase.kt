package com.dino.sufara.feature.lesson.presentation.viewer.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.LineHeightStyle
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
    
    // Lista harfova koji se NE spajaju sa narednim (levim) slovom
    val nonConnectingLetters = setOf('ا', 'د', 'ذ', 'ر', 'ز', 'و', 'أ', 'إ', 'ؤ', 'ء', 'ة', 'ى')
    val isNonConnecting = symbol.firstOrNull() in nonConnectingLetters

    Column(
        modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp, top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isNonConnecting) {
            // 1. SLUČAJ: Ne spaja se (Samo 2 oblika u redu)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Čitamo sa desna na levo, pa je levi deo ekrana "na kraju"
                HarfFormItem(
                    text = "$tatweel$symbol", 
                    label = "у средини\nи на крају", 
                    font = arabicFont, 
                    color = GoldBase, 
                    bodyColor = bodyColor, 
                    fontSize = 110.sp // Manje od glavnog, veće od ona 3
                )
                HarfFormItem(
                    text = symbol, 
                    label = "самостално\nи на почетку", 
                    font = arabicFont, 
                    color = GoldBase, 
                    bodyColor = bodyColor, 
                    fontSize = 110.sp
                )
            }
        } else {
            // 2. SLUČAJ: Spaja se normalno (1 veliki + 3 mala)
            HarfFormItem(
                text = symbol, 
                label = "самостално", 
                font = arabicFont, 
                color = GoldBase, 
                bodyColor = bodyColor, 
                fontSize = 140.sp,
                shadow = Shadow(color = GoldBase.copy(alpha = 0.5f), blurRadius = 20f)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
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
private fun HarfFormItem(
    text: String, 
    label: String, 
    font: FontFamily, 
    color: Color, 
    bodyColor: Color, 
    fontSize: TextUnit,
    shadow: Shadow? = null
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Optimizovana komponenta koja eliminiše višak prostora
        TightHarfText(
            text = text,
            font = font,
            fontSize = fontSize,
            color = color,
            shadow = shadow
        )
        
        Text(
            text = label.asScript(),
            color = bodyColor.copy(alpha = 0.6f),
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp, // Da bi tekst u dva reda (kod nespajajućih) bio lepši
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Suppress("DEPRECATION")
@Composable
private fun TightHarfText(
    text: String,
    font: FontFamily,
    fontSize: TextUnit,
    color: Color,
    shadow: Shadow? = null
) {
    // Ova konfiguracija reže sav nepotrebni prostor iznad i ispod fonta
    val textStyle = TextStyle(
        fontFamily = font,
        fontSize = fontSize,
        color = color,
        shadow = shadow,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Proportional,
            trim = LineHeightStyle.Trim.Both
        )
    )

    // unbounded = true omogućava da font slobodno iscrta donje crte (poput Ra ili Vaw) 
    // van svojih granica bez odsecanja, dok sam Box zauzima minimalan mogući prostor.
    Box(
        modifier = Modifier.wrapContentHeight(unbounded = true),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = textStyle,
            modifier = Modifier.padding(vertical = 4.dp) // Minimalni sigurnosni bafer
        )
    }
}