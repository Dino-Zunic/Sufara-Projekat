package com.dino.sufara.feature.lesson.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MakharijCatalogTest {
    @Test
    fun catalogHasSeventeenStableUniqueEntries() {
        assertEquals(17, MakharijCatalog.all.size)
        assertEquals(17, MakharijCatalog.all.map { it.id }.toSet().size)
        assertEquals((1..17).toList(), MakharijCatalog.all.map { it.number })
    }

    @Test
    fun nonMaddAndMaddWawAreMappedDifferently() {
        assertTrue(MakharijCatalog.forLesson("013", "و").any { it.id == MakhrajId.BETWEEN_LIPS })
        assertTrue(MakharijCatalog.forLesson("026", "ــُـو").any { it.id == MakhrajId.ORAL_CAVITY_MADD })
    }
}
