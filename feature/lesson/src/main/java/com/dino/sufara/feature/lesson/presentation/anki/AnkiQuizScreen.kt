package com.dino.sufara.feature.lesson.presentation.anki

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dino.sufara.core.designsystem.BlueMidnight
import com.dino.sufara.core.designsystem.GoldBase
import com.dino.sufara.core.designsystem.components.SuccessBurst
import com.dino.sufara.feature.lesson.domain.util.asScript
import com.dino.sufara.feature.lesson.presentation.settings.LocalSufaraSettings
import com.dino.sufara.feature.lesson.presentation.settings.SufaraFonts
import com.dino.sufara.feature.lesson.presentation.viewer.components.SufaraText

@Composable
fun AnkiQuizScreen(viewModel: AnkiQuizViewModel, onNavigateBack: () -> Unit) {
    val isLoading by viewModel.isLoading.collectAsState()
    val currentQuestion by viewModel.currentQuestion.collectAsState()
    val remainingCount by viewModel.remainingCount.collectAsState()
    val questionRevision by viewModel.questionRevision.collectAsState()
    val feedbackCorrect by viewModel.feedbackCorrect.collectAsState()
    
    val settings = LocalSufaraSettings.current
    val cyrillicFont = SufaraFonts.getCyrillicFont(settings.cyrillicFont)
    var showExitDialog by remember { mutableStateOf(false) }
    var successTrigger by remember { mutableIntStateOf(0) }
    var rootPosition by remember { mutableStateOf(Offset.Zero) }
    var correctAnswerCenter by remember(currentQuestion?.second?.id) { mutableStateOf<Offset?>(null) }

    BackHandler { showExitDialog = true }
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Напуштање квиза".asScript(), fontFamily = cyrillicFont) },
            text = { Text("Да ли сте сигурни да желите да изађете на главни мени?".asScript(), fontFamily = cyrillicFont) },
            confirmButton = {
                TextButton(onClick = { showExitDialog = false; onNavigateBack() }) {
                    Text("Напусти квиз".asScript(), color = MaterialTheme.colorScheme.error, fontFamily = cyrillicFont)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Остани".asScript(), color = GoldBase, fontWeight = FontWeight.Bold, fontFamily = cyrillicFont)
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize().onGloballyPositioned { rootPosition = it.positionInRoot() }) {
      Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 24.dp, top = 16.dp, bottom = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { showExitDialog = true }) { Icon(Icons.Default.Close, contentDescription = "Затвори".asScript(), tint = MaterialTheme.colorScheme.onBackground) }
            Spacer(modifier = Modifier.width(8.dp))
            if (!isLoading && remainingCount > 0) {
                Text(text = "Преостало: $remainingCount".asScript(), color = GoldBase, fontFamily = cyrillicFont, fontWeight = FontWeight.Bold)
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = GoldBase) }
        } else if (currentQuestion == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                    Text(
                        text = "Обнављање је завршено".asScript(),
                        fontFamily = cyrillicFont, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = GoldBase, textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Нема више доспелих питања.".asScript(),
                        fontFamily = cyrillicFont, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f), textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(48.dp))
                    Button(onClick = onNavigateBack, colors = ButtonDefaults.buttonColors(containerColor = BlueMidnight)) {
                        Text("Назад на главни мени".asScript(), color = GoldBase, modifier = Modifier.padding(8.dp))
                    }
                }
            }
        } else {
            val quiz = currentQuestion!!.second

            AnimatedContent(
                targetState = quiz to questionRevision,
                transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(400)) },
                label = "anki_anim"
            ) { (animatedQuiz, revision) ->
                key(animatedQuiz.id, revision) {
                    val shuffledAnswers = remember { animatedQuiz.answers.shuffled() }
                    var selectedAnswer by remember { mutableStateOf<String?>(null) }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.Top
                    ) {
                        
                        SufaraText(text = animatedQuiz.question, modifier = Modifier.padding(bottom = 32.dp))

                        shuffledAnswers.forEach { answer ->
                            val isCorrect = answer == animatedQuiz.correctAnswer
                            val isSelected = selectedAnswer == answer
                            val hasAnswered = selectedAnswer != null

                            val showCorrect = hasAnswered && isCorrect
                            val showError = hasAnswered && isSelected && !isCorrect
                            val containerColor = when {
                                showCorrect -> Color(0xFF173A2B)
                                showError -> Color(0xFF4A2025)
                                else -> MaterialTheme.colorScheme.surface
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                                    .then(if (isCorrect) Modifier.onGloballyPositioned {
                                        correctAnswerCenter = it.boundsInRoot().center - rootPosition
                                    } else Modifier)
                                    .clickable(enabled = !hasAnswered) {
                                    selectedAnswer = answer
                                    val correct = answer == animatedQuiz.correctAnswer
                                    if (correct) successTrigger++
                                    viewModel.submitAnswer(isCorrect = correct)
                                },
                                colors = CardDefaults.cardColors(containerColor = containerColor)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
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

                        if (selectedAnswer != null) {
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = if (selectedAnswer == animatedQuiz.correctAnswer) {
                                    "Тачно.".asScript()
                                } else {
                                    "Нетачно. Питање се враћа ускоро.".asScript()
                                },
                                color = if (selectedAnswer == animatedQuiz.correctAnswer) Color(0xFFA7E5B8) else Color(0xFFFFA3A3),
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = viewModel::advanceAfterFeedback,
                                enabled = feedbackCorrect != null,
                                modifier = Modifier.fillMaxWidth().height(50.dp)
                            ) {
                                Text(if (feedbackCorrect == null) "Чување...".asScript() else "Настави".asScript())
                            }
                        }
                    }
                }
            }
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
