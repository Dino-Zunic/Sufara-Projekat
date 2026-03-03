package com.dino.sufara.feature.lesson.domain.util

import kotlinx.coroutines.delay
import kotlin.random.Random

object SoundSimulator {
    /**
     * Simulira puštanje zvuka. 
     * @param onStart Poziva se kada "zvuk" krene
     * @param onEnd Poziva se kada "zvuk" završi
     */
    suspend fun playMockSound(onStart: () -> Unit, onEnd: () -> Unit) {
        val duration = Random.nextLong(300, 2000)
        onStart()
        delay(duration)
        onEnd()
    }
}