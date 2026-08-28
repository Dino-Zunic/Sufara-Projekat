package com.dino.sufara.feature.lesson.domain.util

object ArabicWordSplitter {
    private val marks = setOf('َ', 'ُ', 'ِ', 'ً', 'ٌ', 'ٍ', 'ّ', 'ْ', 'ٰ', 'ٓ', 'ٔ', 'ٕ')

    /** Returns isolated written graphemes while keeping every haraka on its base letter. */
    fun splitGraphemes(text: String): List<String> {
        val graphemes = mutableListOf<StringBuilder>()
        text.forEach { character ->
            when {
                character == 'ـ' -> Unit
                character in marks -> {
                    if (graphemes.isEmpty()) graphemes += StringBuilder("◌")
                    graphemes.last().append(character)
                }
                character.isWhitespace() -> Unit
                character == '◌' || character.isArabicLetter() -> graphemes += StringBuilder().append(character)
            }
        }
        return graphemes.map(StringBuilder::toString)
    }

    fun splitWord(word: String): String = splitGraphemes(word).joinToString("  ")

    private fun Char.isArabicLetter(): Boolean =
        this in '\u0621'..'\u064A' || this in '\u0671'..'\u06D3'
}
