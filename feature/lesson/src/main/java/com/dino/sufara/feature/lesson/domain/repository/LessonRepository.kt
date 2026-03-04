package com.dino.sufara.feature.lesson.domain.repository

import com.dino.sufara.feature.lesson.domain.model.Lesson

interface LessonRepository {
    suspend fun getAllLessons(): List<Lesson>
    suspend fun getLessonById(id: String): Lesson?
    suspend fun getFunFacts(): List<String>
    suspend fun completeLessonAndUnlockNext(currentLessonId: String)
    suspend fun unlockAllLessons()
}