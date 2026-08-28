package com.dino.sufara.feature.lesson.domain.model

sealed class LessonStep {
    data class Theory(
        val title: String,
        val text: String,
        val dodatakText: String? = null,
        val makharij: List<MakhrajInfo> = emptyList(),
        val originImagePath: String? = null
    ) : LessonStep()
    data class ImageInfo(val imagePath: String) : LessonStep()
    data class Quiz(val id: String, val question: String, val answers: List<String>, val correctAnswer: String) : LessonStep()
    data class Example(
        val text: String,
        /** Generated reading examples must never silently become writing exercises. */
        val includeInWriting: Boolean = true,
        /** Full path inside AssetManager, or null when this card has no recorded pronunciation. */
        val audioAssetPath: String? = null
    ) : LessonStep()
}
