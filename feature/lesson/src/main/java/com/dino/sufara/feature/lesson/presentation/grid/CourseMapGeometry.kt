package com.dino.sufara.feature.lesson.presentation.grid

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

data class MapPoint(val x: Float, val y: Float) {
    operator fun plus(other: MapPoint) = MapPoint(x + other.x, y + other.y)
    operator fun minus(other: MapPoint) = MapPoint(x - other.x, y - other.y)
    operator fun times(scale: Float) = MapPoint(x * scale, y * scale)
}

data class MapRegion(val left: Float, val top: Float, val right: Float, val bottom: Float)

data class CourseMapConfig(
    val lessonCount: Int,
    val lessonSpacing: Float = 280f,
    val lessonRadius: Float = 58f,
    val regionalPasses: Int = 3,
    val broadCurve: Float = 0.80f,
    val laneConvergence: Float = 0.12f,
    val turnRoundness: Float = 0.65f,
    val routePacking: Float = 1.04f,
    val seed: Long = 1447L
)

data class CourseMapGeometry(
    val sampledPath: List<MapPoint>,
    val cumulativeLengths: FloatArray,
    val lessons: List<MapPoint>,
    val regions: List<MapRegion>,
    val width: Float,
    val height: Float,
    val totalLength: Float,
    val lessonRadius: Float,
    val lessonSpacing: Float
)

/** Stable macro-layout generator. Camera changes never regenerate this geometry. */
object CourseMapGenerator {
    fun generate(config: CourseMapConfig): CourseMapGeometry {
        if (config.lessonCount <= 0) return CourseMapGeometry(
            sampledPath = emptyList(),
            cumulativeLengths = FloatArray(0),
            lessons = emptyList(),
            regions = emptyList(),
            width = 0f,
            height = 0f,
            totalLength = 0f,
            lessonRadius = config.lessonRadius,
            lessonSpacing = config.lessonSpacing
        )

        val targetLength = (config.lessonCount - 1).coerceAtLeast(1) * config.lessonSpacing
        var low = config.lessonSpacing * 3.5f
        var high = config.lessonSpacing * 12f
        repeat(22) {
            val size = (low + high) / 2f
            val length = buildRoute(size, config).sampled.length()
            if (length < targetLength) low = size else high = size
        }

        val regionSize = (low + high) / 2f
        val built = buildRoute(regionSize, config)
        val source = built.sampled
        val minX = source.minOf { it.x }
        val minY = source.minOf { it.y }
        val maxX = source.maxOf { it.x }
        val maxY = source.maxOf { it.y }
        val padding = max(config.lessonRadius * 2.2f, config.lessonSpacing * 0.58f)
        val shift = MapPoint(-minX + padding, -minY + padding)
        val shifted = source.map { it + shift }
        val cumulative = cumulativeLengths(shifted)
        val totalLength = cumulative.lastOrNull() ?: 0f
        val lessons = List(config.lessonCount) { index ->
            val distance = if (config.lessonCount == 1) 0f else {
                totalLength * index / (config.lessonCount - 1).toFloat()
            }
            pointAtDistance(shifted, cumulative, distance)
        }

        return CourseMapGeometry(
            sampledPath = shifted,
            cumulativeLengths = cumulative,
            lessons = lessons,
            regions = built.regions.map { region ->
                MapRegion(
                    left = region.left + shift.x,
                    top = region.top + shift.y,
                    right = region.right + shift.x,
                    bottom = region.bottom + shift.y
                )
            },
            width = maxX - minX + padding * 2f,
            height = maxY - minY + padding * 2f,
            totalLength = totalLength,
            lessonRadius = config.lessonRadius,
            lessonSpacing = config.lessonSpacing
        )
    }

    fun countCrossings(points: List<MapPoint>): Int {
        var crossings = 0
        for (first in 0 until points.lastIndex) {
            for (second in first + 3 until points.lastIndex) {
                if (segmentsIntersect(points[first], points[first + 1], points[second], points[second + 1])) {
                    crossings++
                }
            }
        }
        return crossings
    }

    private data class BuiltRoute(val sampled: List<MapPoint>, val regions: List<MapRegion>)

