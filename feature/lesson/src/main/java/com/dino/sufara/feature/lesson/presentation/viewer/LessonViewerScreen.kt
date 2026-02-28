package com.dino.sufara.feature.lesson.presentation.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
            CircularProgressIndicator()
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
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { if (currentIndex > 0) viewModel.previousStep() else onNavigateBack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Назад", tint = Color.White)
            }
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.weight(1f).height(12.dp).padding(horizontal = 8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surface
            )
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.Close, contentDescription = "Затвори", tint = Color.White)
            }
        }

        when (currentStep) {
            is LessonStep.Theory -> TheoryStepScreen(step = currentStep, lessonId = lesson!!.id, symbol = lesson!!.symbol, onNextClick = handleNextStep)
            is LessonStep.ImageInfo -> ImageInfoStepScreen(step = currentStep, onNextClick = handleNextStep)
            is LessonStep.Quiz -> QuizStepScreen(step = currentStep, stepIndex = currentIndex, onNextClick = handleNextStep)
            is LessonStep.Example -> ExampleStepScreen(step = currentStep, lessonId = lesson!!.id, symbol = lesson!!.symbol, onNextClick = handleNextStep)
        }
    }
}