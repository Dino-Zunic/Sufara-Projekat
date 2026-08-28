package com.dino.sufara.feature.lesson.domain.util

/**
 * Broad pedagogical IPA for the vocalised Classical and Modern Standard Arabic used by the app.
 * It reads grapheme clusters instead of individual UTF-16 characters, so shadda, vowel marks and
 * hidden alif rules are applied to the letter they belong to.
 */
object ArabicIpaTranscriber {
    private data class Grapheme(val base: Char?, val marks: Set<Char>)
    private data class WordIpa(val value: String, val joinsPrevious: Boolean)

    private val consonants = mapOf(
        'ب' to "b", 'ت' to "t", 'ث' to "θ", 'ج' to "d͡ʒ", 'ح' to "ħ", 'خ' to "x",
        'د' to "d", 'ذ' to "ð", 'ر' to "r", 'ز' to "z", 'س' to "s", 'ش' to "ʃ",
        'ص' to "sˤ", 'ض' to "dˤ", 'ط' to "tˤ", 'ظ' to "ðˤ", 'ع' to "ʕ", 'غ' to "ɣ",
        'ف' to "f", 'ق' to "q", 'ك' to "k", 'ل' to "l", 'م' to "m", 'ن' to "n",
        'ه' to "h", 'ة' to "t", 'و' to "w", 'ي' to "j"
    )

    private const val FATHA = 'َ'
    private const val DAMMA = 'ُ'
    private const val KASRA = 'ِ'
    private const val FATHATAN = 'ً'
    private const val DAMMATAN = 'ٌ'
    private const val KASRATAN = 'ٍ'
    private const val SHADDA = 'ّ'
    private const val SUKUN = 'ْ'
    private const val DAGGER_ALIF = 'ٰ'
    private const val MADDA_ABOVE = 'ٓ'
    private const val HAMZA_ABOVE = 'ٔ'
    private const val HAMZA_BELOW = 'ٕ'
    private const val TATWEEL = 'ـ'
    private const val DOTTED_CIRCLE = '◌'

    private val combiningMarks = setOf(
        FATHA, DAMMA, KASRA, FATHATAN, DAMMATAN, KASRATAN,
        SHADDA, SUKUN, DAGGER_ALIF, MADDA_ABOVE, HAMZA_ABOVE, HAMZA_BELOW
    )
    private val hamzaLetters = setOf('أ', 'إ', 'ؤ', 'ئ', 'ء')
    private val alifWaslLetters = setOf('ا', 'ٱ')
    private val sunLetters = setOf('ت', 'ث', 'د', 'ذ', 'ر', 'ز', 'س', 'ش', 'ص', 'ض', 'ط', 'ظ', 'ل', 'ن')
    private val heavyLetters = setOf('خ', 'ص', 'ض', 'غ', 'ط', 'ق', 'ظ')

    fun transcribe(arabicText: String): String {
        val words = arabicText
            .trim()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
        if (words.isEmpty()) return ""

        val result = StringBuilder()
        words.forEachIndexed { index, word ->
            val previousVowel = result.lastBroadVowel()
            val wordIpa = transcribeWord(word, connected = index > 0, previousVowel = previousVowel)
            if (wordIpa.value.isBlank()) return@forEachIndexed
            if (result.isNotEmpty() && !wordIpa.joinsPrevious) result.append(' ')
            result.append(wordIpa.value)
        }
        return result.toString().replace(Regex("\\s+"), " ").trim()
    }

