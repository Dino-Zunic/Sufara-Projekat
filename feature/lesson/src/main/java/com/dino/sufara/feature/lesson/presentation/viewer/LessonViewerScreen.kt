package com.dino.sufara.feature.lesson.presentation.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dino.sufara.core.designsystem.BlueMidnight
import com.dino.sufara.core.designsystem.GoldBase
import com.dino.sufara.feature.lesson.domain.model.LessonStep
import com.dino.sufara.feature.lesson.presentation.viewer.components.ExampleStepScreen
import com.dino.sufara.feature.lesson.presentation.viewer.components.ImageInfoStepScreen
import com.dino.sufara.feature.lesson.presentation.viewer.components.QuizStepScreen
import com.dino.sufara.feature.lesson.presentation.viewer.components.TheoryStepScreen

@Composable
fun LessonViewerScreen(
    viewModel: LessonViewerViewModel,
    onNavigateBack: () -> Unit
) {
    val lesson by viewModel.lesson.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()

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

    val handleNextStep = {
        if (currentIndex == steps.size - 1) onNavigateBack() else viewModel.nextStep()
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.weight(1f).height(4.dp), 
                color = GoldBase, 
                trackColor = BlueMidnight 
            )
        }

        when (currentStep) {
            is LessonStep.Theory -> TheoryStepScreen(step = currentStep, lessonId = lesson!!.id, symbol = lesson!!.symbol, onNextClick = handleNextStep)
            is LessonStep.ImageInfo -> ImageInfoStepScreen(step = currentStep, onNextClick = handleNextStep)
            is LessonStep.Quiz -> QuizStepScreen(step = currentStep, stepIndex = currentIndex, onNextClick = handleNextStep)
            // ИСПРАВКА: Овде је додат stepIndex = currentIndex
            is LessonStep.Example -> ExampleStepScreen(
                step = currentStep, 
                lessonId = lesson!!.id, 
                symbol = lesson!!.symbol, 
                stepIndex = currentIndex,
                onNextClick = handleNextStep,
                onPrevClick = { viewModel.previousStep() } 
            )
        }
    }
}