    private fun buildRoute(regionSize: Float, config: CourseMapConfig): BuiltRoute {
        val packT = ((config.routePacking - 0.78f) / 0.40f).coerceIn(0f, 1f)
        val marginRatio = lerp(0.17f, 0.07f, packT)
        val gap = regionSize * lerp(0.075f, 0.025f, packT)
        val random = Random(config.seed)
        val bowFactors = FloatArray(4) { 0.82f + random.nextFloat() * 0.30f }
        val raw = mutableListOf<MapPoint>()

        appendHorizontalSweep(
            output = raw,
            left = 0f,
            top = 0f,
            size = regionSize,
            passes = config.regionalPasses,
            marginRatio = marginRatio,
            startsRight = true,
            lanesAscending = true,
            bowSign = 1f,
            bowFactor = bowFactors[0],
            config = config
        )
        appendVerticalSweep(
            output = raw,
            left = regionSize + gap,
            top = 0f,
            size = regionSize,
            passes = config.regionalPasses + 1,
            marginRatio = marginRatio,
            startsDown = false,
            lanesAscending = true,
            bowSign = -1f,
            bowFactor = bowFactors[1],
            config = config
        )
        appendHorizontalSweep(
            output = raw,
            left = regionSize + gap,
            top = regionSize + gap,
            size = regionSize,
            passes = config.regionalPasses,
            marginRatio = marginRatio,
            startsRight = false,
            lanesAscending = true,
            bowSign = -1f,
            bowFactor = bowFactors[2],
            config = config
        )
        appendVerticalSweep(
            output = raw,
            left = 0f,
            top = regionSize + gap,
            size = regionSize,
            passes = config.regionalPasses + 1,
            marginRatio = marginRatio,
            startsDown = false,
            lanesAscending = false,
            bowSign = 1f,
            bowFactor = bowFactors[3],
            config = config
        )

        // Portrait UX: the first long sweep becomes predominantly downward.
        val transposed = raw.map { MapPoint(it.y, it.x) }
        val lanePitch = regionSize * 0.74f / (config.regionalPasses - 1).coerceAtLeast(1)
        val requestedRadius = lanePitch * (0.38f + 0.42f * config.turnRoundness)
        val sampleStep = max(8f, config.lessonSpacing / 18f)
        val sampled = roundCorners(transposed, requestedRadius, sampleStep)

        val regions = listOf(
            MapRegion(0f, 0f, regionSize, regionSize),
            MapRegion(regionSize + gap, 0f, regionSize * 2f + gap, regionSize),
            MapRegion(regionSize + gap, regionSize + gap, regionSize * 2f + gap, regionSize * 2f + gap),
            MapRegion(0f, regionSize + gap, regionSize, regionSize * 2f + gap)
        ).map { MapRegion(it.top, it.left, it.bottom, it.right) }

        return BuiltRoute(sampled, regions)
    }

    private fun appendHorizontalSweep(
        output: MutableList<MapPoint>,
        left: Float,
        top: Float,
        size: Float,
        passes: Int,
        marginRatio: Float,
        startsRight: Boolean,
        lanesAscending: Boolean,
        bowSign: Float,
        bowFactor: Float,
        config: CourseMapConfig
    ) {
        val margin = size * marginRatio
        val laneValues = evenlySpaced(top + margin, top + size - margin, passes).let {
            if (lanesAscending) it else it.reversed()
        }
        val center = top + size / 2f
        laneValues.forEachIndexed { lane, laneY ->
            val goesRight = if (lane % 2 == 0) startsRight else !startsRight
            val from = if (goesRight) left + margin else left + size - margin
            val to = if (goesRight) left + size - margin else left + margin
            val points = macroFractions.map { q ->
                val hump = sin(PI.toFloat() * q)
                val soft = hump * hump
                val amplitude = abs(config.broadCurve) * (size / (passes + 1f)) * 0.20f
                val sign = bowSign * if (lane % 2 == 0) 1f else -1f
                val curve = amplitude * bowFactor * sign * hump
                val pull = (center - laneY) * config.laneConvergence * 0.42f * soft
                MapPoint(lerp(from, to, q), laneY + curve + pull)
            }
            appendDistinct(output, points)
            if (lane < laneValues.lastIndex) output += MapPoint(to, laneValues[lane + 1])
        }
    }

    private fun appendVerticalSweep(
        output: MutableList<MapPoint>,
        left: Float,
        top: Float,
        size: Float,
        passes: Int,
        marginRatio: Float,
        startsDown: Boolean,
        lanesAscending: Boolean,
        bowSign: Float,
        bowFactor: Float,
        config: CourseMapConfig
    ) {
        val margin = size * marginRatio
        val laneValues = evenlySpaced(left + margin, left + size - margin, passes).let {
            if (lanesAscending) it else it.reversed()
        }
        val center = left + size / 2f
        laneValues.forEachIndexed { lane, laneX ->
            val goesDown = if (lane % 2 == 0) startsDown else !startsDown
            val from = if (goesDown) top + margin else top + size - margin
            val to = if (goesDown) top + size - margin else top + margin
            val points = macroFractions.map { q ->
                val hump = sin(PI.toFloat() * q)
                val soft = hump * hump
                val amplitude = abs(config.broadCurve) * (size / (passes + 1f)) * 0.20f
                val sign = bowSign * if (lane % 2 == 0) 1f else -1f
                val curve = amplitude * bowFactor * sign * hump
                val pull = (center - laneX) * config.laneConvergence * 0.42f * soft
                MapPoint(laneX + curve + pull, lerp(from, to, q))
            }
            appendDistinct(output, points)
            if (lane < laneValues.lastIndex) output += MapPoint(laneValues[lane + 1], to)
        }
    }

