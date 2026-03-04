package com.dino.sufara.feature.lesson.presentation.viewer

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dino.sufara.core.designsystem.BlueMidnight
import com.dino.sufara.core.designsystem.GoldBase
import com.dino.sufara.core.designsystem.components.GoldenWireButton
import com.dino.sufara.feature.lesson.domain.model.LessonStep
import com.dino.sufara.feature.lesson.domain.util.asScript
import com.dino.sufara.feature.lesson.presentation.settings.LocalSufaraSettings
import com.dino.sufara.feature.lesson.presentation.settings.SufaraFonts
import com.dino.sufara.feature.lesson.presentation.viewer.animations.getScreenTransition
import com.dino.sufara.feature.lesson.presentation.viewer.components.*

@Composable
fun LessonViewerScreen(
    viewModel: LessonViewerViewModel,
    onNavigateBack: () -> Unit
) {
    val lesson by viewModel.lesson.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val settings = LocalSufaraSettings.current
    val cyrillicFont = SufaraFonts.getCyrillicFont(settings.cyrillicFont)

    if (lesson == null) {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = GoldBase)
        }
        return
    }

    val steps = lesson!!.steps
    if (steps.isEmpty()) { onNavigateBack(); return }

    val currentStep = steps[currentIndex]
    val progress = (currentIndex + 1).toFloat() / steps.size.toFloat()

    val handleNextStep = { if (currentIndex == steps.size - 1) onNavigateBack() else viewModel.nextStep() }
    val handlePrevStep = { if (currentIndex > 0) viewModel.previousStep() else onNavigateBack() }

    BackHandler(enabled = currentIndex > 0) { handlePrevStep() }

    var swipeOffset by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (swipeOffset > 150) handlePrevStep()
                        else if (swipeOffset < -150) handleNextStep()
                        swipeOffset = 0f
                    }
                ) { change, dragAmount -> change.consume(); swipeOffset += dragAmount }
            }
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            LinearProgressIndicator(progress = progress, modifier = Modifier.weight(1f).height(4.dp), color = GoldBase, trackColor = BlueMidnight)
        }

        AnimatedContent(
            targetState = Pair(currentIndex, currentStep),
            transitionSpec = { getScreenTransition(targetState.first > initialState.first) },
            label = "ScreenTransition",
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) { (index, step) ->
            when (step) {
                is LessonStep.Theory -> TheoryStepScreen(step = step, lessonId = lesson!!.id, lessonTitle = lesson!!.title, symbol = lesson!!.symbol)
                is LessonStep.ImageInfo -> ImageInfoStepScreen(step = step)
                is LessonStep.Quiz -> QuizStepScreen(step = step, onAutoAdvance = handleNextStep)
                is LessonStep.Example -> ExampleStepScreen(step = step, lessonId = lesson!!.id, symbol = lesson!!.symbol)
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = handlePrevStep, 
                modifier = Modifier.weight(1f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface)
            ) { Text("Nazad".asScript(), color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp, fontFamily = cyrillicFont) }
            
            GoldenWireButton(
                onClick = handleNextStep, text = "Dalje".asScript(), font = cyrillicFont, modifier = Modifier.weight(1f).height(56.dp)
            )
        }
    }
}