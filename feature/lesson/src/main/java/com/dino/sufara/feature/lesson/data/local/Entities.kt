package com.dino.sufara.feature.lesson.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dino.sufara.feature.lesson.domain.model.LessonStatus

@Entity(tableName = "lesson_progress")
data class LessonProgressEntity(
    @PrimaryKey val lessonId: String,
    val status: LessonStatus = LessonStatus.LOCKED,
    val completedAt: Long? = null
)

@Entity(tableName = "quiz_progress")
data class QuizProgressEntity(
    @PrimaryKey val questionId: String, 
    val lessonId: String,
    val nextReviewDate: Long = 0L, 
    val intervalDays: Int = 0,
    val consecutiveCorrectAnswers: Int = 0,
    val easinessFactor: Float = 2.5f 
)