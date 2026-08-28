package com.dino.sufara.feature.lesson.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [LessonProgressEntity::class, QuizProgressEntity::class, WritingProgressEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract val dao: SufaraDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS writing_progress (
                        lessonId TEXT NOT NULL,
                        status TEXT NOT NULL,
                        completedAt INTEGER,
                        PRIMARY KEY(lessonId)
                    )
                    """.trimIndent()
                )
            }
        }
    }
}
