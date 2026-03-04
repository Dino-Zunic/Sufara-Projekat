package com.dino.sufara.feature.lesson.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SufaraDao {
    @Query("SELECT * FROM lesson_progress")
    suspend fun getAllLessonProgress(): List<LessonProgressEntity>

    @Query("SELECT * FROM lesson_progress")
    fun getAllLessonProgressFlow(): Flow<List<LessonProgressEntity>>

    @Query("SELECT * FROM lesson_progress WHERE lessonId = :id LIMIT 1")
    suspend fun getLessonProgress(id: String): LessonProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateLessonProgress(progress: LessonProgressEntity)
    
    @Query("SELECT * FROM quiz_progress WHERE lessonId = :lessonId")
    suspend fun getQuizProgressForLesson(lessonId: String): List<QuizProgressEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateQuizProgress(progress: QuizProgressEntity)
}