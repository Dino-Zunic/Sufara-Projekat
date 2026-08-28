package com.dino.sufara.feature.lesson.data.repository

internal object GeneratedVowelExamplePolicy {
    private const val ARABIC_LETTERS = "ءاأإؤئبتةثجحخدذرزسشصضطظعغفقكلمنهويى"
    private val eligibleLessonIds = setOf(
        "003", "004", "005",
        "011", "012", "013", "014", "015", "016", "017",
        "019", "020", "021",
        "028", "029", "030",
        "035", "036", "037", "038", "039",
        "040", "041", "042", "043", "044", "045", "046", "047"
    )

    fun forLesson(lessonId: String, symbol: String): List<String> {
        if (lessonId !in eligibleLessonIds) return emptyList()
        if (lessonId == "003") return listOf("أَ", "إِ", "أُ")
        val letter = symbol.singleOrNull() ?: return emptyList()
        if (letter !in ARABIC_LETTERS) return emptyList()
        return listOf("$letterَ", "$letterِ", "$letterُ")
    }
}
