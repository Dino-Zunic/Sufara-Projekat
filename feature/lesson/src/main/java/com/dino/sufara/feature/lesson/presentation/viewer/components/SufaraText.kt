package com.dino.sufara.feature.lesson.presentation.viewer.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun SufaraText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    baseFontSize: TextUnit = 18.sp,
    arabicFontSize: TextUnit = 32.sp,
    lineHeight: TextUnit = 32.sp
) {
    val arabicRegex = Regex("[\\u0600-\\u06FF\\u0750-\\u077F\\u08A0-\\u08FF]+")
    
    val annotatedString = buildAnnotatedString {
        val parts = text.split("**")
        parts.forEachIndexed { index, part ->
            val isBold = index % 2 != 0
            val baseStyle = SpanStyle(
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                color = color,
                fontSize = baseFontSize
            )

            withStyle(baseStyle) {
                var lastIndex = 0
                arabicRegex.findAll(part).forEach { matchResult ->
                    append(part.substring(lastIndex, matchResult.range.first))
                    
                    withStyle(SpanStyle(fontSize = arabicFontSize, fontWeight = FontWeight.Normal, color = MaterialTheme.colorScheme.primary)) {
                        append(matchResult.value)
                    }
                    lastIndex = matchResult.range.last + 1
                }
                append(part.substring(lastIndex))
            }
        }
    }

    Text(text = annotatedString, modifier = modifier, lineHeight = lineHeight)
}