package com.dino.sufara.feature.lesson.presentation.grid

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.hypot

class CourseMapGeneratorTest {
    private val geometry = CourseMapGenerator.generate(CourseMapConfig(lessonCount = 100))

    @Test
    fun generatedRouteUsesFourRegionsWithoutCrossings() {
        assertEquals(4, geometry.regions.size)
        assertEquals(0, CourseMapGenerator.countCrossings(geometry.sampledPath))
    }

    @Test
    fun lessonsAreEvenlyDistributedByArcLength() {
        val distances = geometry.lessons.zipWithNext { first, second ->
            hypot(second.x - first.x, second.y - first.y)
        }
        val average = distances.average().toFloat()
        assertTrue("Unexpected average spacing: $average", average in 220f..300f)
        assertTrue("Spacing varies too much: ${distances.minOrNull()}..${distances.maxOrNull()}",
            distances.minOrNull()!! > average * 0.58f && distances.maxOrNull()!! < average * 1.18f)
    }

    @Test
    fun portraitOrientationStartsPredominantlyDownward() {
        val first = geometry.lessons.first()
        val fifth = geometry.lessons[4]
        assertTrue(fifth.y - first.y > kotlin.math.abs(fifth.x - first.x) * 2f)
    }

    @Test
    fun geometryIsDeterministicAndPadded() {
        val copy = CourseMapGenerator.generate(CourseMapConfig(lessonCount = 100))
        assertEquals(geometry.lessons, copy.lessons)
        assertTrue(geometry.lessons.all { it.x > geometry.lessonRadius && it.y > geometry.lessonRadius })
        assertTrue(geometry.lessons.all { it.x < geometry.width && it.y < geometry.height })
    }
}
