package com.dino.sufara.feature.lesson.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test

class ArabicIpaTranscriberTest {
    @Test
    fun `shadda duplicates the complete consonant transcription`() {
        assertEquals("d͡ʒd͡ʒa", ArabicIpaTranscriber.transcribe("جَّ"))
        assertEquals("tˤtˤɑ", ArabicIpaTranscriber.transcribe("طَّ"))
    }

    @Test
    fun `matching carriers form long vowels`() {
        assertEquals("baː", ArabicIpaTranscriber.transcribe("بَا"))
        assertEquals("biː", ArabicIpaTranscriber.transcribe("بِي"))
        assertEquals("buː", ArabicIpaTranscriber.transcribe("بُو"))
    }

    @Test
    fun `hamza seats all represent a glottal stop`() {
        assertEquals("laʔin", ArabicIpaTranscriber.transcribe("لَئِنْ"))
        assertEquals("suʔaːl", ArabicIpaTranscriber.transcribe("سُؤَالْ"))
        assertEquals("ʔaʔiʔu", ArabicIpaTranscriber.transcribe("أَإِؤُ"))
    }

    @Test
    fun `emphatic and heavy surroundings back fatha without changing other vowels`() {
        assertEquals("sˤɑ", ArabicIpaTranscriber.transcribe("صَ"))
        assertEquals("bɑtˤ", ArabicIpaTranscriber.transcribe("بَطْ"))
        assertEquals("xɑ", ArabicIpaTranscriber.transcribe("خَ"))
        assertEquals("sˤi", ArabicIpaTranscriber.transcribe("صِ"))
    }

    @Test
    fun `ra quality follows its vowel and a preceding kasra when sakin`() {
        assertEquals("rˤɑ", ArabicIpaTranscriber.transcribe("رَ"))
        assertEquals("ri", ArabicIpaTranscriber.transcribe("رِ"))
        assertEquals("mir", ArabicIpaTranscriber.transcribe("مِرْ"))
        assertEquals("mɑrˤ", ArabicIpaTranscriber.transcribe("مَرْ"))
    }

    @Test
    fun `dagger alif and historical carriers produce long a`() {
        assertEquals("hudaː", ArabicIpaTranscriber.transcribe("هُدَىٰ"))
        assertEquals("sˤɑlɑːtun", ArabicIpaTranscriber.transcribe("صَلَوٰةٌ"))
        assertEquals("ðaːlika", ArabicIpaTranscriber.transcribe("ذَٰلِكَ"))
        assertEquals("ʕiːsaː", ArabicIpaTranscriber.transcribe("عِيسَىٰ"))
    }

    @Test
    fun `madda contains a glottal stop and a long vowel`() {
        assertEquals("ʔaːdamu", ArabicIpaTranscriber.transcribe("آدَمُ"))
        assertEquals("qurˤʔɑːnun", ArabicIpaTranscriber.transcribe("قُرْآنٌ"))
    }

    @Test
    fun `Allah supplies its unwritten long alif and context sensitive lam`() {
        assertEquals("ʔɑlˤlˤɑːhu", ArabicIpaTranscriber.transcribe("اللَّهُ"))
        assertEquals("ʔɑlˤlˤɑːh", ArabicIpaTranscriber.transcribe("الله"))
        assertEquals("billaːhi", ArabicIpaTranscriber.transcribe("بِاللَّهِ"))
        assertEquals("lillaːhi", ArabicIpaTranscriber.transcribe("لِلَّهِ"))
        assertEquals("fɑdˤlulˤlˤɑːhi", ArabicIpaTranscriber.transcribe("فَضْلُ اللَّهِ"))
    }

    @Test
    fun `definite article links and assimilates sun letters`() {
        assertEquals("ʔannaːsi", ArabicIpaTranscriber.transcribe("النَّاسِ"))
        assertEquals("minannaːsi", ArabicIpaTranscriber.transcribe("مِنَ النَّاسِ"))
        assertEquals("fiːssamaːwaːti", ArabicIpaTranscriber.transcribe("فِي السَّمَاوَاتِ"))
        assertEquals("liðˤðˤɑːlimiːna", ArabicIpaTranscriber.transcribe("لِلظَّالِمِينَ"))
    }

    @Test
    fun `moon article keeps lam while connected hamzat wasl disappears`() {
        assertEquals("ʔalʔɑrˤdˤi", ArabicIpaTranscriber.transcribe("الْأَرْضِ"))
        assertEquals("minalʔɑrˤdˤi", ArabicIpaTranscriber.transcribe("مِنَ الْأَرْضِ"))
        assertEquals("ʔillaːbtiɣɑːʔɑ", ArabicIpaTranscriber.transcribe("إِلَّا ابْتِغَاءَ"))
    }

    @Test
    fun `tanwin sukun ta marbuta and dotted circle remain explicit for learners`() {
        assertEquals("dun", ArabicIpaTranscriber.transcribe("دٌ"))
        assertEquals("din", ArabicIpaTranscriber.transcribe("دٍ"))
        assertEquals("dan", ArabicIpaTranscriber.transcribe("دًا"))
        assertEquals("wɑrˤdɑtun", ArabicIpaTranscriber.transcribe("وَرْدَةٌ"))
        assertEquals("u", ArabicIpaTranscriber.transcribe("◌ُ"))
    }
}
