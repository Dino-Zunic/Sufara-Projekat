package com.dino.sufara.feature.lesson.domain.model

data class Lesson(
    val id: String,
    val title: String,
    val symbol: String,
    val steps: List<LessonStep>
)