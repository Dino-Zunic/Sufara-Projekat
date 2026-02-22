package com.dino.sufara.feature.lesson.data.repository

import android.content.Context
import com.dino.sufara.feature.lesson.domain.model.Lesson
import com.dino.sufara.feature.lesson.domain.repository.LessonRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalAssetLessonRepository(
    private val context: Context
) : LessonRepository {

    override suspend fun getAllLessons(): List<Lesson> = withContext(Dispatchers.IO) {
        val assetManager = context.assets
        val lessonFiles = assetManager.list("lekcije") ?: emptyArray()
        
        lessonFiles.sorted().mapNotNull { fileName ->
            parseLessonFile(fileName, assetManager)
        }
    }

    override suspend fun getLessonById(id: String): Lesson? = withContext(Dispatchers.IO) {
        getAllLessons().find { it.id == id }
    }

    private fun parseLessonFile(fileName: String, assetManager: android.content.res.AssetManager): Lesson? {
        return try {
            val nameWithoutExtension = fileName.removeSuffix(".md")
            val parts = nameWithoutExtension.split(" ", limit = 2)
            if (parts.size < 2) return null

            val id = parts[0]
            val title = parts[1]

            val examples = assetManager.open("lekcije/$fileName")
                .bufferedReader()
                .readLines()
                .filter { it.isNotBlank() }

            Lesson(id = id, title = title, examples = examples)
        } catch (e: Exception) {
            null
        }
    }
}