    private fun transcribeWord(rawWord: String, connected: Boolean, previousVowel: Char?): WordIpa {
        val graphemes = graphemes(rawWord)
        if (graphemes.isEmpty()) return WordIpa("", joinsPrevious = false)

        val result = StringBuilder()
        val forcedGemination = mutableSetOf<Int>()
        var joinsPrevious = false
        var index = 0

        while (index < graphemes.size) {
            val grapheme = graphemes[index]
            val base = grapheme.base

            val allahLength = allahSequenceLength(graphemes, index)
            if (allahLength > 0) {
                val localPreviousVowel = result.lastBroadVowel() ?: previousVowel
                val softLam = localPreviousVowel == 'i'
                val beginsWithWasl = index == 0
                if (beginsWithWasl && connected) joinsPrevious = true
                if (beginsWithWasl && !connected) result.append("ʔɑ")
                result.append(if (softLam) "llaːh" else "lˤlˤɑːh")
                val finalHe = graphemes[index + allahLength - 1]
                result.append(shortVowel(finalHe, backA = false))
                index += allahLength
                continue
            }

            if (isContractedAllahRemainder(graphemes, index)) {
                result.append("llaːh")
                result.append(shortVowel(graphemes[index + 1], backA = false))
                index += 2
                continue
            }

            if (base in alifWaslLetters && graphemes.getOrNull(index + 1)?.base == 'ل') {
                val articleIsConnected = connected || index > 0
                if (index == 0 && articleIsConnected) joinsPrevious = true
                if (!articleIsConnected) result.append("ʔa")

                val articleLetterIndex = index + 2
                val articleLetter = graphemes.getOrNull(articleLetterIndex)?.base
                if (articleLetter in sunLetters) {
                    forcedGemination += articleLetterIndex
                } else {
                    result.append('l')
                }
                index = articleLetterIndex
                continue
            }

            if (isElidedArticleLam(graphemes, index)) {
                forcedGemination += index + 1
                index++
                continue
            }

            if (base == null) {
                result.append(shortVowel(grapheme, backA = false))
                index++
                continue
            }

            if (base == 'آ') {
                result.append("ʔ")
                result.append(if (usesBackA(graphemes, index)) "ɑː" else "aː")
                index++
                continue
            }

            if (base in alifWaslLetters) {
                if (index == 0) {
                    if (connected) {
                        joinsPrevious = true
                    } else if (graphemes.size == 1) {
                        result.append("ʔaː")
                    } else {
                        result.append('ʔ')
                        result.append(initialWaslVowel(grapheme))
                    }
                } else {
                    result.append(if (usesBackA(graphemes, index)) "ɑː" else "aː")
                }
                index++
                continue
            }

            if (base == 'ى') {
                result.append(if (usesBackA(graphemes, index)) "ɑː" else "aː")
                index++
                continue
            }

            val consonant = when {
                base in hamzaLetters -> "ʔ"
                base == 'ر' && isHeavyRa(graphemes, index) -> "rˤ"
                else -> consonants[base].orEmpty()
            }
            if (consonant.isEmpty()) {
                index++
                continue
            }

            val copies = if (SHADDA in grapheme.marks || index in forcedGemination) 2 else 1
            repeat(copies) { result.append(consonant) }

            val backA = usesBackA(graphemes, index)
            when {
                FATHATAN in grapheme.marks -> {
                    result.append(if (backA) "ɑn" else "an")
                    if (graphemes.getOrNull(index + 1)?.base == 'ا') index++
                }
                DAMMATAN in grapheme.marks -> result.append("un")
                KASRATAN in grapheme.marks -> result.append("in")
                FATHA in grapheme.marks -> {
                    val hasLongCarrier = isLongA(graphemes.getOrNull(index + 1))
                    result.append(if (backA) "ɑ" else "a")
                    if (hasLongCarrier || DAGGER_ALIF in grapheme.marks) result.append('ː')
                    if (hasLongCarrier) index++
                }
                DAMMA in grapheme.marks -> {
                    val hasLongCarrier = isPlainCarrier(graphemes.getOrNull(index + 1), 'و')
                    result.append('u')
                    if (hasLongCarrier) {
                        result.append('ː')
                        index++
                    }
                }
                KASRA in grapheme.marks -> {
                    val hasLongCarrier = isPlainCarrier(graphemes.getOrNull(index + 1), 'ي')
                    result.append('i')
                    if (hasLongCarrier) {
                        result.append('ː')
                        index++
                    }
                }
                DAGGER_ALIF in grapheme.marks -> result.append(if (backA) "ɑː" else "aː")
            }
            index++
        }

        return WordIpa(result.toString(), joinsPrevious)
    }

    private fun graphemes(text: String): List<Grapheme> {
        val result = mutableListOf<Grapheme>()
        text.forEach { character ->
            when {
                character == TATWEEL -> Unit
                character == DOTTED_CIRCLE -> result += Grapheme(base = null, marks = emptySet())
                character in combiningMarks -> {
                    if (result.isEmpty()) result += Grapheme(base = null, marks = setOf(character))
                    else result[result.lastIndex] = result.last().copy(marks = result.last().marks + character)
                }
                character.isArabicLetter() -> result += Grapheme(base = character, marks = emptySet())
            }
        }
        return result
    }

