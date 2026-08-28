package com.dino.sufara.feature.lesson.domain.util

import kotlin.random.Random

/** Returns a shuffled order and avoids repeating the immediately previous layout. */
fun reshuffleAnswers(
    answers: List<String>,
    previous: List<String> = emptyList(),
    random: Random = Random.Default
): List<String> {
    if (answers.size < 2) return answers
    val shuffled = answers.shuffled(random)
    return if (shuffled == previous) shuffled.drop(1) + shuffled.first() else shuffled
}
