package com.dino.sufara.feature.lesson.domain.util

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class AnswerOrderTest {
    @Test
    fun reshufflePreservesEveryAnswer() {
        val answers = listOf("a", "b", "c", "d")

        val result = reshuffleAnswers(answers, random = Random(12))

        assertEquals(answers.sorted(), result.sorted())
    }

    @Test
    fun reshuffleNeverRepeatsPreviousOrderWhenAlternativesExist() {
        val answers = listOf("a", "b", "c", "d")

        val result = reshuffleAnswers(answers, previous = answers, random = Random(0))

        assertNotEquals(answers, result)
        assertEquals(answers.sorted(), result.sorted())
    }

    @Test
    fun singleAnswerRemainsStable() {
        assertEquals(listOf("only"), reshuffleAnswers(listOf("only"), listOf("only")))
    }
}
