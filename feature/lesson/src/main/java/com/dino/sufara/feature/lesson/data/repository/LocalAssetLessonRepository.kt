package com.dino.sufara.feature.lesson.data.repository

import android.content.Context
import com.dino.sufara.feature.lesson.data.local.LessonProgressEntity
import com.dino.sufara.feature.lesson.data.local.SufaraDao
import com.dino.sufara.feature.lesson.domain.model.Lesson
import com.dino.sufara.feature.lesson.domain.model.LessonStatus
import com.dino.sufara.feature.lesson.domain.model.LessonStep
import com.dino.sufara.feature.lesson.domain.model.MakharijCatalog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.core.content.edit
import java.io.FileNotFoundException
import com.dino.sufara.feature.lesson.data.local.QuizProgressEntity
import com.dino.sufara.feature.lesson.data.local.WritingProgressEntity

class LocalAssetLessonRepository(
    private val context: Context,
    private val dao: SufaraDao 
) : com.dino.sufara.feature.lesson.domain.repository.LessonRepository {

    private val developerPrefs by lazy {
        context.getSharedPreferences("sufara_developer", Context.MODE_PRIVATE)
    }

    private val lessonTemplates: List<Lesson> by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        val allAssets = context.assets.list("lekcije") ?: emptyArray()
        val symbols = getSymbols()
        val lessonFolders = allAssets
            .filter { it.firstOrNull()?.isDigit() == true }
            .sorted()
        if (symbols.size != lessonFolders.size) {
            com.dino.sufara.feature.lesson.domain.util.SufaraLogger.log(
                "LESSON CONTENT: ${lessonFolders.size} fascikli, ali ${symbols.size} simbola."
            )
        }

        lessonFolders
            .mapIndexedNotNull { ordinal, folderName ->
                val rawSymbol = symbols.getOrNull(ordinal)?.trim() ?: "."
                val finalSymbol = if (rawSymbol == ".") "📖" else rawSymbol
                parseLessonFolder(folderName, finalSymbol, ordinal)
            }
    }

    private fun getSymbols(): List<String> {
        return try {
            context.assets.open("lekcije/симболи.md").bufferedReader().use { it.readText() }
        } catch (e: Exception) { "" }.lines().filter { it.isNotBlank() }
    }

    override suspend fun getAllLessons(): List<Lesson> = withContext(Dispatchers.IO) {
        val progressList = dao.getAllLessonProgress()
        val progressMap = progressList.associateBy { it.lessonId }

        lessonTemplates.map { template ->
            var status = progressMap[template.id]?.status
            if (status == null) {
                status = if (template.ordinal == 0) LessonStatus.UNLOCKED else LessonStatus.LOCKED
                dao.updateLessonProgress(LessonProgressEntity(lessonId = template.id, status = status))
            }

            template.copy(status = status)
        }
    }

    override suspend fun getLessonById(id: String): Lesson? = withContext(Dispatchers.IO) {
        val numericId = id.substringBefore(" ") 
        getAllLessons().find { it.id == numericId }
    }

    private fun parseLessonFolder(folderName: String, symbol: String, ordinal: Int): Lesson? {
        return try {
            val basePath = "lekcije/$folderName"
            val steps = mutableListOf<LessonStep>()
            val idPart = folderName.substringBefore(" ")
            val titlePart = folderName.substringAfter(" ")

            val lekcijaText = readFile(basePath, "лекција.md")
            val dodatakText = readFile(basePath, "додатак.md")
            val originImagePath = if (context.assets.list(basePath)?.contains("исходиште.png") == true) {
                "file:///android_asset/$basePath/исходиште.png"
            } else {
                null
            }

            if (lekcijaText != null) {
                steps.add(
                    LessonStep.Theory(
                        title = "Теорија",
                        text = lekcijaText,
                        dodatakText = dodatakText,
                        makharij = MakharijCatalog.forLesson(idPart, symbol),
                        originImagePath = originImagePath
                    )
                )
            }

            val kvizText = readFile(basePath, "квиз.md")
            if (kvizText != null) {
                steps.addAll(QuizParser.parse(idPart, kvizText))
            }

            val primeriText = readFile(basePath, "примери.md")
            val fileExamples = primeriText
                ?.lines()
                ?.map { it.trim() }
                ?.filter { it.isNotBlank() }
                .orEmpty()
            val audioFolderPath = "lekcije/audio/$idPart"
            val audioAssetNames = runCatching {
                context.assets.list(audioFolderPath).orEmpty().toList()
            }.getOrElse { error ->
                com.dino.sufara.feature.lesson.domain.util.SufaraLogger.log(
                    "LESSON AUDIO [$idPart]: fascikla nije mogla da se procita: ${error.message.orEmpty()}"
                )
                emptyList()
            }
            val audioPairing = ExampleAudioPairer.pair(fileExamples, audioAssetNames)
            if (!audioPairing.isExactMatch) {
                com.dino.sufara.feature.lesson.domain.util.SufaraLogger.log(
                    "LESSON AUDIO [$idPart]: ${audioPairing.exampleCount} primera, " +
                        "${audioPairing.audioFileCount} snimaka, " +
                        "${audioPairing.missingAudioCount} bez snimka, " +
                        "${audioPairing.unusedAudioCount} visak snimaka."
                )
            }
            audioPairing.assignments.forEach { assignment ->
                steps.add(
                    LessonStep.Example(
                        text = assignment.text,
                        audioAssetPath = assignment.audioFileName?.let { "$audioFolderPath/$it" }
                    )
                )
            }

            // Ови примери су намерно последњи кораци лекције и не улазе у курс писања.
            GeneratedVowelExamplePolicy.forLesson(idPart, symbol)
                .filterNot(fileExamples::contains)
                .forEach { text ->
                    steps.add(LessonStep.Example(text = text, includeInWriting = false))
                }
            Lesson(
                id = idPart,
                ordinal = ordinal,
                title = titlePart,
                symbol = symbol,
                status = LessonStatus.LOCKED,
                steps = steps
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun readFile(folder: String, fileName: String): String? {
        return try {
            context.assets.open("$folder/$fileName").bufferedReader().use { it.readText() }
        } catch (e: FileNotFoundException) { null }
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
            developerPrefs.edit { putBoolean("expert_unlock", true) }
            val allLessons = getAllLessons()
            allLessons.filter { it.status == LessonStatus.LOCKED }.forEach { lesson ->
                dao.updateLessonProgress(LessonProgressEntity(lesson.id, LessonStatus.UNLOCKED))
            }
            val writingProgress = dao.getAllWritingProgress().associateBy { it.lessonId }
            lessonTemplates
                .filter { lesson -> lesson.hasWritingContent() }
                .forEach { lesson ->
                    val existing = writingProgress[lesson.id]
                    if (existing?.status != LessonStatus.COMPLETED) {
                        dao.updateWritingProgress(WritingProgressEntity(lesson.id, LessonStatus.UNLOCKED))
                    }
                }
        }
    }

    override suspend fun completeWritingLessonAndUnlockNext(currentLessonId: String) {
        withContext(Dispatchers.IO) {
            dao.updateWritingProgress(
                WritingProgressEntity(currentLessonId, LessonStatus.COMPLETED, System.currentTimeMillis())
            )
            val writingLessons = getWritingLessons()
            val currentIndex = writingLessons.indexOfFirst { it.id == currentLessonId }
            val nextLesson = if (currentIndex >= 0) writingLessons.getOrNull(currentIndex + 1) else null
            if (nextLesson?.status == LessonStatus.LOCKED) {
                dao.updateWritingProgress(WritingProgressEntity(nextLesson.id, LessonStatus.UNLOCKED))
            }
        }
    }

    override suspend fun resetAllProgress() {
        withContext(Dispatchers.IO) {
            developerPrefs.edit { putBoolean("expert_unlock", false) }
            dao.deleteQuizProgress()
            dao.deleteWritingProgress()
            dao.deleteLessonProgress()
        }
    }

    override suspend fun isWritingUnlocked(): Boolean = withContext(Dispatchers.IO) {
        if (developerPrefs.getBoolean("expert_unlock", false)) return@withContext true
        val readingLessons = getAllLessons()
        readingLessons.isNotEmpty() && readingLessons.all { it.status == LessonStatus.COMPLETED }
    }

    override suspend fun getWritingLessons(): List<Lesson> = withContext(Dispatchers.IO) {
        val writingTemplates = lessonTemplates.mapNotNull { lesson ->
            if (!lesson.hasWritingContent()) return@mapNotNull null
            val examples = lesson.steps.filterIsInstance<LessonStep.Example>().filter { it.includeInWriting }
            if (examples.isEmpty()) null else lesson.copy(steps = examples)
        }
        val progressMap = dao.getAllWritingProgress().associateBy { it.lessonId }
        val courseUnlocked = isWritingUnlocked()
        if (!courseUnlocked) return@withContext writingTemplates.map { it.copy(status = LessonStatus.LOCKED) }
        val writingIds = writingTemplates.mapTo(mutableSetOf()) { it.id }
        val firstLessonNeedsUnlock = progressMap.values.none {
            it.lessonId in writingIds && it.status != LessonStatus.LOCKED
        }

        writingTemplates.mapIndexed { index, template ->
            var status = progressMap[template.id]?.status
            if (status == null || (index == 0 && firstLessonNeedsUnlock && status == LessonStatus.LOCKED)) {
                status = if (courseUnlocked && index == 0) LessonStatus.UNLOCKED else status ?: LessonStatus.LOCKED
                dao.updateWritingProgress(WritingProgressEntity(template.id, status))
            }
            template.copy(status = status)
        }
    }

    private fun Lesson.hasWritingContent(): Boolean =
        id !in NON_WRITING_VOWEL_LESSONS &&
            steps.filterIsInstance<LessonStep.Example>().any { it.includeInWriting }

    private companion object {
        val NON_WRITING_VOWEL_LESSONS = setOf("002", "023", "024", "025", "026")
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
            val schedule = ReviewScheduler.next(
                current = currentProgress,
                isCorrect = isCorrect,
                nowMillis = System.currentTimeMillis()
            )

            com.dino.sufara.feature.lesson.domain.util.SufaraLogger.log(
                "ANKI: pitanje [$questionId] | tacno: $isCorrect | " +
                    "zaredom: ${schedule.consecutiveCorrectAnswers} | " +
                    "EF: ${schedule.easinessFactor} | interval: ${schedule.intervalDays} dana"
            )

            dao.updateQuizProgress(
                QuizProgressEntity(
                    questionId = questionId,
                    lessonId = lessonId,
                    nextReviewDate = schedule.nextReviewDate,
                    intervalDays = schedule.intervalDays,
                    consecutiveCorrectAnswers = schedule.consecutiveCorrectAnswers,
                    easinessFactor = schedule.easinessFactor
                )
            )
        }
    }
}
