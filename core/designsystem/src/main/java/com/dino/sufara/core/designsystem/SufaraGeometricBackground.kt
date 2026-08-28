package com.dino.sufara.core.designsystem

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.ceil
import kotlin.math.sqrt
import kotlin.math.sin

enum class BackgroundPatternStyle { RIDGE, LIGHT, DARK, DUAL }
enum class BackgroundLightMode { SOFT_BLUE, GOLD, OFF }
enum class BackgroundLightStrength(val multiplier: Float) {
    SUBTLE(0.72f), MEDIUM(1f), STRONG(1.65f)
}

/**
 * App-wide decorative layer. Static twelve-fold geometry is cached per size;
 * all optional motion shares one deliberately slow animation clock.
 */
@Composable
fun SufaraGeometricBackground(
    modifier: Modifier = Modifier,
    patternStyle: BackgroundPatternStyle = BackgroundPatternStyle.RIDGE,
    lightMode: BackgroundLightMode = BackgroundLightMode.SOFT_BLUE,
    lightStrength: BackgroundLightStrength = BackgroundLightStrength.MEDIUM,
    patternVisibility: Float = 0.11f,
    orbitEnabled: Boolean = false,
    blobEnabled: Boolean = true,
    particlesEnabled: Boolean = true,
    particleVisibility: Float = 1.5f
) {
    val density = LocalDensity.current
    val motifRadius = with(density) { 128.dp.toPx() }
    val baseStroke = with(density) { 0.9.dp.toPx() }
    val motionEnabled = particlesEnabled || (lightMode != BackgroundLightMode.OFF && (orbitEnabled || blobEnabled))

    val phaseState = if (motionEnabled) {
        val transition = rememberInfiniteTransition(label = "geometric_background")
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 60_000, easing = LinearEasing)
            ),
            label = "background_motion_phase"
        )
    } else {
        null
    }

    Canvas(
        modifier = modifier.drawWithCache {
            val geometry = buildTwelveFoldPattern(size, motifRadius)
            val visibility = patternVisibility.coerceIn(0.04f, 0.22f)
            val particles = buildParticles(size)
            val orbitDash = floatArrayOf(motifRadius * 0.72f, motifRadius * 5.55f)

            onDrawBehind {
                when (patternStyle) {
                    BackgroundPatternStyle.RIDGE -> {
                        translate(left = baseStroke * 0.8f, top = baseStroke * 1.15f) {
                            drawPath(
                                path = geometry.structure,
                                color = Color.Black.copy(alpha = visibility * 1.45f),
                                style = Stroke(width = baseStroke * 1.55f, cap = StrokeCap.Round)
                            )
                        }
                        translate(left = -baseStroke * 0.45f, top = -baseStroke * 0.55f) {
                            drawPath(
                                path = geometry.structure,
                                color = BlueRoyal.copy(alpha = visibility * 0.92f),
                                style = Stroke(width = baseStroke, cap = StrokeCap.Round)
                            )
                        }
                    }
                    BackgroundPatternStyle.LIGHT -> drawPath(
                        path = geometry.structure,
                        color = BlueRoyal.copy(alpha = visibility),
                        style = Stroke(width = baseStroke, cap = StrokeCap.Round)
                    )
                    BackgroundPatternStyle.DARK -> drawPath(
                        path = geometry.structure,
                        color = Color.Black.copy(alpha = visibility * 1.85f),
                        style = Stroke(width = baseStroke * 1.25f, cap = StrokeCap.Round)
                    )
                    BackgroundPatternStyle.DUAL -> {
                        drawPath(
                            path = geometry.structure,
                            color = Color.Black.copy(alpha = visibility * 1.45f),
                            style = Stroke(width = baseStroke * 2.5f, cap = StrokeCap.Round)
                        )
                        drawPath(
                            path = geometry.structure,
                            color = BlueRoyal.copy(alpha = visibility * 0.9f),
                            style = Stroke(width = baseStroke * 0.8f, cap = StrokeCap.Round)
                        )
                    }
                }

                // Read animation state in the draw phase: geometry remains cached
                // instead of being rebuilt on every animation frame.
                val animated = phaseState?.value ?: 0f
                val lightColor = when (lightMode) {
                    BackgroundLightMode.SOFT_BLUE -> Color(0xFF31558C)
                    BackgroundLightMode.GOLD -> GoldBase
                    BackgroundLightMode.OFF -> Color.Transparent
                }

                if (blobEnabled && lightMode != BackgroundLightMode.OFF) {
                    val angle = animated * (2f * PI.toFloat())
                    val center = Offset(
                        x = size.width * (0.5f + 0.34f * sin(angle * 0.73f)),
                        y = size.height * (0.5f + 0.39f * sin(angle * 1.07f + 1.8f))
                    )
                    drawPath(
                        path = geometry.structure,
                        brush = Brush.radialGradient(
                            colors = listOf(
                                lightColor.copy(alpha = (if (lightMode == BackgroundLightMode.GOLD) 0.13f else 0.18f) * lightStrength.multiplier),
                                lightColor.copy(alpha = 0.035f * lightStrength.multiplier),
                                Color.Transparent
                            ),
                            center = center,
                            radius = motifRadius * 1.65f
                        ),
                        style = Stroke(width = baseStroke * 1.35f, cap = StrokeCap.Round)
                    )
                }

                if (orbitEnabled && lightMode != BackgroundLightMode.OFF) {
                    val effect = PathEffect.dashPathEffect(
                        intervals = orbitDash,
                        phase = -animated * orbitDash.sum()
                    )
                    drawPath(
                        path = geometry.orbits,
                        color = lightColor.copy(alpha = (if (lightMode == BackgroundLightMode.GOLD) 0.16f else 0.22f) * lightStrength.multiplier),
                        style = Stroke(width = baseStroke * 2.8f, cap = StrokeCap.Round, pathEffect = effect)
                    )
                    drawPath(
                        path = geometry.orbits,
                        color = lightColor.copy(alpha = (if (lightMode == BackgroundLightMode.GOLD) 0.42f else 0.34f) * lightStrength.multiplier),
                        style = Stroke(width = baseStroke, cap = StrokeCap.Round, pathEffect = effect)
                    )
                }

                if (particlesEnabled) {
                    val tau = 2f * PI.toFloat()
                    val particleScale = particleVisibility.coerceIn(0.5f, 2.2f)
                    particles.forEach { particle ->
                        val x = wrap(
                            particle.origin.x + size.width * 0.08f * sin(tau * (animated * particle.speed + particle.phase)),
                            size.width
                        )
                        val y = wrap(
                            particle.origin.y - size.height * animated * particle.drift +
                                size.height * 0.025f * sin(tau * (animated * particle.wobble + particle.phase * 1.7f)),
                            size.height
                        )
                        val center = Offset(x, y)
                        drawCircle(
                            color = Color(0xFF527DB9).copy(alpha = particle.alpha * 0.24f * particleScale),
                            radius = baseStroke * particle.radius * 3.2f,
                            center = center
                        )
                        drawCircle(
                            color = Color(0xFF89A9D4).copy(alpha = particle.alpha * particleScale),
                            radius = baseStroke * particle.radius * (0.9f + particleScale * 0.12f),
                            center = center
                        )
                    }
                }
            }
        }
    ) {}
}

