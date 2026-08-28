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
import androidx.compose.runtime.compositionLocalOf
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
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

enum class WireMotionStyle { UNIFORM, ORGANIC, CALM }

val LocalWireMotionStyle = compositionLocalOf { WireMotionStyle.ORGANIC }

@Composable
fun GoldenWireButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    font: FontFamily = FontFamily.Default,
    fontWeight: FontWeight = FontWeight.Bold,
    wireThickness: Dp = 1.dp, 
    animDuration: Int = 3500, 
    baseAlpha: Float = 0.5f,
    enabled: Boolean = true,
    onDisabledClick: (() -> Unit)? = null,
    motionStyle: WireMotionStyle? = null
) {
    val angle = rememberGoldenWireAngle(enabled, motionStyle, animDuration, text)

    val baseGold = if (enabled) GoldBase.copy(alpha = baseAlpha) else Color.Gray.copy(alpha = 0.2f)
    val highlightGold = if (enabled) GoldLight else Color.Gray.copy(alpha = 0.3f)
    
    val borderGradient = remember(baseGold, highlightGold) {
        Brush.sweepGradient(0.0f to baseGold, 0.6f to baseGold, 0.95f to highlightGold, 1.0f to baseGold)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(32.dp))
            .clickable(
                enabled = enabled || onDisabledClick != null,
                onClick = if (enabled) onClick else onDisabledClick ?: {}
            )
            .drawBehind {
                rotate(angle) { drawCircle(brush = borderGradient, radius = size.width, center = Offset(size.width / 2, size.height / 2)) }
            }
            .padding(wireThickness)
            .background(BlueMidnight, RoundedCornerShape(30.dp))
            .padding(horizontal = 24.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text, 
            color = if (enabled) TextParchment else TextParchment.copy(alpha = 0.3f), 
            fontSize = 16.sp, 
            fontWeight = fontWeight,
            fontFamily = font, 
            letterSpacing = 1.5.sp,
            lineHeight = 24.sp,
            maxLines = 1,
            softWrap = false
        )
    }
}

/** Stable random phase plus an integer number of rotations keeps every loop seamless. */
@Composable
fun rememberGoldenWireAngle(
    enabled: Boolean,
    motionStyle: WireMotionStyle? = null,
    baseDurationMillis: Int = 3500,
    phaseKey: Any? = Unit
): Float {
    val resolvedMotionStyle = motionStyle ?: LocalWireMotionStyle.current
    val phase = remember(phaseKey) { Random.nextFloat() }
    if (!enabled) return phase * 360f

    val infiniteTransition = rememberInfiniteTransition(label = "wire_anim")
    val clock by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(baseDurationMillis * 5, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wire_clock"
    )
    val phaseAngle = phase * 360f
    val wave = sin((clock + phase) * (2f * PI.toFloat()))
    return when (resolvedMotionStyle) {
        WireMotionStyle.UNIFORM -> phaseAngle + clock * 1800f
        WireMotionStyle.ORGANIC -> phaseAngle + clock * 1800f + wave * 24f
        WireMotionStyle.CALM -> phaseAngle + clock * 1080f + wave * 14f
    }
}
