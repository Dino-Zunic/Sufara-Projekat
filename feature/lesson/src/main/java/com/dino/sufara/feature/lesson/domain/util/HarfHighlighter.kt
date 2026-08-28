package com.dino.sufara.feature.lesson.domain.util

// ОВО ЈЕ ФАЛИЛО!
enum class HighlightType { HARAKAH, HARF, NONE }

sealed class HighlightAction {
    data class Harakah(val diacritics: Set<Int>, val supports: Set<Int>) : HighlightAction()
    data class Harf(val characters: Set<Int>, val attachedDiacritics: Set<Int>) : HighlightAction()
    object None : HighlightAction()
}

object HarfHighlighter {
    private val harakahLessonIds = setOf(
        "001", "002", "006", "007", "008", "009", "010", "018",
        "023", "024", "025", "026", "048"
    )
    
    fun Char.isArabicDiacritic(): Boolean {
        return this in '\u0610'..'\u061A' || this in '\u064B'..'\u065F' || this == '\u0670'
    }

    fun analyze(text: String, lessonId: String, symbol: String): HighlightAction {
        SufaraLogger.log("--- АНАЛИЗА РЕЧИ: '$text' | Лекција: $lessonId | Симбол: '$symbol' ---")

        when (lessonId) {
            in harakahLessonIds -> {
                val targets = mutableSetOf<Int>()
                val supports = mutableSetOf<Int>()
                
                when (lessonId) {
                    "001" -> {
                        text.forEachIndexed { i, c ->
                            if (c.isArabicDiacritic()) targets.add(i)
                        }
                    }
                    "002" -> {
                        val vowels = listOf('َ', 'ُ', 'ِ', '\u0670') 
                        text.forEachIndexed { i, c -> if (c in vowels) targets.add(i) }
                    }
                    "006" -> {
                        val tanween = setOf('ً', 'ٌ', 'ٍ')
                        text.forEachIndexed { i, c -> if (c in tanween) targets.add(i) }
                    }
                    "007" -> {
                        text.forEachIndexed { i, c -> if (c == 'ٌ') targets.add(i) }
                    }
                    "008" -> {
                        text.forEachIndexed { i, c -> if (c == 'ٍ') targets.add(i) }
                    }
                    "009" -> {
                        text.forEachIndexed { i, c -> if (c == 'ً') targets.add(i) }
                    }
                    "010" -> {
                        text.forEachIndexed { i, c -> if (c == 'ْ') targets.add(i) }
                    }
                    "018" -> {
                        text.forEachIndexed { i, c -> if (c == 'ّ') targets.add(i) }
                    }
                    "023" -> {
                        addLongVowelTargets(text, targets, shortVowel = 'َ', longVowel = 'ا')
                        addLongVowelTargets(text, targets, shortVowel = 'ِ', longVowel = 'ي')
                        addLongVowelTargets(text, targets, shortVowel = 'ُ', longVowel = 'و')
                    }
                    "024" -> addLongVowelTargets(text, targets, shortVowel = 'َ', longVowel = 'ا')
                    "025" -> addLongVowelTargets(text, targets, shortVowel = 'ِ', longVowel = 'ي')
                    "026" -> addLongVowelTargets(text, targets, shortVowel = 'ُ', longVowel = 'و')
                    "048" -> {
                        text.forEachIndexed { i, c -> if (c == '\u0670') targets.add(i) }
                    }
                }

                val finalTargets = targets.toMutableSet()
                targets.forEach { i ->
                    var j = i - 1
                    while (j >= 0 && text[j].isArabicDiacritic()) j--
                    if (j >= 0) supports.add(j)
                }

                if (lessonId == "009") {
                    val elifs = listOf('ا', 'ى')
                    text.forEachIndexed { i, c ->
                        if (c in elifs && (targets.contains(i - 1) || targets.contains(i + 1))) {
                            finalTargets.add(i)
                            supports.remove(i)
                        }
                    }
                }

                SufaraLogger.log("Резултат HARAKAH -> Мете: $finalTargets | Носачи: $supports")
                return HighlightAction.Harakah(finalTargets, supports)
            }
            
            else -> { 
                if (symbol == "." || symbol == "📖" || symbol.isBlank()) {
                    SufaraLogger.log("Резултат: Нема симбола за Харф лекцију (None)")
                    return HighlightAction.None
                }

                val targets = mutableSetOf<Int>()
                val attachedDiacritics = mutableSetOf<Int>()
                val cleanSymbolChars = symbol.filter { it in '\u0600'..'\u06FF' && it != 'ـ' }.toSet()
                val extraTargets = mutableSetOf<Char>()

                if (cleanSymbolChars.intersect(setOf('أ', 'إ', 'ؤ', 'ئ', 'ء')).isNotEmpty()) {
                    extraTargets.addAll(setOf('أ', 'إ', 'ؤ', 'ئ', 'ء'))
                }

                val finalTargets = cleanSymbolChars + extraTargets

                text.forEachIndexed { i, c ->
                    if (c in finalTargets) {
                        targets.add(i) 
                        var j = i + 1
                        while (j < text.length && text[j].isArabicDiacritic()) {
                            attachedDiacritics.add(j)
                            j++
                        }
                    }
                }

                SufaraLogger.log("Резултат HARF -> Слово(мета): $targets | Харекети: $attachedDiacritics")
                return HighlightAction.Harf(targets, attachedDiacritics)
            }
        }
    }

    private fun addLongVowelTargets(
        text: String,
        targets: MutableSet<Int>,
        shortVowel: Char,
        longVowel: Char
    ) {
        text.forEachIndexed { index, char ->
            if (char == shortVowel && text.getOrNull(index + 1) == longVowel) {
                targets.add(index)
                targets.add(index + 1)
            }
        }
    }
}
