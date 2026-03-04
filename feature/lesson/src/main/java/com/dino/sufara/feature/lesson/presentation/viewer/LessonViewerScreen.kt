package com.dino.sufara.feature.lesson.presentation.viewer

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
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

@Composable
fun LessonViewerScreen(
    viewModel: LessonViewerViewModel, 
    onNavigateBack: () -> Unit
) {
    val lesson by viewModel.lesson.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val completedSteps by viewModel.completedSteps.collectAsState()
    val settings = LocalSufaraSettings.current
    val cyrillicFont = SufaraFonts.getCyrillicFont(settings.cyrillicFont)

    // УКЛОЊЕН isLoading КОЈИ ЈЕ УБИЈАО АНИМАЦИЈУ!
    var showExitDialog by remember { mutableStateOf(false) }
    var swipeOffset by remember { mutableFloatStateOf(0f) }

    if (lesson == null) {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) { 
            CircularProgressIndicator(color = GoldBase) 
        }
        return
    }

    val steps = lesson!!.steps
    if (steps.isEmpty()) { 
        onNavigateBack()
        return 
    }

    val currentStep = steps[currentIndex]
    val progress = (currentIndex + 1).toFloat() / steps.size.toFloat()

    val isNextEnabled = remember(lesson!!.status, completedSteps, currentIndex, currentStep) {
        lesson!!.status == LessonStatus.COMPLETED || 
        completedSteps.contains(currentIndex) || 
        currentStep is LessonStep.Theory || 
        currentStep is LessonStep.ImageInfo
    }

    val handleNextStep = { 
        if (isNextEnabled) {
            if (currentIndex == steps.size - 1) {
                viewModel.finishLesson(onComplete = onNavigateBack) 
            } else {
                viewModel.nextStep()
            }
        }
    }
    
    val handlePrevStep = { 
        if (currentIndex > 0) {
            viewModel.previousStep() 
        } else {
            showExitDialog = true 
        }
    }

    BackHandler { handlePrevStep() }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text(text = "Напуштање лекције".asScript(), fontFamily = cyrillicFont, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
            text = { Text(text = "Да ли сте сигурни да желите да изађете? Напредак у овој лекцији ће бити изгубљен.".asScript(), fontFamily = cyrillicFont, fontSize = 16.sp, lineHeight = 24.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)) },
            containerColor = MaterialTheme.colorScheme.surface,
            confirmButton = { TextButton(onClick = { showExitDialog = false; onNavigateBack() }) { Text(text = "Да".asScript(), fontSize = 16.sp, fontFamily = cyrillicFont, color = TextSilver.copy(alpha = 0.6f)) } },
            dismissButton = { TextButton(onClick = { showExitDialog = false }) { Text(text = "Не".asScript(), fontSize = 16.sp, fontFamily = cyrillicFont, fontWeight = FontWeight.Bold, color = GoldBase) } }
        )
    }

    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    
    // ИСПРАВКА 2: Обезбеђујемо да pointerInput увек види најновије стање!
    val currentIsNextEnabled by rememberUpdatedState(isNextEnabled)
    val currentHandleNext by rememberUpdatedState(handleNextStep)
    val currentHandlePrev by rememberUpdatedState(handlePrevStep)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = { 
                        SufaraLogger.log("SWIPE END: Offset = $swipeOffset | Даље дозвољено = $currentIsNextEnabled")
                        
                        if (swipeOffset > 250) {
                            SufaraLogger.log("SWIPE REGISTROVAN: Идемо НАЗАД")
                            currentHandlePrev() 
                        } else if (swipeOffset < -250) {
                            if (currentIsNextEnabled) {
                                SufaraLogger.log("SWIPE REGISTROVAN: Идемо ДАЉЕ")
                                currentHandleNext()
                            } else {
                                SufaraLogger.log("SWIPE ОДБИЈЕН: Даље је закључано!")
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            }
                        }
                        swipeOffset = 0f 
                    }
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
                is LessonStep.Theory -> TheoryStepScreen(step = step, lessonId = lesson!!.id, lessonTitle = lesson!!.title, symbol = lesson!!.symbol)
                is LessonStep.ImageInfo -> ImageInfoStepScreen(step = step)
                is LessonStep.Quiz -> QuizStepScreen(step = step, onActionComplete = { viewModel.markStepAsCompleted(index) }, onAutoAdvance = handleNextStep)
                is LessonStep.Example -> ExampleStepScreen(step = step, lessonId = lesson!!.id, symbol = lesson!!.symbol, onActionComplete = { viewModel.markStepAsCompleted(index) })
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = handlePrevStep, modifier = Modifier.weight(1f).height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface)) { 
                Text(text = "Назад".asScript(), color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp, fontFamily = cyrillicFont) 
            }
            GoldenWireButton(onClick = handleNextStep, text = if (currentIndex == steps.size - 1) "Заврши".asScript() else "Даље".asScript(), font = cyrillicFont, fontWeight = FontWeight.Normal, modifier = Modifier.weight(1f).height(56.dp), enabled = isNextEnabled)
        }
    }
}