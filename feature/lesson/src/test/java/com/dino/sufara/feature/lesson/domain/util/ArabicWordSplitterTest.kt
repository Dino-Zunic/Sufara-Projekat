package com.dino.sufara.feature.lesson.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test

class ArabicWordSplitterTest {
    @Test
    fun `keeps hamza seat and kasra in one isolated grapheme`() {
        assertEquals(listOf("لَ", "ئِ", "نْ"), ArabicWordSplitter.splitGraphemes("لَئِنْ"))
        assertEquals(
            listOf("يَ", "وْ", "مَ", "ئِ", "ذٍ"),
            ArabicWordSplitter.splitGraphemes("يَوْمَئِذٍ")
        )
        assertEquals("لَ  ئِ  نْ", ArabicWordSplitter.splitWord("لَئِنْ"))
    }

    @Test
    fun `keeps shadda and vowel with their base letter`() {
        assertEquals(listOf("تُ", "بَ", "وِّ", "ئُ"), ArabicWordSplitter.splitGraphemes("تُبَوِّئُ"))
    }

    @Test
    fun `uses dotted circle for a standalone mark and ignores tatweel`() {
        assertEquals(listOf("◌ُ"), ArabicWordSplitter.splitGraphemes("ــُـ"))
        assertEquals(listOf("◌ِ"), ArabicWordSplitter.splitGraphemes("◌ِ"))
    }

    @Test
    fun `separates letters across spaces without creating empty graphemes`() {
        assertEquals(listOf("فِ", "ي", "بَ", "يْ", "تٍ"), ArabicWordSplitter.splitGraphemes("فِي بَيْتٍ"))
    }
}
