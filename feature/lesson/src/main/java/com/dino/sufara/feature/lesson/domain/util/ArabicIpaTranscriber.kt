package com.dino.sufara.feature.lesson.domain.util

object ArabicIpaTranscriber {

    private val consonantMap = mapOf(
        'ب' to "b", 'ت' to "t", 'ث' to "θ", 'ج' to "d͡ʒ", 'ح' to "ħ", 'خ' to "x",
        'د' to "d", 'ذ' to "ð", 'ر' to "r", 'ز' to "z", 'س' to "s", 'ش' to "ʃ",
        'ص' to "sˤ", 'ض' to "dˤ", 'ط' to "tˤ", 'ظ' to "ðˤ", 'ع' to "ʕ", 'غ' to "ɣ",
        'ف' to "f", 'ق' to "q", 'ك' to "k", 'ل' to "l", 'م' to "m", 'ن' to "n",
        'ه' to "h", 'و' to "w", 'ي' to "j"
    )

    private const val FATHA = 'َ'
    private const val DAMMA = 'ُ'
    private const val KASRA = 'ِ'
    private const val SUKUN = 'ْ'
    private const val SHADDA = 'ّ'
    private const val FATHATAN = 'ً'
    private const val DAMMATAN = 'ٌ'
    private const val KASRATAN = 'ٍ'

    fun transcribe(arabicText: String): String {
        var processedText = arabicText.trim()
        if (processedText.isEmpty()) return ""

        processedText = applyLexicalExceptions(processedText)
        processedText = applySunLettersAssimilation(processedText)

        var ipaResult = mapToIpa(processedText)

        // ИСКЉУЧЕНА WAQF ПРАВИЛА ИЗ ПЕДАГОШКИХ РАЗЛОГА
        // Почетници морају да читају последњи харекет/тенвин без претварања у сукун или дуго 'a'
        ipaResult = ipaResult.trimEnd('.')
        
        ipaResult = applyEmphaticAllophony(ipaResult)

        return ipaResult.replace(Regex("\\.+"), ".").removePrefix(".").removeSuffix(".")
    }

    private fun applyLexicalExceptions(text: String): String {
        var t = text
        val wawAsAlifWords = mapOf("صلوة" to "صلاة", "زكوة" to "زكاة", "حيوة" to "حياة")
        wawAsAlifWords.forEach { (old, new) -> t = t.replace(old, new) }
        t = t.replace('ى', 'ا')
        return t
    }

    private fun applySunLettersAssimilation(text: String): String {
        val sunLetters = "[تثدذرزسشصضطظلن]"
        return text.replace(Regex("ال($sunLetters)"), "a$1ّ")
    }

    private fun mapToIpa(text: String): String {
        val sb = StringBuilder()
        var i = 0
        
        while (i < text.length) {
            val char = text[i]

            when {
                char in listOf('أ', 'إ', 'ؤ', 'ئ', 'ء') -> {
                    sb.append("ʔ")
                }
                consonantMap.containsKey(char) -> {
                    sb.append(consonantMap[char])
                }
                char == FATHA -> {
                    if (i + 1 < text.length && text[i + 1] == 'ا') {
                        sb.append("aː.")
                        i++ 
                    } else sb.append("a.")
                }
                char == DAMMA -> {
                    if (i + 1 < text.length && text[i + 1] == 'و') {
                        sb.append("uː.")
                        i++ 
                    } else sb.append("u.")
                }
                char == KASRA -> {
                    if (i + 1 < text.length && text[i + 1] == 'ي') {
                        sb.append("iː.")
                        i++ 
                    } else sb.append("i.")
                }
                char == SHADDA -> {
                    val lastChar = sb.lastOrNull()
                    if (lastChar != null && lastChar != '.') {
                        sb.append(lastChar)
                    }
                }
                char == FATHATAN -> sb.append("an.")
                char == DAMMATAN -> sb.append("un.")
                char == KASRATAN -> sb.append("in.")
            }
            i++
        }
        return sb.toString()
    }

    private fun applyEmphaticAllophony(ipaText: String): String {
        val emphaticConsonants = listOf("sˤ", "dˤ", "tˤ", "ðˤ", "q", "r")
        var modifiedIpa = ipaText

        emphaticConsonants.forEach { emphatic ->
            modifiedIpa = modifiedIpa.replace("$emphatic.a", "$emphatic.ɑ")
            modifiedIpa = modifiedIpa.replace("$emphatic.aː", "$emphatic.ɑː")
            modifiedIpa = modifiedIpa.replace("a.$emphatic", "ɑ.$emphatic")
            modifiedIpa = modifiedIpa.replace("aː.$emphatic", "ɑː.$emphatic")
        }
        return modifiedIpa
    }
}