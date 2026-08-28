package com.dino.sufara.feature.lesson.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuizParserTest {
    @Test
    fun parsesIndentedAnswersAndStableQuestionIds() {
        val source = listOf(
            "1. Prvo pitanje?",
            "\t1. tačno",
            "\t2. netačno",
            "2. Drugo pitanje?",
            "    1. prvi odgovor",
            "    2. drugi odgovor"
        ).joinToString("\n")

        val quizzes = QuizParser.parse("014", source)

        assertEquals(listOf("014_1", "014_2"), quizzes.map { it.id })
        assertEquals("Prvo pitanje?", quizzes.first().question)
        assertEquals(listOf("tačno", "netačno"), quizzes.first().answers)
        assertEquals("tačno", quizzes.first().correctAnswer)
    }

    @Test
    fun ignoresIncompleteQuestionsAndOrphanAnswers() {
        val source = listOf(
            "\t1. odgovor bez pitanja",
            "1. Pitanje bez odgovora",
            "2. Ispravno pitanje",
            "\t1. odgovor"
        ).joinToString("\n")

        val quizzes = QuizParser.parse("001", source)

        assertEquals(1, quizzes.size)
        assertEquals("Ispravno pitanje", quizzes.single().question)
        assertTrue(quizzes.single().answers.isNotEmpty())
    }
}
