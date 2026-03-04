package com.dino.sufara.feature.lesson.domain.model

enum class LessonStatus { LOCKED, UNLOCKED, COMPLETED }

data class Lesson(
    val id: String,
    val title: String,
    val symbol: String,
    val status: LessonStatus,
    val steps: List<LessonStep>
)