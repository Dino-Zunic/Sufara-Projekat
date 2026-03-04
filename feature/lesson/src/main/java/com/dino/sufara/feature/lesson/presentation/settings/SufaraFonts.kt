package com.dino.sufara.feature.lesson.presentation.settings

import android.content.Context
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.dino.sufara.feature.lesson.R

object SufaraFonts {
    val Lora = FontFamily(Font(R.font.lora_regular, FontWeight.Normal), Font(R.font.lora_bold, FontWeight.Bold))
    val RobotoSlab = FontFamily(Font(R.font.roboto_slab_light, FontWeight.Normal), Font(R.font.roboto_slab_bold, FontWeight.Bold))
    val Amiri = FontFamily(Font(R.font.amiri_regular, FontWeight.Normal))
    val NotoNaskh = FontFamily(Font(R.font.noto_naskh_arabic_regular, FontWeight.Normal))
    val Kfgqpc = FontFamily(Font(R.font.kfgqpc, FontWeight.Normal))

    fun getCyrillicFont(name: String): FontFamily = when(name) {
        "Roboto Slab" -> RobotoSlab
        else -> Lora
    }

    fun getArabicFont(name: String): FontFamily = when(name) {
        "Noto Naskh" -> NotoNaskh
        "KFGQPC" -> Kfgqpc
        else -> Amiri
    }

    fun getArabicTypeface(context: Context, name: String): Typeface {
        return when(name) {
            "Noto Naskh" -> ResourcesCompat.getFont(context, R.font.noto_naskh_arabic_regular) ?: Typeface.DEFAULT
            "KFGQPC" -> ResourcesCompat.getFont(context, R.font.kfgqpc) ?: Typeface.DEFAULT
            else -> ResourcesCompat.getFont(context, R.font.amiri_regular) ?: Typeface.DEFAULT
        }
    }
}