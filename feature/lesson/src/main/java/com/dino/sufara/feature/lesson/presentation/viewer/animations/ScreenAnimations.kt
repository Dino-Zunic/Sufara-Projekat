package com.dino.sufara.feature.lesson.presentation.viewer.animations

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.IntOffset

@OptIn(ExperimentalAnimationApi::class)
fun getScreenTransition(isForward: Boolean): ContentTransform {
    val duration = 400
    val slideSpec = tween<IntOffset>(durationMillis = duration, easing = FastOutSlowInEasing)
    val fadeSpec = tween<Float>(durationMillis = duration, easing = FastOutSlowInEasing)
    
    val enterOffset = if (isForward) 1000 else -1000
    val exitOffset = if (isForward) -1000 else 1000
    
    return (slideInHorizontally(slideSpec) { enterOffset } + fadeIn(fadeSpec)) togetherWith
           (slideOutHorizontally(slideSpec) { exitOffset } + fadeOut(fadeSpec))
}