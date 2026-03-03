package com.dino.sufara.feature.lesson.presentation.viewer.animations

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.IntOffset

enum class CardAnimationType { SLIDE, SCALE_FADE, TILT_SLIDE, FADE }

@OptIn(ExperimentalAnimationApi::class)
fun getCardTransition(type: CardAnimationType, isForward: Boolean): ContentTransform {
    // Punchy (брз и одсечан) ефекат анимације
    val duration = 250
    val slideSpec = tween<IntOffset>(durationMillis = duration, easing = FastOutSlowInEasing)
    val floatSpec = tween<Float>(durationMillis = duration, easing = FastOutSlowInEasing)
    
    val offset = if (isForward) 1200 else -1200
    
    return when (type) {
        CardAnimationType.SLIDE -> {
            (slideInHorizontally(slideSpec) { offset } + fadeIn(floatSpec)) togetherWith
            (slideOutHorizontally(slideSpec) { -offset } + fadeOut(floatSpec))
        }
        CardAnimationType.SCALE_FADE -> {
            (scaleIn(floatSpec, initialScale = 0.8f) + fadeIn(floatSpec)) togetherWith
            (scaleOut(floatSpec, targetScale = 0.8f) + fadeOut(floatSpec))
        }
        CardAnimationType.TILT_SLIDE -> {
            val enterOffset = if (isForward) 800 else -800
            val exitOffset = if (isForward) -400 else 400
            (slideInHorizontally(slideSpec) { enterOffset } + scaleIn(floatSpec, 0.85f) + fadeIn(floatSpec)) togetherWith
            (slideOutHorizontally(slideSpec) { exitOffset } + scaleOut(floatSpec, 0.85f) + fadeOut(floatSpec))
        }
        CardAnimationType.FADE -> {
            fadeIn(floatSpec) togetherWith fadeOut(floatSpec)
        }
    }
}