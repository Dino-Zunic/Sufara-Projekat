package com.dino.sufara.feature.lesson.data.repository

import com.dino.sufara.feature.lesson.data.local.QuizProgressEntity

internal data class ReviewSchedule(
    val nextReviewDate: Long,
    val intervalDays: Int,
    val consecutiveCorrectAnswers: Int,
    val easinessFactor: Float
)

/** Pure scheduling logic shared by persistence and unit tests. */
internal object ReviewScheduler {
    private const val DAY_MILLIS = 86_400_000L

    fun next(
        current: QuizProgressEntity,
        isCorrect: Boolean,
        nowMillis: Long
    ): ReviewSchedule {
        if (!isCorrect) {
            return ReviewSchedule(
                nextReviewDate = nowMillis,
                intervalDays = 1,
                consecutiveCorrectAnswers = 0,
                easinessFactor = (current.easinessFactor - 0.2f).coerceAtLeast(1.3f)
            )
        }

        val consecutive = current.consecutiveCorrectAnswers + 1
        val interval = when (consecutive) {
            1 -> 1
            2 -> 6
            else -> (current.intervalDays * current.easinessFactor).toInt().coerceAtLeast(1)
        }
        return ReviewSchedule(
            nextReviewDate = nowMillis + interval * DAY_MILLIS,
            intervalDays = interval,
            consecutiveCorrectAnswers = consecutive,
            easinessFactor = (current.easinessFactor + 0.05f).coerceAtMost(2.5f)
        )
    }
}
