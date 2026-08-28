package com.dino.sufara.feature.lesson.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GeneratedVowelExamplePolicyTest {

    @Test
    fun `hamza lesson uses its three orthographically correct seats`() {
        assertEquals(
            listOf("أَ", "إِ", "أُ"),
            GeneratedVowelExamplePolicy.forLesson("003", "أ")
        )
    }

    @Test
    fun `special hamza seat lessons do not generate isolated vowel cards`() {
        assertTrue(GeneratedVowelExamplePolicy.forLesson("032", "ئ").isEmpty())
        assertTrue(GeneratedVowelExamplePolicy.forLesson("033", "ؤ").isEmpty())
        assertTrue(GeneratedVowelExamplePolicy.forLesson("034", "ء").isEmpty())
    }

    @Test
    fun `ordinary letter lesson still receives three vowel examples`() {
        assertEquals(
            listOf("بَ", "بِ", "بُ"),
            GeneratedVowelExamplePolicy.forLesson("014", "ب")
        )
    }

    @Test
    fun `non-letter lesson remains excluded`() {
        assertTrue(GeneratedVowelExamplePolicy.forLesson("031", "📖").isEmpty())
    }
}
