package com.dino.sufara.feature.lesson.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class WritingMatcherTest {
    private val body = (0..60).map { index ->
        val t = index / 60f
        WritingPoint(24f + 104f * t, 112f - sin(t * PI).toFloat() * 58f)
    }
    private val mark = (0..16).map { index ->
        val angle = index / 16f * PI.toFloat() * 2f
        WritingPoint(104f + cos(angle) * 7f, 35f + sin(angle) * 5f)
    }
    private val reference = WritingReference.fromComponents(
        width = 160,
        height = 160,
        components = listOf(body, mark),
        radius = 4f
    )

    @Test
    fun faithfulShapePassesAndReportsBothComponents() {
        val result = WritingMatcher.score(reference, listOf(body, mark))
        assertTrue("Faithful guide trace should pass: $result", result.score >= 88)
        assertEquals(0, result.missingComponents)
        assertEquals(3, result.scoreVersion)
    }

    @Test
    fun smallParallelOffsetAndJitterRemainTolerated() {
        val shifted = body.mapIndexed { index, point ->
            WritingPoint(point.x + 6f, point.y + 4f + if (index % 2 == 0) 1.2f else -1.2f)
        }
        val shiftedMark = mark.map { WritingPoint(it.x + 6f, it.y + 4f) }
        val result = WritingMatcher.score(reference, listOf(shifted, shiftedMark))
        assertTrue("Small touch-screen error should be tolerated: $result", result.score >= 78)
    }

    @Test
    fun shortStrokeInsideGuideCannotPassOnPrecisionAlone() {
        val short = body.subList(20, 36)
        val result = WritingMatcher.score(reference, listOf(short))
        assertTrue("Partial in-corridor stroke must fail: $result", result.score < 58)
        assertTrue(result.coverage < result.precision)
    }

    @Test
    fun unrelatedLineThroughShapeIsRejected() {
        val line = (0..60).map { WritingPoint(15f + it * 2.2f, 82f) }
        val result = WritingMatcher.score(reference, listOf(line))
        assertTrue("A crossing line is not the target shape: $result", result.score < 50)
    }

    @Test
    fun missingSeparateMarkIsWorseThanAImperfectMark() {
        val missing = WritingMatcher.score(reference, listOf(body))
        val imperfectMark = mark.map { WritingPoint(it.x + 5f, it.y + 3f) }
        val imperfect = WritingMatcher.score(reference, listOf(body, imperfectMark))
        assertTrue("Omitted component should be detected: $missing", missing.missingComponents >= 1)
        assertTrue("Drawing the mark imperfectly should beat omitting it: $missing vs $imperfect",
            imperfect.score >= missing.score + 6)
    }

    @Test
    fun largeRelocationOrScalingDoesNotGetNormalizedAway() {
        val relocated = (body + mark).map { WritingPoint(it.x + 26f, it.y + 22f) }
        val scaled = (body + mark).map { WritingPoint(80f + (it.x - 80f) * 1.35f, 80f + (it.y - 80f) * 1.35f) }
        val exact = WritingMatcher.score(reference, listOf(body, mark)).score
        assertTrue(WritingMatcher.score(reference, listOf(relocated)).score < exact - 22)
        assertTrue(WritingMatcher.score(reference, listOf(scaled)).score < exact - 15)
    }

    @Test
    fun extraScribbleLowersPrecisionAndScore() {
        val scribble = (0..50).map { index ->
            WritingPoint(12f + index * 2.7f, if (index % 2 == 0) 145f else 12f)
        }
        val exact = WritingMatcher.score(reference, listOf(body, mark))
        val withScribble = WritingMatcher.score(reference, listOf(body, mark, scribble))
        assertTrue(withScribble.precision < exact.precision - 0.15f)
        assertTrue(withScribble.score < exact.score - 10)
    }

    @Test
    fun reverseStrokeDirectionIsNotPenalized() {
        val forward = WritingMatcher.score(reference, listOf(body, mark)).score
        val reverse = WritingMatcher.score(reference, listOf(body.reversed(), mark.reversed())).score
        assertTrue("Authored stroke order is unavailable, so direction must be invariant", kotlin.math.abs(forward - reverse) <= 2)
    }
}
