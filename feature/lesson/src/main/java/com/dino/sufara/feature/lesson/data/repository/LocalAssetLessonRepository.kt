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
import com.dino.sufara.feature.lesson.data.local.QuizProgressEntity

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

    override suspend fun completeLessonAndUnlockNext(currentLessonId: String) {
        withContext(Dispatchers.IO) {
            // 1. Означи тренутну лекцију као завршену
            dao.updateLessonProgress(LessonProgressEntity(currentLessonId, LessonStatus.COMPLETED, System.currentTimeMillis()))
            
            // 2. Пронађи следећу лекцију и откључај је (ако већ није откључана/завршена)
            val allLessons = getAllLessons()
            val currentIndex = allLessons.indexOfFirst { it.id == currentLessonId }
            if (currentIndex != -1 && currentIndex < allLessons.size - 1) {
                val nextLesson = allLessons[currentIndex + 1]
                if (nextLesson.status == LessonStatus.LOCKED) {
                    dao.updateLessonProgress(LessonProgressEntity(nextLesson.id, LessonStatus.UNLOCKED))
                }
            }
        }
    }

    override suspend fun unlockAllLessons() {
        withContext(Dispatchers.IO) {
            val allLessons = getAllLessons()
            allLessons.forEach { lesson ->
                dao.updateLessonProgress(LessonProgressEntity(lesson.id, LessonStatus.COMPLETED, System.currentTimeMillis()))
            }
        }
    }

    override suspend fun getDueQuizzes(): List<Pair<String, LessonStep.Quiz>> = withContext(Dispatchers.IO) {
        // 1. Nalazimo koje su lekcije završene
        val completedLessonIds = dao.getAllLessonProgress().filter { it.status == LessonStatus.COMPLETED }.map { it.lessonId }
        val allLessons = getAllLessons().filter { it.id in completedLessonIds }

        // 2. Izvlačimo sve kvizove iz tih lekcija
        val allUnlockedQuizzes = allLessons.flatMap { lesson ->
            lesson.steps.filterIsInstance<LessonStep.Quiz>().map { quiz -> Pair(lesson.id, quiz) }
        }

        val currentTime = System.currentTimeMillis()
        val dueQuizzes = mutableListOf<Pair<String, LessonStep.Quiz>>()

        // 3. Filtriramo one kojima je vreme za ponavljanje prošlo (ili su novi)
        for (item in allUnlockedQuizzes) {
            val progress = dao.getQuizProgress(item.second.id)
            if (progress == null || progress.nextReviewDate <= currentTime) {
                dueQuizzes.add(item)
            }
        }
        
        // Vraćamo ih promešane da korisnik ne bi učio po redosledu lekcija!
        return@withContext dueQuizzes.shuffled()
    }

    override suspend fun submitQuizAnswer(questionId: String, lessonId: String, isCorrect: Boolean) {
        withContext(Dispatchers.IO) {
            val currentProgress = dao.getQuizProgress(questionId) ?: QuizProgressEntity(questionId, lessonId)

            var consecutive = currentProgress.consecutiveCorrectAnswers
            var ef = currentProgress.easinessFactor
            var interval = currentProgress.intervalDays

            if (isCorrect) {
                consecutive++
                // Anki SM-2 formula za uspešan odgovor
                interval = when (consecutive) {
                    1 -> 1
                    2 -> 6
                    else -> (interval * ef).toInt()
                }
                // Blago nagrađujemo EF ako zna dobro
                ef = minOf(2.5f, ef + 0.05f) 
            } else {
                consecutive = 0
                interval = 1 // Vraćamo ga na početak
                ef = maxOf(1.3f, ef - 0.2f) // Kažnjavamo EF jer je teško pitanje (ne ide ispod 1.3)
            }

            // Računamo sledeće vreme u milisekundama (interval u danima * 24h * 60m * 60s * 1000ms)
            val nextReview = System.currentTimeMillis() + interval * 86400000L

            com.dino.sufara.feature.lesson.domain.util.SufaraLogger.log(
                "ANKI DEBUG: Pitanje [$questionId] | Tačno: $isCorrect | Zaredom: $consecutive | EF: $ef | Interval: $interval dana"
            )

            dao.updateQuizProgress(
                QuizProgressEntity(questionId, lessonId, nextReview, interval, consecutive, ef)
            )
        }
    }
}