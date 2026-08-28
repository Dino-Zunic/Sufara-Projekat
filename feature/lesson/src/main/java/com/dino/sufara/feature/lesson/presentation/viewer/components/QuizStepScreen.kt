package com.dino.sufara.feature.lesson.presentation.viewer.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dino.sufara.core.designsystem.GoldBase
import com.dino.sufara.core.designsystem.components.SuccessBurst
import com.dino.sufara.feature.lesson.domain.model.LessonStep
import com.dino.sufara.feature.lesson.domain.util.reshuffleAnswers
import com.dino.sufara.feature.lesson.domain.util.asScript
import com.dino.sufara.feature.lesson.presentation.settings.LocalSufaraSettings

@Composable
fun QuizStepScreen(step: LessonStep.Quiz, onActionComplete: () -> Unit) {
    var shuffledAnswers by remember(step.id) { mutableStateOf(reshuffleAnswers(step.answers)) }
    var selectedAnswer by remember(step.id) { mutableStateOf<String?>(null) }
    var successTrigger by remember(step.id) { mutableIntStateOf(0) }
    var rootPosition by remember(step.id) { mutableStateOf(Offset.Zero) }
    var correctAnswerCenter by remember(step.id) { mutableStateOf<Offset?>(null) }
    val settings = LocalSufaraSettings.current
    val hasAnswered = selectedAnswer != null

    Box(
        Modifier
            .fillMaxSize()
            .onGloballyPositioned { rootPosition = it.positionInRoot() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, top = 40.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.Top
        ) {
            SufaraText(text = step.question, modifier = Modifier.padding(bottom = 32.dp))

            shuffledAnswers.forEach { answer ->
                val isCorrect = answer == step.correctAnswer
                val isSelected = selectedAnswer == answer
                val showCorrect = hasAnswered && isCorrect
                val showError = hasAnswered && isSelected && !isCorrect
                val containerColor = when {
                    showCorrect -> Color(0xFF173A2B)
                    showError -> Color(0xFF4A2025)
                    else -> MaterialTheme.colorScheme.surface
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .then(if (isCorrect) Modifier.onGloballyPositioned {
                            correctAnswerCenter = it.boundsInRoot().center - rootPosition
                        } else Modifier)
                        .clickable(enabled = !hasAnswered) {
                            selectedAnswer = answer
                            if (isCorrect) {
                                successTrigger++
                                onActionComplete()
                            }
                        },
                    colors = CardDefaults.cardColors(containerColor = containerColor)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SufaraText(text = answer, modifier = Modifier.weight(1f))
                        when {
                            showCorrect -> {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFFA7E5B8))
                                Text("Тачно".asScript(), color = Color(0xFFA7E5B8), fontWeight = FontWeight.Bold)
                            }
                            showError -> {
                                Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFFFFA3A3))
                                Text("Нетачно".asScript(), color = Color(0xFFFFA3A3), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            if (selectedAnswer != null && selectedAnswer != step.correctAnswer) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Одговор није тачан. Покушајте поново.".asScript(),
                    color = Color(0xFFFFA3A3),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        shuffledAnswers = reshuffleAnswers(step.answers, shuffledAnswers)
                        selectedAnswer = null
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Покушај поново".asScript())
                }
            } else if (selectedAnswer == step.correctAnswer) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Тачно.".asScript(),
                    color = GoldBase,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }

        SuccessBurst(
            trigger = successTrigger,
            enabled = settings.successBurstStyle.count > 0,
            modifier = Modifier.fillMaxSize(),
            particleCount = settings.successBurstStyle.count,
            origin = correctAnswerCenter
        )
    }
}
