package com.dino.sufara.core.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val IpaFontFamily = FontFamily(
    Font(resId = R.font.doulossil_regular, weight = FontWeight.Normal)
)

val SufaraTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    displayMedium = TextStyle(
        fontFamily = IpaFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp
    )
)