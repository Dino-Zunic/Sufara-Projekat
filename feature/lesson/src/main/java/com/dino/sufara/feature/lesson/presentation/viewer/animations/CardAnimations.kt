package com.dino.sufara.feature.lesson.presentation.viewer.animations

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.IntOffset

enum class CardAnimationType { SLIDE, SCALE_FADE, TILT_SLIDE, FADE }

@OptIn(ExperimentalAnimationApi::class)
fun getCardTransition(type: CardAnimationType, isForward: Boolean): ContentTransform {
    val duration = 400
    val easing = FastOutSlowInEasing
    
    val enterOffset = if (isForward) 1200 else -1200
    val exitOffset = if (isForward) -1200 else 1200
    
    return when (type) {
        CardAnimationType.SLIDE -> {
            (slideInHorizontally(tween(duration, easing = easing)) { enterOffset } + 
             fadeIn(tween(duration))) togetherWith 
            (slideOutHorizontally(tween(duration, easing = easing)) { exitOffset } + 
             fadeOut(tween(duration)))
        }
        CardAnimationType.SCALE_FADE -> {
            (scaleIn(tween(duration, easing = easing), initialScale = 0.8f) + 
             fadeIn(tween(duration))) togetherWith 
            (scaleOut(tween(duration, easing = easing), targetScale = 0.8f) + 
             fadeOut(tween(duration)))
        }
        CardAnimationType.TILT_SLIDE -> {
            (slideInHorizontally(tween(duration, easing = easing)) { enterOffset / 2 } + 
             scaleIn(tween(duration, easing = easing), initialScale = 0.85f) + 
             fadeIn(tween(duration))) togetherWith 
            (slideOutHorizontally(tween(duration, easing = easing)) { exitOffset / 2 } + 
             scaleOut(tween(duration, easing = easing), targetScale = 0.85f) + 
             fadeOut(tween(duration)))
        }
        CardAnimationType.FADE -> {
            fadeIn(tween(duration)) togetherWith fadeOut(tween(duration))
        }
    }
}