private data class PatternGeometry(val structure: Path, val orbits: Path)
private data class BackgroundParticle(
    val origin: Offset,
    val speed: Float,
    val wobble: Float,
    val drift: Float,
    val phase: Float,
    val radius: Float,
    val alpha: Float
)

private fun buildTwelveFoldPattern(size: Size, radius: Float): PatternGeometry {
    val structure = Path()
    val orbits = Path()
    // Centres lie on an exact triangular lattice. Its Voronoi cells are
    // regular hexagons, so neighbouring motifs share their boundary instead
    // of overlapping with an empirically chosen x/y step.
    val stepX = radius * 2f
    val stepY = stepX * sqrt(3f) / 2f
    val overscan = radius * 1.35f
    val firstRow = floor(-overscan / stepY).toInt() - 1
    val lastRow = ceil((size.height + overscan) / stepY).toInt() + 1

    for (row in firstRow..lastRow) {
        val y = row * stepY
        val rowOffset = if ((row and 1) == 0) 0f else radius
        val firstColumn = floor((-overscan - rowOffset) / stepX).toInt() - 1
        val lastColumn = ceil((size.width + overscan - rowOffset) / stepX).toInt() + 1
        for (column in firstColumn..lastColumn) {
            val x = column * stepX + rowOffset
            addTwelveFoldMotif(structure, orbits, Offset(x, y), radius)
        }
    }
    return PatternGeometry(structure, orbits)
}

