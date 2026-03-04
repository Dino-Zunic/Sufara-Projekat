package com.dino.sufara.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dino.sufara.core.designsystem.*
import com.dino.sufara.core.designsystem.components.GoldenWireButton
import com.dino.sufara.feature.lesson.domain.model.LessonStatus
import com.dino.sufara.feature.lesson.domain.repository.LessonRepository
import com.dino.sufara.feature.lesson.domain.util.asScript
import com.dino.sufara.feature.lesson.presentation.settings.LocalSufaraSettings
import com.dino.sufara.feature.lesson.presentation.settings.SufaraFonts
import kotlinx.coroutines.delay

@Composable
fun MainMenuScreen(
    repository: LessonRepository,
    onStartClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val settings = LocalSufaraSettings.current
    val cyrillicFont = SufaraFonts.getCyrillicFont(settings.cyrillicFont)
    
    var totalLessons by remember { mutableIntStateOf(0) }
    var completedLessons by remember { mutableIntStateOf(0) }
    
    var allFacts by remember { mutableStateOf(listOf<String>()) }
    var shuffledBag by remember { mutableStateOf(emptyList<String>()) }
    var factIndex by remember { mutableIntStateOf(0) }
    
    val currentFact = if (shuffledBag.isNotEmpty()) shuffledBag[factIndex] else "Учитавање..."

    val showNextFact = {
        if (factIndex >= shuffledBag.size - 1) {
            val lastFact = shuffledBag.last()
            var newBag = allFacts.shuffled()
            while (newBag.first() == lastFact && allFacts.size > 1) {
                newBag = allFacts.shuffled()
            }
            shuffledBag = newBag
            factIndex = 0
        } else {
            factIndex++
        }
    }

    LaunchedEffect(Unit) {
        val loadedFacts = repository.getFunFacts()
        if (loadedFacts.isNotEmpty()) {
            allFacts = loadedFacts
            shuffledBag = loadedFacts.shuffled() 
        }
        val all = repository.getAllLessons()
        totalLessons = all.size
        completedLessons = all.count { it.status == LessonStatus.COMPLETED }
    }

    // ИСПРАВКА: Време промене чињенице повећано на 2 минута (120,000 ms)
    LaunchedEffect(factIndex, shuffledBag) {
        if (shuffledBag.isNotEmpty()) {
            delay(120_000L)
            showNextFact()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        
        IconButton(onClick = onSettingsClick, modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)) {
            Icon(Icons.Default.Settings, contentDescription = "Подешавања".asScript(), tint = GoldBase)
        }

        Column(modifier = Modifier.fillMaxSize()) {
            
            Spacer(modifier = Modifier.weight(0.2f))

            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                val metallicGoldBrush = remember { Brush.verticalGradient(listOf(GoldLight, GoldBase, GoldDark)) }
                val titleShadow = remember { Shadow(color = GoldBase.copy(alpha = 0.5f), blurRadius = 24f) }

                Text(
                    text = "Суфара".asScript(),
                    style = TextStyle(brush = metallicGoldBrush, shadow = titleShadow, fontSize = 64.sp, fontFamily = cyrillicFont, fontWeight = FontWeight.Bold)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                if (totalLessons > 0) {
                    val progressRatio = completedLessons.toFloat() / totalLessons.toFloat()
                    val percentage = (progressRatio * 100).toInt()
                    
                    Column(modifier = Modifier.fillMaxWidth(0.6f)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Напредак".asScript() + ": $completedLessons/$totalLessons", color = TextSilver.copy(alpha = 0.8f), fontSize = 12.sp, fontFamily = cyrillicFont)
                            Text("$percentage%", color = GoldBase, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = cyrillicFont)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = progressRatio,
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = GoldBase, trackColor = BlueMidnight
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(48.dp))

                GoldenWireButton(onClick = onStartClick, text = "Покрени лекције".asScript(), font = cyrillicFont)
            }

            Spacer(modifier = Modifier.height(64.dp))

            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Divider(color = TextSilver.copy(alpha = 0.4f), thickness = 1.dp)
                
                // ИСПРАВКА: Alignment.Center осигурава да кратке чињенице буду вертикално на средини овог контејнера
                Box(
                    modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 120.dp).clickable { showNextFact() },
                    contentAlignment = Alignment.Center 
                ) {
                    AnimatedContent(targetState = currentFact, transitionSpec = { fadeIn(tween(800)) togetherWith fadeOut(tween(800)) }, label = "FactAnim") { fact ->
                        Text(
                            text = fact.asScript(), 
                            color = TextSilver.copy(alpha = 0.9f), 
                            fontSize = 14.sp, 
                            fontFamily = cyrillicFont, 
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                        )
                    }
                }
                
                Divider(color = TextSilver.copy(alpha = 0.4f), thickness = 1.dp)
            }

            Spacer(modifier = Modifier.weight(1f)) 
        }
    }
}