package com.dino.sufara.feature.lesson.data.repository

internal data class ExampleAudioAssignment(
    val text: String,
    val audioFileName: String?
)

internal data class ExampleAudioPairing(
    val assignments: List<ExampleAudioAssignment>,
    val audioFileCount: Int
) {
    val exampleCount: Int = assignments.size
    val missingAudioCount: Int = (exampleCount - audioFileCount).coerceAtLeast(0)
    val unusedAudioCount: Int = (audioFileCount - exampleCount).coerceAtLeast(0)
    val isExactMatch: Boolean = exampleCount == audioFileCount
}

/**
 * Pairs content lines with recordings by the plain lexicographic filename order requested by the
 * content author. Missing recordings deliberately produce null assignments and extra recordings
 * are ignored, so malformed content cannot prevent the rest of a lesson from loading.
 */
internal object ExampleAudioPairer {
    fun pair(exampleLines: List<String>, assetNames: List<String>): ExampleAudioPairing {
        val orderedAudioFiles = assetNames
            .filter { it.endsWith(".mp3", ignoreCase = true) }
            .sorted()

        return ExampleAudioPairing(
            assignments = exampleLines.mapIndexed { index, text ->
                ExampleAudioAssignment(text = text, audioFileName = orderedAudioFiles.getOrNull(index))
            },
            audioFileCount = orderedAudioFiles.size
        )
    }
}
