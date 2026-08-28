package com.dino.sufara.feature.lesson.presentation.viewer

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dino.sufara.core.designsystem.BlueMidnight
import com.dino.sufara.core.designsystem.GoldBase
import com.dino.sufara.core.designsystem.TextSilver
import com.dino.sufara.core.designsystem.components.GoldenWireButton
import com.dino.sufara.feature.lesson.domain.model.LessonStatus
import com.dino.sufara.feature.lesson.domain.model.LessonStep
import com.dino.sufara.feature.lesson.domain.util.SufaraLogger
import com.dino.sufara.feature.lesson.domain.util.asScript
import com.dino.sufara.feature.lesson.presentation.settings.LocalSufaraSettings
import com.dino.sufara.feature.lesson.presentation.settings.SufaraFonts
import com.dino.sufara.feature.lesson.presentation.viewer.animations.getCardTransition
import com.dino.sufara.feature.lesson.presentation.viewer.components.*
import kotlinx.coroutines.delay

@Composable
fun LessonViewerScreen(
    viewModel: LessonViewerViewModel,
    onExitToMain: () -> Unit,
    onLessonFinished: () -> Unit
) {
    val lesson by viewModel.lesson.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val completedSteps by viewModel.completedSteps.collectAsState()
    val settings = LocalSufaraSettings.current
    val cyrillicFont = SufaraFonts.getCyrillicFont(settings.cyrillicFont)

    // УКЛОЊЕН isLoading КОЈИ ЈЕ УБИЈАО АНИМАЦИЈУ!
    var showExitDialog by remember { mutableStateOf(false) }
    var navigationHintVisible by remember { mutableStateOf(false) }
    LaunchedEffect(navigationHintVisible) {
        if (navigationHintVisible) {
            delay(3_200)
            navigationHintVisible = false
        }
    }
    val currentLesson = lesson
    if (currentLesson == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = GoldBase) 
        }
        return
    }

    val steps = currentLesson.steps
    if (steps.isEmpty()) {
        LaunchedEffect(currentLesson.id) { onLessonFinished() }
        return
    }

    val currentStep = steps[currentIndex]
    val progress = (currentIndex + 1).toFloat() / steps.size.toFloat()

    val isNextEnabled = remember(currentLesson.status, completedSteps, currentIndex, currentStep) {
        currentLesson.status == LessonStatus.COMPLETED ||
        completedSteps.contains(currentIndex) || 
        currentStep is LessonStep.Theory || 
        currentStep is LessonStep.ImageInfo
    }

    val handleNextStep = { 
        if (isNextEnabled) {
            if (currentIndex == steps.size - 1) {
                viewModel.finishLesson(onComplete = onLessonFinished)
            } else {
                viewModel.nextStep()
            }
        }
    }
    
    val handlePrevStep = { 
        if (currentIndex > 0) viewModel.previousStep()
    }

    BackHandler { showExitDialog = true }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text(text = "Напуштање лекције".asScript(), fontFamily = cyrillicFont, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
            text = { Text(text = "Да ли сте сигурни да желите да изађете на главни мени?".asScript(), fontFamily = cyrillicFont, fontSize = 16.sp, lineHeight = 24.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)) },
            containerColor = MaterialTheme.colorScheme.surface,
            confirmButton = { TextButton(onClick = { showExitDialog = false; onExitToMain() }) { Text(text = "Напусти лекцију".asScript(), fontSize = 16.sp, fontFamily = cyrillicFont, color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { showExitDialog = false }) { Text(text = "Остани".asScript(), fontSize = 16.sp, fontFamily = cyrillicFont, fontWeight = FontWeight.Bold, color = GoldBase) } }
        )
    }

    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val swipeThresholdPx = with(LocalDensity.current) { 96.dp.toPx() }
    
    // ИСПРАВКА 2: Обезбеђујемо да pointerInput увек види најновије стање!
    val currentIsNextEnabled by rememberUpdatedState(isNextEnabled)
    val currentHandleNext by rememberUpdatedState(handleNextStep)
    val currentHandlePrev by rememberUpdatedState(handlePrevStep)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(swipeThresholdPx) {
                var swipeOffset = 0f
                detectHorizontalDragGestures(
                    onDragStart = { swipeOffset = 0f },
                    onDragEnd = { 
                        SufaraLogger.log("SWIPE END: Offset = $swipeOffset | Даље дозвољено = $currentIsNextEnabled")
                        
                        if (swipeOffset > swipeThresholdPx) {
                            SufaraLogger.log("SWIPE REGISTROVAN: Идемо НАЗАД")
                            currentHandlePrev() 
                        } else if (swipeOffset < -swipeThresholdPx) {
                            if (currentIsNextEnabled) {
                                SufaraLogger.log("SWIPE REGISTROVAN: Идемо ДАЉЕ")
                                currentHandleNext()
                            } else {
                                SufaraLogger.log("SWIPE ОДБИЈЕН: Даље је закључано!")
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            }
                        }
                        swipeOffset = 0f 
                    },
                    onDragCancel = { swipeOffset = 0f }
                ) { change, dragAmount -> 
                    change.consume()
                    swipeOffset += dragAmount 
                }
            }
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 24.dp, top = 16.dp, bottom = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { showExitDialog = true }) { Icon(imageVector = Icons.Default.Close, contentDescription = "Изађи", tint = MaterialTheme.colorScheme.onBackground) }
            LinearProgressIndicator(progress = progress, modifier = Modifier.weight(1f).height(4.dp), color = GoldBase, trackColor = BlueMidnight)
        }

        AnimatedContent(
            targetState = currentIndex, 
            transitionSpec = { 
                getCardTransition(type = settings.cardAnimation, isForward = targetState > initialState) 
            }, 
            label = "ScreenTransition", 
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) { index ->
            val step = steps[index]
            when (step) {
                is LessonStep.Theory -> TheoryStepScreen(step = step, lessonId = currentLesson.id, lessonTitle = currentLesson.title, symbol = currentLesson.symbol)
                is LessonStep.ImageInfo -> ImageInfoStepScreen(step = step)
                is LessonStep.Quiz -> QuizStepScreen(
                    step = step,
                    onActionComplete = { viewModel.markStepAsCompleted(index) }
                )
                is LessonStep.Example -> ExampleStepScreen(step = step, lessonId = currentLesson.id, symbol = currentLesson.symbol, onActionComplete = { viewModel.markStepAsCompleted(index) })
            }
        }

        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp)) {
            Text(
                text = if (navigationHintVisible) "Решите тренутни задатак да бисте наставили.".asScript() else " ",
                color = GoldBase.copy(alpha = if (navigationHintVisible) 0.78f else 0f),
                fontSize = 12.sp,
                fontFamily = cyrillicFont,
                modifier = Modifier.fillMaxWidth().height(20.dp)
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = handlePrevStep,
                enabled = currentIndex > 0,
                modifier = Modifier.weight(1f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Text(
                    text = "Назад".asScript(),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (currentIndex > 0) 1f else 0.35f),
                    fontSize = 16.sp,
                    fontFamily = cyrillicFont
                )
            }
            GoldenWireButton(
                onClick = handleNextStep,
                onDisabledClick = { navigationHintVisible = true },
                text = if (currentIndex == steps.size - 1) "Заврши".asScript() else "Даље".asScript(),
                font = cyrillicFont,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.weight(1f).height(56.dp),
                enabled = isNextEnabled
            )
            }
        }
    }
}
