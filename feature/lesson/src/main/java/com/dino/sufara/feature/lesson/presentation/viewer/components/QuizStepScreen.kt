package com.dino.sufara.feature.lesson.presentation.viewer.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dino.sufara.feature.lesson.domain.model.LessonStep
import com.dino.sufara.feature.lesson.domain.util.asScript
import com.dino.sufara.feature.lesson.presentation.settings.LocalSufaraSettings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun QuizStepScreen(
    step: LessonStep.Quiz,
    stepIndex: Int, 
    onNextClick: () -> Unit
) {
    var attemptCount by remember { mutableIntStateOf(0) }

    key(stepIndex, attemptCount) { 
        val shuffledAnswers = remember { step.answers.shuffled() }
        var selectedAnswer by remember { mutableStateOf<String?>(null) }
        val scope = rememberCoroutineScope()
        val settings = LocalSufaraSettings.current

        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                SufaraText(text = step.question, modifier = Modifier.padding(bottom = 32.dp))

                shuffledAnswers.forEach { answer ->
                    val isCorrect = answer == step.correctAnswer
                    val isSelected = selectedAnswer == answer
                    val hasAnswered = selectedAnswer != null

                    val containerColor = when {
                        hasAnswered && isCorrect -> Color(0xFF4CAF50)
                        hasAnswered && isSelected && !isCorrect -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.surface
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .clickable(enabled = !hasAnswered) {
                                selectedAnswer = answer
                                if (answer == step.correctAnswer) {
                                    scope.launch { delay(1000); onNextClick() } 
                                }
                            },
                        colors = CardDefaults.cardColors(containerColor = containerColor)
                    ) {
                        SufaraText(text = answer, modifier = Modifier.padding(16.dp))
                    }
                }

                if (settings.isDebugMode) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onNextClick, 
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("ПРЕСКОЧИ КВИЗ (Debug)".asScript())
                    }
                }
            }

            if (selectedAnswer != null && selectedAnswer != step.correctAnswer) {
                Box(
                    modifier = Modifier.fillMaxSize().clickable(
                        interactionSource = remember { MutableInteractionSource() }, indication = null
                    ) {
                        selectedAnswer = null
                        attemptCount++ 
                    }
                ) {
                    Text(
                        text = "Додирни било где за поновни покушај".asScript(),
                        color = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.align(Alignment.BottomCenter).padding(32.dp)
                    )
                }
            }
        }
    }
}