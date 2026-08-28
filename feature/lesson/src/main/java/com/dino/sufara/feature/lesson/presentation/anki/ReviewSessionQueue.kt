package com.dino.sufara.feature.lesson.presentation.anki

internal object ReviewSessionQueue {
    /** Keeps a wrong item in the session and places it after at most two other questions. */
    fun <T> afterAnswer(items: List<T>, isCorrect: Boolean): List<T> {
        if (items.isEmpty()) return emptyList()
        val answered = items.first()
        val remaining = items.drop(1).toMutableList()
        if (!isCorrect) remaining.add(index = minOf(2, remaining.size), element = answered)
        return remaining
    }
}
