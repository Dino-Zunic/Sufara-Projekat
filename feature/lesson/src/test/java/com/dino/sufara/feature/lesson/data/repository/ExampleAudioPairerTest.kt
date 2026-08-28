package com.dino.sufara.feature.lesson.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleAudioPairerTest {

    @Test
    fun `pairs recordings using plain lexicographic filename order`() {
        val result = ExampleAudioPairer.pair(
            exampleLines = listOf("prvi", "drugi", "treci", "cetvrti"),
            assetNames = listOf("0316_bb.mp3", "notes.txt", "0317.mp3", "0316_a.mp3", "0316.mp3")
        )

        assertEquals(
            listOf("0316.mp3", "0316_a.mp3", "0316_bb.mp3", "0317.mp3"),
            result.assignments.map { it.audioFileName }
        )
        assertTrue(result.isExactMatch)
    }

    @Test
    fun `missing recordings leave safe null assignments`() {
        val result = ExampleAudioPairer.pair(
            exampleLines = listOf("prvi", "drugi", "treci"),
            assetNames = listOf("0001.mp3")
        )

        assertEquals("0001.mp3", result.assignments[0].audioFileName)
        assertNull(result.assignments[1].audioFileName)
        assertNull(result.assignments[2].audioFileName)
        assertEquals(2, result.missingAudioCount)
        assertEquals(0, result.unusedAudioCount)
        assertFalse(result.isExactMatch)
    }

    @Test
    fun `extra recordings are counted but not assigned past the last card`() {
        val result = ExampleAudioPairer.pair(
            exampleLines = listOf("jedini"),
            assetNames = listOf("0002.MP3", "0001.mp3", "cover.jpg")
        )

        assertEquals(listOf("0001.mp3"), result.assignments.map { it.audioFileName })
        assertEquals(0, result.missingAudioCount)
        assertEquals(1, result.unusedAudioCount)
        assertFalse(result.isExactMatch)
    }

    @Test
    fun `empty lesson and folder are an exact match`() {
        val result = ExampleAudioPairer.pair(emptyList(), emptyList())

        assertTrue(result.assignments.isEmpty())
        assertTrue(result.isExactMatch)
    }
}
