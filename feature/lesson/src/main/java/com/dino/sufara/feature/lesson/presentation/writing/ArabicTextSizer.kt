package com.dino.sufara.feature.lesson.presentation.writing

import android.content.Context
import android.graphics.Paint
import android.graphics.Rect
import com.dino.sufara.feature.lesson.presentation.settings.SufaraFonts
import java.util.concurrent.ConcurrentHashMap

/** Fits using actual glyph bounds; Arabic font metrics contain too much invisible vertical space. */
object ArabicTextSizer {
    private val cache = ConcurrentHashMap<String, Float>()

    fun fitTextSizePx(
        context: Context,
        text: String,
        fontName: String,
        maximumWidthPx: Int,
        maximumHeightPx: Int
    ): Float {
        if (text.isBlank() || maximumWidthPx <= 0 || maximumHeightPx <= 0) return 24f
        val key = "$fontName\u0000$text\u0000$maximumWidthPx\u0000$maximumHeightPx"
        return cache.getOrPut(key) {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = SufaraFonts.getArabicTypeface(context, fontName)
            }
            val bounds = Rect()
            fun fits(size: Float): Boolean {
                paint.textSize = size
                paint.getTextBounds(text, 0, text.length, bounds)
                return bounds.width() <= maximumWidthPx && bounds.height() <= maximumHeightPx
            }

            var size = 28f
            while (!fits(size) && size > 10f) size /= 1.2f
            var previous = size
            while (size < 720f && fits(size)) {
                previous = size
                size *= 1.2f
            }
            previous.coerceAtLeast(10f)
        }
    }
}
