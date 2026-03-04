package com.dino.sufara.feature.lesson.presentation.viewer.animations

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween

enum class CardAnimationType { SLIDE, SCALE_FADE, TILT_SLIDE, FADE }

fun getCardTransition(type: CardAnimationType, isForward: Boolean): ContentTransform {
    val duration = 400
    val easing = FastOutSlowInEasing
    
    return when (type) {
        CardAnimationType.SLIDE -> {
            (slideInHorizontally(tween(duration, easing = easing)) { if (isForward) it else -it } + 
            fadeIn(tween(duration))) togetherWith 
            (slideOutHorizontally(tween(duration, easing = easing)) { if (isForward) -it else it } + 
            fadeOut(tween(duration)))
        }
        CardAnimationType.SCALE_FADE -> {
            (scaleIn(tween(duration, easing = easing), initialScale = 0.8f) + 
            fadeIn(tween(duration))) togetherWith 
            (scaleOut(tween(duration, easing = easing), targetScale = 0.8f) + 
            fadeOut(tween(duration)))
        }
        CardAnimationType.TILT_SLIDE -> {
            (slideInHorizontally(tween(duration, easing = easing)) { if (isForward) it / 2 else -it / 2 } + 
            scaleIn(tween(duration, easing = easing), initialScale = 0.9f) + 
            fadeIn(tween(duration))) togetherWith 
            (slideOutHorizontally(tween(duration, easing = easing)) { if (isForward) -it / 2 else it / 2 } + 
            scaleOut(tween(duration, easing = easing), targetScale = 0.9f) + 
            fadeOut(tween(duration)))
        }
        CardAnimationType.FADE -> {
            fadeIn(tween(duration)) togetherWith fadeOut(tween(duration))
        }
    }
}