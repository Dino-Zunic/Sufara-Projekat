package com.dino.sufara.feature.lesson.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [LessonProgressEntity::class, QuizProgressEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract val dao: SufaraDao
}