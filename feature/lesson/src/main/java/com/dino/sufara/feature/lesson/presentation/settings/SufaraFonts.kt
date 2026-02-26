package com.dino.sufara.feature.lesson.presentation.settings

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.dino.sufara.feature.lesson.R

object SufaraFonts {
    val Lora = FontFamily(
        Font(R.font.lora_regular, FontWeight.Normal),
        Font(R.font.lora_bold, FontWeight.Bold)
    )
    
    val RobotoSlab = FontFamily(
        Font(R.font.roboto_slab_light, FontWeight.Normal),
        Font(R.font.roboto_slab_bold, FontWeight.Bold)
    )
    
    val Amiri = FontFamily(
        Font(R.font.amiri_regular, FontWeight.Normal)
    )
    
    val NotoNaskh = FontFamily(
        Font(R.font.noto_naskh_arabic_regular, FontWeight.Normal)
    )

    fun getCyrillicFont(name: String): FontFamily = when(name) {
        "Roboto Slab" -> RobotoSlab
        else -> Lora
    }

    fun getArabicFont(name: String): FontFamily = when(name) {
        "Noto Naskh" -> NotoNaskh
        else -> Amiri
    }
}