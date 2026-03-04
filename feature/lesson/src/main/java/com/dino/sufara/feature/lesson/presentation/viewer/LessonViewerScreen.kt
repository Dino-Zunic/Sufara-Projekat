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
import androidx.compose.ui.graphics.Color
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
import com.dino.sufara.feature.lesson.domain.util.asScript
import com.dino.sufara.feature.lesson.presentation.settings.LocalSufaraSettings
import com.dino.sufara.feature.lesson.presentation.settings.SufaraFonts
// ИСПРАВКА: Вратили смо стару логику за анимације!
import com.dino.sufara.feature.lesson.presentation.viewer.animations.getCardTransition
import com.dino.sufara.feature.lesson.presentation.viewer.components.*
import kotlinx.coroutines.delay

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

    var isLoading by remember(currentIndex) { mutableStateOf(true) }
    var showExitDialog by remember { mutableStateOf(false) }
    var swipeOffset by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(currentIndex) { 
        delay(150)
        isLoading = false 
    }

    if (lesson == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background), 
            contentAlignment = Alignment.Center
        ) { 
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

    BackHandler { 
        handlePrevStep() 
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { 
                Text(
                    text = "Napuštanje lekcije".asScript(), 
                    fontFamily = cyrillicFont, 
                    fontSize = 22.sp, 
                    fontWeight = FontWeight.Bold, 
                    color = MaterialTheme.colorScheme.onSurface
                ) 
            },
            text = { 
                Text(
                    text = "Da li ste sigurni da želite da izađete? Napredak u ovoj lekciji će biti izgubljen.".asScript(), 
                    fontFamily = cyrillicFont, 
                    fontSize = 16.sp, 
                    lineHeight = 24.sp, 
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                ) 
            },
            containerColor = MaterialTheme.colorScheme.surface,
            confirmButton = { 
                TextButton(
                    onClick = { 
                        showExitDialog = false
                        onNavigateBack() 
                    }
                ) { 
                    Text(
                        text = "Da".asScript(), 
                        fontSize = 16.sp, 
                        fontFamily = cyrillicFont, 
                        color = TextSilver.copy(alpha = 0.6f)
                    ) 
                } 
            },
            dismissButton = { 
                TextButton(
                    onClick = { showExitDialog = false }
                ) { 
                    Text(
                        text = "Ne".asScript(), 
                        fontSize = 16.sp, 
                        fontFamily = cyrillicFont, 
                        fontWeight = FontWeight.Bold, 
                        color = GoldBase
                    ) 
                } 
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = { 
                        if (swipeOffset > 150) handlePrevStep() 
                        else if (swipeOffset < -150 && isNextEnabled) handleNextStep()
                        swipeOffset = 0f 
                    }
                ) { change, dragAmount -> 
                    change.consume()
                    swipeOffset += dragAmount 
                }
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 24.dp, top = 16.dp, bottom = 16.dp), 
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { showExitDialog = true }) { 
                Icon(
                    imageVector = Icons.Default.Close, 
                    contentDescription = "Izađi", 
                    tint = MaterialTheme.colorScheme.onBackground
                ) 
            }
            LinearProgressIndicator(
                progress = progress, 
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp), 
                color = GoldBase, 
                trackColor = BlueMidnight
            )
        }

        if (isLoading) { 
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) 
        } else {
            AnimatedContent(
                targetState = Pair(currentIndex, currentStep), 
                transitionSpec = { 
                    // ИСПРАВКА: Поново користимо опцију из подешавања!
                    getCardTransition(
                        type = settings.cardAnimation, 
                        isForward = targetState.first > initialState.first
                    ) 
                }, 
                label = "ScreenTransition", 
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { (index, step) ->
                when (step) {
                    is LessonStep.Theory -> TheoryStepScreen(
                        step = step, 
                        lessonId = lesson!!.id, 
                        lessonTitle = lesson!!.title, 
                        symbol = lesson!!.symbol
                    )
                    is LessonStep.ImageInfo -> ImageInfoStepScreen(
                        step = step
                    )
                    is LessonStep.Quiz -> QuizStepScreen(
                        step = step, 
                        onActionComplete = { viewModel.markStepAsCompleted(index) }, 
                        onAutoAdvance = handleNextStep
                    )
                    is LessonStep.Example -> ExampleStepScreen(
                        step = step, 
                        lessonId = lesson!!.id, 
                        symbol = lesson!!.symbol, 
                        onActionComplete = { viewModel.markStepAsCompleted(index) }
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp), 
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = handlePrevStep, 
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp), 
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface)
            ) { 
                Text(
                    text = "Nazad".asScript(), 
                    color = MaterialTheme.colorScheme.onSurface, 
                    fontSize = 16.sp, 
                    fontFamily = cyrillicFont
                ) 
            }
            
            GoldenWireButton(
                onClick = handleNextStep, 
                text = if (currentIndex == steps.size - 1) "Završi".asScript() else "Dalje".asScript(), 
                font = cyrillicFont, 
                fontWeight = FontWeight.Normal, 
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp), 
                enabled = isNextEnabled
            )
        }
    }
}