package com.dino.sufara.feature.lesson.domain.util

import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.ceil
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

data class WritingPoint(val x: Float, val y: Float) {
    operator fun plus(other: WritingPoint) = WritingPoint(x + other.x, y + other.y)
    operator fun minus(other: WritingPoint) = WritingPoint(x - other.x, y - other.y)
}

data class WritingBounds(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top
    val center: WritingPoint get() = WritingPoint((left + right) / 2f, (top + bottom) / 2f)
}

data class WritingReferenceSample(
    val point: WritingPoint,
    val tangent: WritingPoint,
    val radius: Float,
    val componentId: Int
)

data class WritingReferenceComponent(
    val id: Int,
    val sampleIndices: IntRange,
    val approximateLength: Float
)

data class WritingReference(
    val width: Int,
    val height: Int,
    val samples: List<WritingReferenceSample>,
    val components: List<WritingReferenceComponent>,
    val guideArgb: IntArray = IntArray(0),
    val contentBounds: WritingBounds = WritingBounds(0f, 0f, width.toFloat(), height.toFloat())
) {
    companion object {
        fun fromComponents(
            width: Int,
            height: Int,
            components: List<List<WritingPoint>>,
            radius: Float = 3f
        ): WritingReference {
            val samples = mutableListOf<WritingReferenceSample>()
            val metadata = mutableListOf<WritingReferenceComponent>()
            components.forEachIndexed { componentId, points ->
                val first = samples.size
                points.forEachIndexed { index, point ->
                    val previous = points[(index - 1).coerceAtLeast(0)]
                    val next = points[(index + 1).coerceAtMost(points.lastIndex)]
                    samples += WritingReferenceSample(
                        point = point,
                        tangent = normalized(next - previous),
                        radius = radius,
                        componentId = componentId
                    )
                }
                if (samples.size > first) {
                    metadata += WritingReferenceComponent(
                        id = componentId,
                        sampleIndices = first until samples.size,
                        approximateLength = polylineLength(points).coerceAtLeast(points.size.toFloat())
                    )
                }
            }
            return WritingReference(
                width = width,
                height = height,
                samples = samples,
                components = metadata,
                contentBounds = bounds(samples.map { it.point })
            )
        }
    }
}

data class WritingScoringConfig(
    val softSlackFraction: Float = 0.030f,
    val hardSlackMultiplier: Float = 1.5f,
    val userRadiusFraction: Float = 0.0125f,
    val maxAlignmentFraction: Float = 0.025f,
    val componentWeight: Float = 0.25f,
    val coverageBeta: Float = 1.2f,
    val tangentSigmaRadians: Float = 0.61f,
    val tangentWeight: Float = 0.12f,
    val resampleSpacingFraction: Float = 0.014f
)

data class WritingMatchResult(
    val score: Int,
    val coverage: Float,
    val precision: Float,
    val missingComponents: Int,
    val geometry: Float,
    val tangent: Float,
    val hardCoverage: Float,
    val componentCoverage: List<Float>,
    val pathLengthRatio: Float,
    val rawScore: Float,
    val scoreVersion: Int = SCORE_VERSION
)