    private fun Char.isArabicLetter(): Boolean =
        this in '\u0621'..'\u064A' || this in '\u0671'..'\u06D3'

    private fun allahSequenceLength(graphemes: List<Grapheme>, start: Int): Int {
        if (start + 3 >= graphemes.size) return 0
        val bases = (start..start + 3).map { graphemes[it].base }
        if (bases != listOf('ا', 'ل', 'ل', 'ه')) return 0
        return 4
    }

    /** Handles the spelling لِلَّهِ, where the alif of Allah is omitted after the prefix li-. */
    private fun isContractedAllahRemainder(graphemes: List<Grapheme>, index: Int): Boolean {
        if (index <= 0 || index + 1 != graphemes.lastIndex) return false
        return graphemes[index - 1].base == 'ل' && KASRA in graphemes[index - 1].marks &&
            graphemes[index].base == 'ل' && SHADDA in graphemes[index].marks &&
            graphemes[index + 1].base == 'ه'
    }

    /** Handles لِلشَّمْسِ: after li-, the written article alif is omitted and its lam assimilates. */
    private fun isElidedArticleLam(graphemes: List<Grapheme>, index: Int): Boolean {
        if (index <= 0 || index + 1 >= graphemes.size) return false
        return graphemes[index].base == 'ل' &&
            graphemes[index - 1].base == 'ل' && KASRA in graphemes[index - 1].marks &&
            graphemes[index + 1].base in sunLetters
    }

    private fun initialWaslVowel(grapheme: Grapheme): Char = when {
        DAMMA in grapheme.marks -> 'u'
        FATHA in grapheme.marks -> 'a'
        else -> 'i'
    }

    private fun isLongA(next: Grapheme?): Boolean {
        if (next == null) return false
        return next.base == 'ا' || next.base == 'ى' || DAGGER_ALIF in next.marks
    }

    private fun isPlainCarrier(next: Grapheme?, expectedBase: Char): Boolean =
        next?.base == expectedBase && next.marks.none {
            it == FATHA || it == DAMMA || it == KASRA || it == FATHATAN ||
                it == DAMMATAN || it == KASRATAN || it == SHADDA || it == SUKUN
        }

    private fun shortVowel(grapheme: Grapheme, backA: Boolean): String = when {
        FATHATAN in grapheme.marks -> if (backA) "ɑn" else "an"
        DAMMATAN in grapheme.marks -> "un"
        KASRATAN in grapheme.marks -> "in"
        FATHA in grapheme.marks -> if (DAGGER_ALIF in grapheme.marks) {
            if (backA) "ɑː" else "aː"
        } else if (backA) "ɑ" else "a"
        DAMMA in grapheme.marks -> "u"
        KASRA in grapheme.marks -> "i"
        else -> ""
    }

    private fun usesBackA(graphemes: List<Grapheme>, index: Int): Boolean {
        val start = (index - 2).coerceAtLeast(0)
        val end = (index + 2).coerceAtMost(graphemes.lastIndex)
        return (start..end).any { nearby -> isHeavy(graphemes, nearby) }
    }

    private fun isHeavy(graphemes: List<Grapheme>, index: Int): Boolean {
        val base = graphemes.getOrNull(index)?.base ?: return false
        return base in heavyLetters || (base == 'ر' && isHeavyRa(graphemes, index))
    }

    private fun isHeavyRa(graphemes: List<Grapheme>, index: Int): Boolean {
        val marks = graphemes[index].marks
        if (KASRA in marks || KASRATAN in marks) return false
        if (FATHA in marks || DAMMA in marks || FATHATAN in marks || DAMMATAN in marks) return true
        if (SUKUN in marks) {
            for (previous in index - 1 downTo 0) {
                val previousMarks = graphemes[previous].marks
                if (KASRA in previousMarks || KASRATAN in previousMarks) return false
                if (FATHA in previousMarks || DAMMA in previousMarks ||
                    FATHATAN in previousMarks || DAMMATAN in previousMarks
                ) return true
            }
        }
        return true
    }

    private fun StringBuilder.lastBroadVowel(): Char? =
        asSequence().toList().asReversed().firstOrNull { it == 'a' || it == 'ɑ' || it == 'i' || it == 'u' }
}
