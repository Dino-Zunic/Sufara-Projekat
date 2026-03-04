package com.dino.sufara.core.designsystem.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dino.sufara.core.designsystem.BlueMidnight
import com.dino.sufara.core.designsystem.GoldBase
import com.dino.sufara.core.designsystem.GoldLight
import com.dino.sufara.core.designsystem.TextParchment

@Composable
fun GoldenWireButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    font: FontFamily = FontFamily.Default,
    wireThickness: Dp = 1.dp, 
    animDuration: Int = 3500, 
    baseAlpha: Float = 0.5f // Базна светлост жице (50%)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wire_anim")
    val wireEasing = Easing { fraction ->
        val linear = fraction
        val smooth = fraction * fraction * (3 - 2 * fraction)
        (linear * 0.4f) + (smooth * 0.6f)
    }

    val angle by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(animDuration, easing = wireEasing), repeatMode = RepeatMode.Restart),
        label = "angle"
    )

    val baseGold = GoldBase.copy(alpha = baseAlpha)
    val borderGradient = remember(baseGold) {
        Brush.sweepGradient(0.0f to baseGold, 0.6f to baseGold, 0.95f to GoldLight, 1.0f to baseGold)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(32.dp))
            .clickable(onClick = onClick)
            .drawBehind {
                rotate(angle) { drawCircle(brush = borderGradient, radius = size.width, center = Offset(size.width / 2, size.height / 2)) }
            }
            .padding(wireThickness)
            .background(BlueMidnight, RoundedCornerShape(30.dp))
            // ИСПРАВКА: Уклоњен fillMaxHeight() одавде!
            .padding(horizontal = 32.dp, vertical = 16.dp), 
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = TextParchment, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = font, letterSpacing = 2.sp)
    }
}