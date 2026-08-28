package com.dino.sufara.feature.lesson.data.repository

import com.dino.sufara.feature.lesson.domain.model.LessonStep

internal object QuizParser {
    private val questionPattern = Regex("^(\\d+)\\.\\s*(.+)$")
    private val answerPattern = Regex("^\\s+(\\d+)\\.\\s*(.+)$")

    fun parse(lessonId: String, text: String): List<LessonStep.Quiz> {
        val quizzes = mutableListOf<LessonStep.Quiz>()
        var question = ""
        var answers = mutableListOf<String>()

        fun flushQuestion() {
            if (question.isNotBlank() && answers.isNotEmpty()) {
                val questionNumber = quizzes.size + 1
                quizzes += LessonStep.Quiz(
                    id = "${lessonId}_$questionNumber",
                    question = question,
                    answers = answers.toList(),
                    correctAnswer = answers.first()
                )
            }
        }

        text.lineSequence().forEach { line ->
            when {
                answerPattern.matches(line) -> {
                    val answer = answerPattern.matchEntire(line)?.groupValues?.get(2).orEmpty().trim()
                    if (question.isNotBlank() && answer.isNotBlank()) answers += answer
                }
                questionPattern.matches(line) -> {
                    flushQuestion()
                    question = questionPattern.matchEntire(line)?.groupValues?.get(2).orEmpty().trim()
                    answers = mutableListOf()
                }
            }
        }
        flushQuestion()

        return quizzes
    }
}
