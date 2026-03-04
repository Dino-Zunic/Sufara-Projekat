package com.dino.sufara.feature.lesson.data.repository

import android.content.Context
import com.dino.sufara.feature.lesson.data.local.LessonProgressEntity
import com.dino.sufara.feature.lesson.data.local.SufaraDao
import com.dino.sufara.feature.lesson.domain.model.Lesson
import com.dino.sufara.feature.lesson.domain.model.LessonStatus
import com.dino.sufara.feature.lesson.domain.model.LessonStep
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException

class LocalAssetLessonRepository(
    private val context: Context,
    private val dao: SufaraDao 
) : com.dino.sufara.feature.lesson.domain.repository.LessonRepository {

    private fun getSymbols(): List<String> {
        return try {
            context.assets.open("lekcije/симболи.md").bufferedReader().use { it.readText() }
        } catch (e: Exception) { "" }.lines().filter { it.isNotBlank() }
    }

    override suspend fun getAllLessons(): List<Lesson> = withContext(Dispatchers.IO) {
        val allAssets = context.assets.list("lekcije") ?: emptyArray()
        val symbols = getSymbols()
        val validFolders = allAssets.filter { it.firstOrNull()?.isDigit() == true }.sorted()

        val progressList = dao.getAllLessonProgress()
        val progressMap = progressList.associateBy { it.lessonId }

        validFolders.mapIndexedNotNull { index, folderName ->
            val idPart = folderName.substringBefore(" ")
            val rawSymbol = symbols.getOrNull(index)?.trim() ?: "."
            val finalSymbol = if (rawSymbol == ".") "📖" else rawSymbol
            
            var status = progressMap[idPart]?.status
            if (status == null) {
                status = if (idPart == "001") LessonStatus.UNLOCKED else LessonStatus.LOCKED
                dao.updateLessonProgress(LessonProgressEntity(lessonId = idPart, status = status))
            }

            parseLessonFolder(folderName, finalSymbol, status)
        }
    }

    override suspend fun getLessonById(id: String): Lesson? = withContext(Dispatchers.IO) {
        val numericId = id.substringBefore(" ") 
        getAllLessons().find { it.id == numericId }
    }

    private suspend fun parseLessonFolder(folderName: String, symbol: String, status: LessonStatus): Lesson? = withContext(Dispatchers.IO) {
        try {
            val basePath = "lekcije/$folderName"
            val steps = mutableListOf<LessonStep>()
            val idPart = folderName.substringBefore(" ")
            val titlePart = folderName.substringAfter(" ")

            val lekcijaText = readFile(basePath, "лекција.md")
            val dodatakText = readFile(basePath, "додатак.md")

            if (lekcijaText != null) {
                steps.add(LessonStep.Theory("Теорија", lekcijaText, dodatakText))
                if (context.assets.list(basePath)?.contains("исходиште.png") == true) {
                    steps.add(LessonStep.ImageInfo("file:///android_asset/$basePath/исходиште.png"))
                }
            }

            val kvizText = readFile(basePath, "квиз.md")
            if (kvizText != null) {
                steps.addAll(parseQuiz(idPart, kvizText)) 
            }

            val primeriText = readFile(basePath, "примери.md")
            if (primeriText != null) {
                primeriText.lines().filter { it.isNotBlank() }.forEach { line ->
                    steps.add(LessonStep.Example(line.trim()))
                }
            }

            Lesson(id = idPart, title = titlePart, symbol = symbol, status = status, steps = steps)
        } catch (e: Exception) {
            null
        }
    }

    private fun readFile(folder: String, fileName: String): String? {
        return try {
            context.assets.open("$folder/$fileName").bufferedReader().use { it.readText() }
        } catch (e: FileNotFoundException) { null }
    }

    private fun parseQuiz(lessonId: String, text: String): List<LessonStep.Quiz> {
        val quizzes = mutableListOf<LessonStep.Quiz>()
        var currentQuestion = ""
        var currentAnswers = mutableListOf<String>()
        var questionIndex = 1 

        text.lines().forEach { line ->
            if (line.isNotBlank()) {
                if (line.trim().first().isDigit() && !line.startsWith(" ") && !line.startsWith("\t")) {
                    if (currentQuestion.isNotEmpty() && currentAnswers.isNotEmpty()) {
                        quizzes.add(LessonStep.Quiz("${lessonId}_$questionIndex", currentQuestion, currentAnswers, currentAnswers.first()))
                        questionIndex++
                    }
                    currentQuestion = line.substringAfter(".").trim()
                    currentAnswers = mutableListOf()
                } else if (line.trim().firstOrNull()?.isDigit() == true) {
                    currentAnswers.add(line.substringAfter(".").trim())
                }
            }
        }
        if (currentQuestion.isNotEmpty() && currentAnswers.isNotEmpty()) {
            quizzes.add(LessonStep.Quiz("${lessonId}_$questionIndex", currentQuestion, currentAnswers, currentAnswers.first()))
        }
        return quizzes
    }

    override suspend fun getFunFacts(): List<String> = withContext(Dispatchers.IO) {
        try {
            context.assets.open("lekcije/cinjenice.md").bufferedReader().readLines().filter { it.isNotBlank() }
        } catch (e: Exception) {
            listOf("Учи у име Господара твога који ствара...") 
        }
    }
}