/** Fixed-coordinate, centre-line/corridor handwriting quality scorer. */
object WritingMatcher {
    fun score(
        reference: WritingReference,
        strokes: List<List<WritingPoint>>,
        config: WritingScoringConfig = WritingScoringConfig()
    ): WritingMatchResult {
        val validStrokes = strokes.filter { it.size >= 2 }
        if (reference.samples.size < 2 || validStrokes.isEmpty()) return emptyResult(reference.components.size)

        val height = reference.height.toFloat().coerceAtLeast(1f)
        val spacing = height * config.resampleSpacingFraction
        val smoothed = validStrokes.map(::smoothStroke).map { resample(it, spacing) }
        val userBounds = bounds(smoothed.flatten())
        val maxShift = height * config.maxAlignmentFraction
        val desiredShift = reference.contentBounds.center - userBounds.center
        val alignment = WritingPoint(
            desiredShift.x.coerceIn(-maxShift, maxShift),
            desiredShift.y.coerceIn(-maxShift, maxShift)
        )
        val candidates = smoothed.flatMapIndexed { strokeId, stroke ->
            stroke.mapIndexed { index, point ->
                val previous = stroke[(index - 1).coerceAtLeast(0)]
                val next = stroke[(index + 1).coerceAtMost(stroke.lastIndex)]
                CandidateSample(point + alignment, normalized(next - previous), strokeId)
            }
        }
        if (candidates.size < 2) return emptyResult(reference.components.size)

        val slack = height * config.softSlackFraction
        val hardSlack = slack * config.hardSlackMultiplier
        val userRadius = height * config.userRadiusFraction

        var precisionSum = 0f
        candidates.forEach { candidate ->
            val nearest = nearestReference(candidate.point, reference.samples)
            val outside = max(0f, sqrt(nearest.distanceSquared) - nearest.sample.radius - userRadius)
            precisionSum += gaussian(outside, slack)
        }
        val precision = precisionSum / candidates.size

        val componentSoftSums = FloatArray(reference.components.size)
        val componentHardSums = FloatArray(reference.components.size)
        val componentCounts = IntArray(reference.components.size)
        var lengthSoftSum = 0f
        var lengthHardSum = 0f
        var tangentSum = 0f
        var tangentWeightSum = 0f

        reference.samples.forEach { target ->
            val nearest = nearestCandidate(target.point, candidates)
            val distance = sqrt(nearest.distanceSquared)
            val outside = max(0f, distance - target.radius - userRadius)
            val similarity = gaussian(outside, slack)
            val hard = if (outside <= hardSlack) 1f else 0f
            lengthSoftSum += similarity
            lengthHardSum += hard
            if (target.componentId in componentCounts.indices) {
                componentSoftSums[target.componentId] += similarity
                componentHardSums[target.componentId] += hard
                componentCounts[target.componentId]++
            }
            if (similarity > 0.30f && target.tangent.lengthSquared() > 0.2f) {
                val orientation = tangentSimilarity(target.tangent, nearest.sample.tangent, config.tangentSigmaRadians)
                tangentSum += orientation * similarity
                tangentWeightSum += similarity
            }
        }

        val lengthRecall = lengthSoftSum / reference.samples.size
        val lengthHard = lengthHardSum / reference.samples.size
        val componentSoft = componentCounts.indices.map { index ->
            if (componentCounts[index] == 0) 0f else componentSoftSums[index] / componentCounts[index]
        }
        val componentHard = componentCounts.indices.map { index ->
            if (componentCounts[index] == 0) 0f else componentHardSums[index] / componentCounts[index]
        }
        val componentRecall = componentSoft.averageFloat()
        val componentHardCoverage = componentHard.averageFloat()
        val componentWeight = config.componentWeight.coerceIn(0f, 0.5f)
        val recall = lengthRecall * (1f - componentWeight) + componentRecall * componentWeight
        val hardCoverage = lengthHard * (1f - componentWeight) + componentHardCoverage * componentWeight
        val fScore = fBeta(precision, recall, config.coverageBeta)
        val geometry = (0.65f * fScore + 0.35f * hardCoverage).coerceIn(0f, 1f)
        val tangent = if (tangentWeightSum > 0.001f) tangentSum / tangentWeightSum else geometry

        val userLength = smoothed.sumOf { polylineLength(it).toDouble() }.toFloat()
        val referenceLength = reference.components.sumOf { it.approximateLength.toDouble() }.toFloat().coerceAtLeast(1f)
        val lengthRatio = userLength / referenceLength
        val lengthSanity = when {
            lengthRatio < 0.38f -> (lengthRatio / 0.38f).coerceIn(0f, 1f)
            lengthRatio > 2.8f -> (2.8f / lengthRatio).coerceIn(0f, 1f)
            else -> 1f
        }
        val tangentWeight = config.tangentWeight.coerceIn(0f, 0.25f)
        val shapeScore = geometry * (1f - tangentWeight) + tangent * tangentWeight
        val raw = (shapeScore * (0.72f + 0.28f * lengthSanity)).coerceIn(0f, 1f)
        val missing = componentSoft.count { it < 0.30f }

        return WritingMatchResult(
            score = (raw * 100f).roundToInt().coerceIn(0, 100),
            coverage = recall,
            precision = precision,
            missingComponents = missing,
            geometry = geometry,
            tangent = tangent,
            hardCoverage = hardCoverage,
            componentCoverage = componentSoft,
            pathLengthRatio = lengthRatio,
            rawScore = raw
        )
    }

    private data class CandidateSample(val point: WritingPoint, val tangent: WritingPoint, val strokeId: Int)
    private data class ReferenceNearest(val sample: WritingReferenceSample, val distanceSquared: Float)
    private data class CandidateNearest(val sample: CandidateSample, val distanceSquared: Float)

    private fun nearestReference(point: WritingPoint, samples: List<WritingReferenceSample>): ReferenceNearest {
        var best = samples.first()
        var bestDistance = Float.POSITIVE_INFINITY
        samples.forEach { sample ->
            val distance = squaredDistance(point, sample.point)
            if (distance < bestDistance) {
                best = sample
                bestDistance = distance
            }
        }
        return ReferenceNearest(best, bestDistance)
    }

