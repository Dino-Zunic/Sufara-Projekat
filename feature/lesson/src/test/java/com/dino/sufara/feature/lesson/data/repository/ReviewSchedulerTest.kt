package com.dino.sufara.feature.lesson.data.repository

import com.dino.sufara.feature.lesson.data.local.QuizProgressEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewSchedulerTest {
    private val now = 1_000_000L

    @Test
    fun wrongAnswerIsDueImmediatelyAndResetsStreak() {
        val current = progress(interval = 14, consecutive = 4, easiness = 1.4f)

        val next = ReviewScheduler.next(current, isCorrect = false, nowMillis = now)

        assertEquals(now, next.nextReviewDate)
        assertEquals(1, next.intervalDays)
        assertEquals(0, next.consecutiveCorrectAnswers)
        assertEquals(1.3f, next.easinessFactor, 0.0001f)
    }

    @Test
    fun firstTwoCorrectAnswersUseOneAndSixDayIntervals() {
        val first = ReviewScheduler.next(progress(), isCorrect = true, nowMillis = now)
        val second = ReviewScheduler.next(
            progress(
                interval = first.intervalDays,
                consecutive = first.consecutiveCorrectAnswers,
                easiness = first.easinessFactor
            ),
            isCorrect = true,
            nowMillis = now
        )

        assertEquals(1, first.intervalDays)
        assertEquals(1, first.consecutiveCorrectAnswers)
        assertEquals(6, second.intervalDays)
        assertEquals(2, second.consecutiveCorrectAnswers)
    }

    @Test
    fun laterCorrectAnswerUsesEasinessAndNeverProducesZeroDays() {
        val normal = ReviewScheduler.next(
            progress(interval = 6, consecutive = 2, easiness = 2f),
            isCorrect = true,
            nowMillis = now
        )
        val defensive = ReviewScheduler.next(
            progress(interval = 0, consecutive = 2, easiness = 1.3f),
            isCorrect = true,
            nowMillis = now
        )

        assertEquals(12, normal.intervalDays)
        assertEquals(1, defensive.intervalDays)
        assertTrue(normal.nextReviewDate > now)
        assertTrue(defensive.nextReviewDate > now)
    }

    @Test
    fun easinessFactorStaysInsideSupportedBounds() {
        val rewarded = ReviewScheduler.next(
            progress(easiness = 2.5f),
            isCorrect = true,
            nowMillis = now
        )
        val penalized = ReviewScheduler.next(
            progress(easiness = 1.3f),
            isCorrect = false,
            nowMillis = now
        )

        assertEquals(2.5f, rewarded.easinessFactor, 0.0001f)
        assertEquals(1.3f, penalized.easinessFactor, 0.0001f)
    }

    private fun progress(
        interval: Int = 0,
        consecutive: Int = 0,
        easiness: Float = 2.5f
    ) = QuizProgressEntity(
        questionId = "q",
        lessonId = "001",
        intervalDays = interval,
        consecutiveCorrectAnswers = consecutive,
        easinessFactor = easiness
    )
}