private fun addTwelveFoldMotif(structure: Path, orbits: Path, center: Offset, radius: Float) {
    val startAngle = 0.0

    fun cellRadius(index: Int): Float = if (index % 2 == 0) {
        radius
    } else {
        radius * 2f / sqrt(3f)
    }

    repeat(24) { index ->
        val point = polarPoint(
            center = center,
            radius = radius * if (index % 2 == 0) 0.35f else 0.23f,
            angle = startAngle + index * PI / 12.0
        )
        if (index == 0) structure.moveTo(point.x, point.y) else structure.lineTo(point.x, point.y)
    }
    structure.close()

    repeat(12) { index ->
        val angle = startAngle + index * PI / 6.0
        val starPoint = polarPoint(center, radius * 0.35f, angle)
        val petalTip = polarPoint(center, cellRadius(index) * 0.72f, angle)
        val left = polarPoint(center, radius * 0.54f, angle - PI / 24.0)
        val right = polarPoint(center, radius * 0.54f, angle + PI / 24.0)
        structure.moveTo(starPoint.x, starPoint.y)
        structure.lineTo(left.x, left.y)
        structure.lineTo(petalTip.x, petalTip.y)
        structure.lineTo(right.x, right.y)
        structure.close()
    }

    // Each outer cell ends on the mathematically exact shared hexagonal
    // boundary. These segments therefore continue cleanly into the next tile.
    repeat(12) { index ->
        val angle = startAngle + index * PI / 6.0
        val nextAngle = startAngle + (index + 1) * PI / 6.0
        val petalTip = polarPoint(center, cellRadius(index) * 0.72f, angle)
        val nextPetalTip = polarPoint(center, cellRadius(index + 1) * 0.72f, nextAngle)
        val boundary = polarPoint(center, cellRadius(index), angle)
        val nextBoundary = polarPoint(center, cellRadius(index + 1), nextAngle)
        val outerCrown = polarPoint(center, radius * 0.76f, angle + PI / 12.0)
        structure.moveTo(petalTip.x, petalTip.y)
        structure.lineTo(boundary.x, boundary.y)
        structure.lineTo(nextBoundary.x, nextBoundary.y)
        structure.lineTo(nextPetalTip.x, nextPetalTip.y)
        structure.lineTo(outerCrown.x, outerCrown.y)
        structure.close()
    }

    repeat(12) { index ->
        val point = polarPoint(center, cellRadius(index) * 0.72f, startAngle + index * PI / 6.0)
        if (index == 0) orbits.moveTo(point.x, point.y) else orbits.lineTo(point.x, point.y)
    }
    orbits.close()
}

private fun buildParticles(size: Size): List<BackgroundParticle> {
    if (size.width <= 0f || size.height <= 0f) return emptyList()
    return List(11) { index ->
        val seed = index + 1f
        BackgroundParticle(
            origin = Offset(
                size.width * fract(seed * 0.6180339f),
                size.height * fract(seed * 0.4142135f)
            ),
            speed = 0.34f + fract(seed * 0.2718f) * 0.58f,
            wobble = 0.45f + fract(seed * 0.1732f) * 0.8f,
            drift = 0.10f + fract(seed * 0.319f) * 0.18f,
            phase = fract(seed * 0.7548777f),
            radius = 0.75f + fract(seed * 0.438f) * 1.15f,
            alpha = 0.055f + fract(seed * 0.527f) * 0.055f
        )
    }
}

private fun wrap(value: Float, limit: Float): Float {
    if (limit <= 0f) return 0f
    return value - floor(value / limit) * limit
}

private fun fract(value: Float): Float = value - floor(value)

private fun polarPoint(center: Offset, radius: Float, angle: Double): Offset = Offset(
    x = center.x + radius * cos(angle).toFloat(),
    y = center.y + radius * sin(angle).toFloat()
)