    private fun nearestCandidate(point: WritingPoint, samples: List<CandidateSample>): CandidateNearest {
        var best = samples.first()
        var bestDistance = Float.POSITIVE_INFINITY
        samples.forEach { sample ->
            val distance = squaredDistance(point, sample.point)
            if (distance < bestDistance) {
                best = sample
                bestDistance = distance
            }
        }
        return CandidateNearest(best, bestDistance)
    }

    private fun smoothStroke(points: List<WritingPoint>): List<WritingPoint> {
        if (points.size < 3) return points
        return points.mapIndexed { index, point ->
            if (index == 0 || index == points.lastIndex) point else {
                val previous = points[index - 1]
                val next = points[index + 1]
                WritingPoint(
                    x = previous.x * 0.2f + point.x * 0.6f + next.x * 0.2f,
                    y = previous.y * 0.2f + point.y * 0.6f + next.y * 0.2f
                )
            }
        }
    }

    private fun resample(points: List<WritingPoint>, spacing: Float): List<WritingPoint> {
        if (points.size < 2) return points
        val output = mutableListOf(points.first())
        points.zipWithNext().forEach { (start, end) ->
            val distance = sqrt(squaredDistance(start, end))
            val count = max(1, ceil(distance / spacing.coerceAtLeast(0.5f)).toInt())
            repeat(count) { index ->
                val t = (index + 1f) / count
                output += WritingPoint(start.x + (end.x - start.x) * t, start.y + (end.y - start.y) * t)
            }
        }
        return if (output.size <= 320) output else {
            val step = ceil(output.size / 320f).toInt()
            output.filterIndexed { index, _ -> index % step == 0 || index == output.lastIndex }
        }
    }

    private fun gaussian(outsideDistance: Float, slack: Float): Float {
        if (outsideDistance <= 0f) return 1f
        val z = outsideDistance / slack.coerceAtLeast(0.001f)
        return exp(-0.5f * z * z).coerceIn(0f, 1f)
    }

    private fun fBeta(precision: Float, recall: Float, beta: Float): Float {
        val betaSquared = beta * beta
        return ((1f + betaSquared) * precision * recall /
            (betaSquared * precision + recall + 0.000001f)).coerceIn(0f, 1f)
    }

    private fun tangentSimilarity(first: WritingPoint, second: WritingPoint, sigma: Float): Float {
        val dot = abs(first.x * second.x + first.y * second.y).coerceIn(0f, 1f)
        val angle = acos(dot)
        val z = angle / sigma.coerceAtLeast(0.01f)
        return exp(-0.5f * z * z)
    }

    private fun emptyResult(componentCount: Int) = WritingMatchResult(
        score = 0,
        coverage = 0f,
        precision = 0f,
        missingComponents = componentCount,
        geometry = 0f,
        tangent = 0f,
        hardCoverage = 0f,
        componentCoverage = List(componentCount) { 0f },
        pathLengthRatio = 0f,
        rawScore = 0f
    )
}

private fun bounds(points: List<WritingPoint>): WritingBounds {
    if (points.isEmpty()) return WritingBounds(0f, 0f, 1f, 1f)
    var minX = Float.POSITIVE_INFINITY
    var minY = Float.POSITIVE_INFINITY
    var maxX = Float.NEGATIVE_INFINITY
    var maxY = Float.NEGATIVE_INFINITY
    points.forEach { point ->
        minX = min(minX, point.x)
        minY = min(minY, point.y)
        maxX = max(maxX, point.x)
        maxY = max(maxY, point.y)
    }
    return WritingBounds(minX, minY, maxX, maxY)
}

private fun normalized(point: WritingPoint): WritingPoint {
    val length = sqrt(point.x * point.x + point.y * point.y)
    return if (length < 0.0001f) WritingPoint(0f, 0f) else WritingPoint(point.x / length, point.y / length)
}

private fun squaredDistance(first: WritingPoint, second: WritingPoint): Float {
    val dx = first.x - second.x
    val dy = first.y - second.y
    return dx * dx + dy * dy
}

private fun WritingPoint.lengthSquared(): Float = x * x + y * y

private fun polylineLength(points: List<WritingPoint>): Float = points.zipWithNext().sumOf { (first, second) ->
    sqrt(squaredDistance(first, second)).toDouble()
}.toFloat()

private fun Iterable<Float>.averageFloat(): Float {
    var total = 0f
    var count = 0
    forEach { value -> total += value; count++ }
    return if (count == 0) 0f else total / count
}

private const val SCORE_VERSION = 3
