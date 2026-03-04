package com.dino.sufara.feature.lesson.presentation.anki

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dino.sufara.core.designsystem.BlueMidnight
import com.dino.sufara.core.designsystem.GoldBase
import com.dino.sufara.feature.lesson.domain.util.asScript
import com.dino.sufara.feature.lesson.presentation.settings.LocalSufaraSettings
import com.dino.sufara.feature.lesson.presentation.settings.SufaraFonts
import com.dino.sufara.feature.lesson.presentation.viewer.components.SufaraText
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AnkiQuizScreen(viewModel: AnkiQuizViewModel, onNavigateBack: () -> Unit) {
    val isLoading by viewModel.isLoading.collectAsState()
    val currentQuestion by viewModel.currentQuestion.collectAsState()
    val remainingCount by viewModel.remainingCount.collectAsState()
    
    val settings = LocalSufaraSettings.current
    val cyrillicFont = SufaraFonts.getCyrillicFont(settings.cyrillicFont)

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // --- HEADER ---
        Row(modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 24.dp, top = 16.dp, bottom = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNavigateBack) { Icon(Icons.Default.Close, contentDescription = "Затвори".asScript(), tint = MaterialTheme.colorScheme.onBackground) }
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
                    Text(text = "🎉", fontSize = 64.sp, modifier = Modifier.padding(bottom = 16.dp))
                    Text(
                        text = "Успешно сте обновили градиво!".asScript(),
                        fontFamily = cyrillicFont, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = GoldBase, textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Сва доспела питања су решена. Можете наставити са учењем нових лекција.".asScript(),
                        fontFamily = cyrillicFont, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f), textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(48.dp))
                    Button(onClick = onNavigateBack, colors = ButtonDefaults.buttonColors(containerColor = BlueMidnight)) {
                        Text("Назад на главни мени".asScript(), color = GoldBase, modifier = Modifier.padding(8.dp))
                    }
                }
            }
        } else {
            // --- KVIZ EKRAN ---
            val quiz = currentQuestion!!.second
            var attemptCount by remember { mutableIntStateOf(0) }
            
            key(quiz.id, attemptCount) {
                val shuffledAnswers = remember { quiz.answers.shuffled() }
                var selectedAnswer by remember { mutableStateOf<String?>(null) }
                val scope = rememberCoroutineScope()

                AnimatedContent(targetState = quiz, transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(400)) }, label = "anki_anim") { _ ->
                    Column(modifier = Modifier.fillMaxSize().padding(start = 24.dp, end = 24.dp, top = 24.dp), verticalArrangement = Arrangement.Top) {
                        
                        SufaraText(text = quiz.question, modifier = Modifier.padding(bottom = 32.dp))

                        shuffledAnswers.forEach { answer ->
                            val isCorrect = answer == quiz.correctAnswer
                            val isSelected = selectedAnswer == answer
                            val hasAnswered = selectedAnswer != null

                            val containerColor = when {
                                hasAnswered && isCorrect -> Color(0xFF4CAF50)
                                hasAnswered && isSelected && !isCorrect -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.surface
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).clickable(enabled = !hasAnswered) {
                                    selectedAnswer = answer
                                    scope.launch { 
                                        delay(1000) 
                                        viewModel.submitAnswer(isCorrect = answer == quiz.correctAnswer)
                                    }
                                },
                                colors = CardDefaults.cardColors(containerColor = containerColor)
                            ) {
                                SufaraText(text = answer, modifier = Modifier.padding(16.dp))
                            }
                        }

                        if (selectedAnswer != null && selectedAnswer != quiz.correctAnswer) {
                            Box(modifier = Modifier.fillMaxWidth().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                                selectedAnswer = null
                                attemptCount++ 
                            }) {
                                Text(
                                    text = "Додирните било где за поновни покушај".asScript(),
                                    color = Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.align(Alignment.Center).padding(top = 32.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}