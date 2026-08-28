package com.dino.sufara.feature.lesson.presentation.anki

import org.junit.Assert.assertEquals
import org.junit.Test

class ReviewSessionQueueTest {
    @Test
    fun `correct answer removes current question`() {
        assertEquals(listOf("b", "c"), ReviewSessionQueue.afterAnswer(listOf("a", "b", "c"), true))
    }

    @Test
    fun `wrong answer returns after two other questions`() {
        assertEquals(
            listOf("b", "c", "a", "d"),
            ReviewSessionQueue.afterAnswer(listOf("a", "b", "c", "d"), false)
        )
    }

    @Test
    fun `wrong answer is not lost in a short queue`() {
        assertEquals(listOf("b", "a"), ReviewSessionQueue.afterAnswer(listOf("a", "b"), false))
        assertEquals(listOf("a"), ReviewSessionQueue.afterAnswer(listOf("a"), false))
    }

    @Test
    fun `empty queue stays empty`() {
        assertEquals(emptyList<String>(), ReviewSessionQueue.afterAnswer(emptyList<String>(), false))
    }
}
