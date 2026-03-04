package com.dino.sufara.feature.lesson.domain.model

sealed class LessonStep {
    data class Theory(val title: String, val text: String, val dodatakText: String? = null) : LessonStep()
    data class ImageInfo(val imagePath: String) : LessonStep()
    data class Quiz(val id: String, val question: String, val answers: List<String>, val correctAnswer: String) : LessonStep()
    data class Example(val text: String) : LessonStep()
}