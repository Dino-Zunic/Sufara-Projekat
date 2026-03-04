package com.dino.sufara.feature.lesson.domain.repository

import com.dino.sufara.feature.lesson.domain.model.Lesson
import com.dino.sufara.feature.lesson.domain.model.LessonStep

interface LessonRepository {
    suspend fun getAllLessons(): List<Lesson>
    suspend fun getLessonById(id: String): Lesson?
    suspend fun getFunFacts(): List<String>
    suspend fun completeLessonAndUnlockNext(currentLessonId: String)
    suspend fun unlockAllLessons()
    suspend fun getDueQuizzes(): List<Pair<String, LessonStep.Quiz>>
    suspend fun submitQuizAnswer(questionId: String, lessonId: String, isCorrect: Boolean)
}