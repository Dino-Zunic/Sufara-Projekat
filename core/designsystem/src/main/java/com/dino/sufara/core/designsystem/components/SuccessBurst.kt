package com.dino.sufara.core.designsystem.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import com.dino.sufara.core.designsystem.GoldBase
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** A short formula-based celebration; no particle objects survive the animation. */
@Composable
fun SuccessBurst(
    trigger: Int,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    particleCount: Int = 13,
    origin: Offset? = null
) {
    val progress = remember { Animatable(1f) }
    LaunchedEffect(trigger, enabled) {
        if (enabled && trigger > 0) {
            progress.snapTo(0f)
            progress.animateTo(1f, tween(920, easing = LinearOutSlowInEasing))
        }
    }

    Canvas(modifier) {
        if (!enabled || progress.value >= 1f || trigger <= 0) return@Canvas
        val t = progress.value
        val count = particleCount.coerceIn(10, 36)
        val launchOrigin = origin ?: center
        repeat(count) { index ->
            val angleJitter = stableFraction(index, 17) - 0.5f
            val fraction = (index + 0.5f) / count
            val angle = (-PI * 0.92 + fraction * PI * 1.84 + angleJitter * 0.16).toFloat()
            val speedFactor = 0.18f + stableFraction(index, 41) * 0.34f
            val speed = size.minDimension * speedFactor
            val phaseDelay = stableFraction(index, 73) * 0.085f
            val localT = ((t - phaseDelay) / (1f - phaseDelay)).coerceIn(0f, 1f)
            val velocity = Offset(cos(angle) * speed, sin(angle) * speed - size.minDimension * 0.16f)
            val gravity = size.minDimension * 0.88f
            val position = Offset(
                x = launchOrigin.x + velocity.x * localT,
                y = launchOrigin.y + velocity.y * localT + 0.5f * gravity * localT * localT
            )
            val initialAlpha = 0.48f + stableFraction(index, 97) * 0.52f
            val alpha = initialAlpha.coerceAtMost(1f) * (1f - localT) * (1f - localT)
            val radius = size.minDimension * (0.0105f + stableFraction(index, 131) * 0.0075f)
            rotate(degrees = index * 29f + localT * (540f + (index % 3) * 120f), pivot = position) {
                drawPath(
                    path = starPath(position, radius),
                    color = if (index % 4 == 0) Color(0xFFFFE9A0).copy(alpha = alpha) else GoldBase.copy(alpha = alpha)
                )
            }
        }
    }
}

private fun stableFraction(index: Int, salt: Int): Float {
    var value = index * 1_103_515_245 + salt * 12_345
    value = value xor (value ushr 16)
    return (value and 0x7FFF) / 32_767f
}

private fun starPath(center: Offset, radius: Float): Path = Path().apply {
    repeat(10) { pointIndex ->
        val pointRadius = if (pointIndex % 2 == 0) radius else radius * 0.43f
        val angle = -PI / 2.0 + pointIndex * PI / 5.0
        val x = center.x + cos(angle).toFloat() * pointRadius
        val y = center.y + sin(angle).toFloat() * pointRadius
        if (pointIndex == 0) moveTo(x, y) else lineTo(x, y)
    }
    close()
}
