package com.dino.sufara.feature.lesson.domain.util

object ArabicWordSplitter {
    /**
     * Раздваја арапску реч на појединачна слова, притом задржавајући 
     * харекете везане за њихово основно слово.
     */
    fun splitWord(word: String): String {
        val sb = StringBuilder()
        val diacritics = setOf('َ', 'ُ', 'ِ', 'ً', 'ٌ', 'ٍ', 'ّ', 'ْ', 'ٰ', '\u0670')
        
        for (i in word.indices) {
            val char = word[i]
            // Ако тренутни карактер није харекет и није прво слово, додајемо размак ПРЕ њега
            if (char !in diacritics && i > 0 && word[i - 1] != ' ') {
                sb.append("  ") // Дупли размак ради боље прегледности
            }
            sb.append(char)
        }
        
        return sb.toString().trim()
    }
}