    private fun roundCorners(points: List<MapPoint>, requestedRadius: Float, sampleStep: Float): List<MapPoint> {
        if (points.size < 3) return points
        val result = mutableListOf(points.first())
        var current = points.first()
        for (index in 1 until points.lastIndex) {
            val previous = points[index - 1]
            val corner = points[index]
            val next = points[index + 1]
            val incoming = previous - corner
            val outgoing = next - corner
            val incomingLength = incoming.length()
            val outgoingLength = outgoing.length()
            if (incomingLength < 0.01f || outgoingLength < 0.01f) continue
            val radius = min(requestedRadius, min(incomingLength * 0.38f, outgoingLength * 0.38f))
            val pIn = corner + incoming * (radius / incomingLength)
            val pOut = corner + outgoing * (radius / outgoingLength)
            appendLineSamples(result, current, pIn, sampleStep)
            appendQuadraticSamples(result, pIn, corner, pOut, sampleStep)
            current = pOut
        }
        appendLineSamples(result, current, points.last(), sampleStep)
        return result
    }

    private fun appendLineSamples(output: MutableList<MapPoint>, start: MapPoint, end: MapPoint, step: Float) {
        val count = max(1, ceil((end - start).length() / step).toInt())
        for (index in 1..count) output += lerp(start, end, index / count.toFloat())
    }

    private fun appendQuadraticSamples(
        output: MutableList<MapPoint>,
        start: MapPoint,
        control: MapPoint,
        end: MapPoint,
        step: Float
    ) {
        val estimate = (control - start).length() + (end - control).length()
        val count = max(4, ceil(estimate / step).toInt())
        for (index in 1..count) {
            val t = index / count.toFloat()
            val inverse = 1f - t
            output += start * (inverse * inverse) + control * (2f * inverse * t) + end * (t * t)
        }
    }

    private fun cumulativeLengths(points: List<MapPoint>): FloatArray {
        val cumulative = FloatArray(points.size)
        for (index in 1 until points.size) {
            cumulative[index] = cumulative[index - 1] + (points[index] - points[index - 1]).length()
        }
        return cumulative
    }

    private fun pointAtDistance(points: List<MapPoint>, cumulative: FloatArray, distance: Float): MapPoint {
        if (points.size <= 1) return points.firstOrNull() ?: MapPoint(0f, 0f)
        var low = 0
        var high = cumulative.lastIndex
        while (low < high) {
            val middle = (low + high) ushr 1
            if (cumulative[middle] < distance) low = middle + 1 else high = middle
        }
        val upper = low.coerceAtLeast(1)
        val lower = upper - 1
        val segmentLength = (cumulative[upper] - cumulative[lower]).coerceAtLeast(0.0001f)
        val t = ((distance - cumulative[lower]) / segmentLength).coerceIn(0f, 1f)
        return lerp(points[lower], points[upper], t)
    }

    private fun segmentsIntersect(a: MapPoint, b: MapPoint, c: MapPoint, d: MapPoint): Boolean {
        val abC = cross(a, b, c)
        val abD = cross(a, b, d)
        val cdA = cross(c, d, a)
        val cdB = cross(c, d, b)
        return abC * abD < -0.0001f && cdA * cdB < -0.0001f
    }

    private fun cross(a: MapPoint, b: MapPoint, c: MapPoint): Float =
        (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x)

    private fun evenlySpaced(start: Float, end: Float, count: Int): List<Float> =
        if (count <= 1) listOf((start + end) / 2f)
        else List(count) { index -> lerp(start, end, index / (count - 1f)) }

    private fun appendDistinct(output: MutableList<MapPoint>, values: List<MapPoint>) {
        values.forEach { point -> if (output.lastOrNull() != point) output += point }
    }

    private fun List<MapPoint>.length(): Float = zipWithNext().sumOf { (a, b) -> (b - a).length().toDouble() }.toFloat()
    private fun MapPoint.length(): Float = sqrt(x * x + y * y)
    private fun lerp(start: Float, end: Float, t: Float): Float = start + (end - start) * t
    private fun lerp(start: MapPoint, end: MapPoint, t: Float): MapPoint = start + (end - start) * t
    private val macroFractions = listOf(0f, 0.28f, 0.50f, 0.72f, 1f)
}
