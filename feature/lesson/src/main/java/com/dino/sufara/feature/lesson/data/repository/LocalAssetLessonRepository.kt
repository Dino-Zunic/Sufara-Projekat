package com.dino.sufara.feature.lesson.data.repository

import android.content.Context
import com.dino.sufara.feature.lesson.domain.model.Lesson
import com.dino.sufara.feature.lesson.domain.model.LessonStep
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException

class LocalAssetLessonRepository(private val context: Context) : com.dino.sufara.feature.lesson.domain.repository.LessonRepository {

    // Pomoćna funkcija za čitanje simbola iz fajla
    private fun getSymbols(): List<String> {
        return try {
            context.assets.open("lekcije/симболи.md").bufferedReader().use { it.readText() }
        } catch (e: Exception) { "" }.lines().filter { it.isNotBlank() }
    }

    override suspend fun getAllLessons(): List<Lesson> = withContext(Dispatchers.IO) {
        val allAssets = context.assets.list("lekcije") ?: emptyArray()
        val symbols = getSymbols()

        // Filtriramo samo prave foldere (koji počinju brojem, npr. "001") da ne bi pucalo na fajlovima
        val validFolders = allAssets.filter { it.firstOrNull()?.isDigit() == true }.sorted()

        validFolders.mapIndexedNotNull { index, folderName ->
            val rawSymbol = symbols.getOrNull(index)?.trim() ?: "."
            val finalSymbol = if (rawSymbol == ".") "📖" else rawSymbol
            parseLessonFolder(folderName, finalSymbol)
        }
    }

    // ISPRAVKA: Ovaj metod sada traži folder i vraća tačan simbol umjesto hardkodovane knjige!
    override suspend fun getLessonById(id: String): Lesson? = withContext(Dispatchers.IO) {
        // Izvlačimo samo broj lekcije (npr. "004") u slučaju da je proslijeđen cijeli naziv
        val numericId = id.substringBefore(" ") 
        
        // Vraćamo lekciju koja ima sve perfektno parsirano, uključujući i pravi simbol!
        getAllLessons().find { it.id == numericId }
    }

    // Ovo je naša logika za čitanje fajlova
    private suspend fun parseLessonFolder(folderName: String, symbol: String): Lesson? = withContext(Dispatchers.IO) {
        try {
            val basePath = "lekcije/$folderName"
            val steps = mutableListOf<LessonStep>()

            val lekcijaText = readFile(basePath, "лекција.md")
            val dodatakText = readFile(basePath, "додатак.md") // Čitamo dodatak unaprijed

            if (lekcijaText != null) {
                // Pakujemo lekciju i dodatak zajedno
                steps.add(LessonStep.Theory("Теорија", lekcijaText, dodatakText))
                
                if (context.assets.list(basePath)?.contains("исходиште.png") == true) {
                    steps.add(LessonStep.ImageInfo("file:///android_asset/$basePath/исходиште.png"))
                }
            }

            val kvizText = readFile(basePath, "квиз.md")
            if (kvizText != null) {
                steps.addAll(parseQuiz(kvizText))
            }

            val primeriText = readFile(basePath, "примери.md")
            if (primeriText != null) {
                primeriText.lines().filter { it.isNotBlank() }.forEach { line ->
                    steps.add(LessonStep.Example(line.trim()))
                }
            }

            val idPart = folderName.substringBefore(" ")
            val titlePart = folderName.substringAfter(" ")

            Lesson(id = idPart, title = titlePart, symbol = symbol, steps = steps)
        } catch (e: Exception) {
            null
        }
    }

    private fun readFile(folder: String, fileName: String): String? {
        return try {
            context.assets.open("$folder/$fileName").bufferedReader().use { it.readText() }
        } catch (e: FileNotFoundException) { null }
    }

    private fun parseQuiz(text: String): List<LessonStep.Quiz> {
        val quizzes = mutableListOf<LessonStep.Quiz>()
        var currentQuestion = ""
        var currentAnswers = mutableListOf<String>()

        text.lines().forEach { line ->
            if (line.isNotBlank()) {
                if (line.trim().first().isDigit() && !line.startsWith(" ") && !line.startsWith("\t")) {
                    if (currentQuestion.isNotEmpty() && currentAnswers.isNotEmpty()) {
                        quizzes.add(LessonStep.Quiz(currentQuestion, currentAnswers, currentAnswers.first()))
                    }
                    currentQuestion = line.substringAfter(".").trim()
                    currentAnswers = mutableListOf()
                } else if (line.trim().firstOrNull()?.isDigit() == true) {
                    currentAnswers.add(line.substringAfter(".").trim())
                }
            }
        }
        if (currentQuestion.isNotEmpty() && currentAnswers.isNotEmpty()) {
            quizzes.add(LessonStep.Quiz(currentQuestion, currentAnswers, currentAnswers.first()))
        }
        return quizzes
    }

    override suspend fun getFunFacts(): List<String> = withContext(Dispatchers.IO) {
        try {
            context.assets.open("lekcije/cinjenice.md").bufferedReader().readLines().filter { it.isNotBlank() }
        } catch (e: Exception) {
            listOf("Учи у име Господара твога који ствара...") // Сигурносна порука ако фајл фали
        }
    }
}