package com.dino.sufara.feature.lesson.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test

class MapSymbolFormatterTest {
    @Test
    fun `tatweel based haraka uses dotted circle`() {
        assertEquals("◌ْ", mapDisplaySymbol("ــْـ"))
        assertEquals("◌ً", mapDisplaySymbol("ــًـ"))
        assertEquals("◌ٍ", mapDisplaySymbol("ــٍـ"))
        assertEquals("◌ٌ", mapDisplaySymbol("ــٌـ"))
        assertEquals("◌ّ", mapDisplaySymbol("ــّـ"))
    }

    @Test
    fun `ordinary Arabic symbol is unchanged`() {
        assertEquals("ب", mapDisplaySymbol("ب"))
        assertEquals("لا", mapDisplaySymbol("لا"))
    }
}
