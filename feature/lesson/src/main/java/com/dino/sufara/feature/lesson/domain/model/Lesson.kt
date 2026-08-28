package com.dino.sufara.feature.lesson.domain.model

enum class LessonStatus { LOCKED, UNLOCKED, COMPLETED }

data class Lesson(
    val id: String,
    /** Zero-based position after sorting source folders; source [id] stays stable for Room. */
    val ordinal: Int,
    val title: String,
    val symbol: String,
    val status: LessonStatus,
    val steps: List<LessonStep>
)
