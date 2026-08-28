package com.dino.sufara.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
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
    onCourseClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAnkiClick: () -> Unit
) {
    val settings = LocalSufaraSettings.current
    val cyrillicFont = SufaraFonts.getCyrillicFont(settings.cyrillicFont)
    
    var totalLessons by remember { mutableIntStateOf(0) }
    var completedLessons by remember { mutableIntStateOf(0) }
    var progressRefresh by remember { mutableIntStateOf(0) }
    
    var allFacts by remember { mutableStateOf(listOf<String>()) }
    var shuffledBag by remember { mutableStateOf(emptyList<String>()) }
    var factIndex by remember { mutableIntStateOf(0) }
    
    val currentFact = if (shuffledBag.isNotEmpty()) shuffledBag[factIndex] else "Учитавање..."

    val showNextFact = {
        if (shuffledBag.isNotEmpty() && allFacts.isNotEmpty()) {
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
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) progressRefresh++
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        val loadedFacts = repository.getFunFacts()
        if (loadedFacts.isNotEmpty()) {
            allFacts = loadedFacts
            shuffledBag = loadedFacts.shuffled()
        }
    }

    LaunchedEffect(progressRefresh) {
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Default.Settings, contentDescription = "Подешавања".asScript(), tint = GoldBase)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

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
                    
                    Column(modifier = Modifier.fillMaxWidth().widthIn(max = 320.dp)) {
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
                
                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = "Курс".asScript(),
                    color = TextSilver.copy(alpha = 0.72f),
                    fontFamily = cyrillicFont,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                GoldenWireButton(
                    onClick = onCourseClick,
                    text = "Отвори мапу".asScript(),
                    font = cyrillicFont,
                    modifier = Modifier.fillMaxWidth().widthIn(max = 320.dp).height(54.dp)
                )

                Spacer(Modifier.height(18.dp))

                OutlinedButton(
                    onClick = onAnkiClick,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldBase),
                    modifier = Modifier.fillMaxWidth().widthIn(max = 320.dp).height(48.dp)
                ) {
                    Text("Обнови градиво".asScript(), fontFamily = cyrillicFont, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
        }

        Spacer(modifier = Modifier.height(30.dp))

        Column(modifier = Modifier.fillMaxWidth().widthIn(max = 520.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Divider(color = TextSilver.copy(alpha = 0.4f), thickness = 1.dp)

                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val displayedFacts = allFacts.map { it.asScript() }
                    val factStyle = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = cyrillicFont,
                        textAlign = TextAlign.Center,
                        lineHeight = 21.sp
                    )
                    val textMeasurer = rememberTextMeasurer(cacheSize = displayedFacts.size.coerceAtLeast(8))
                    val availableWidthPx = constraints.maxWidth.coerceAtLeast(1)
                    val tallestFactPx = remember(displayedFacts, factStyle, availableWidthPx) {
                        displayedFacts.maxOfOrNull { fact ->
                            textMeasurer.measure(
                                text = fact,
                                style = factStyle,
                                constraints = Constraints(maxWidth = availableWidthPx)
                            ).size.height
                        } ?: 0
                    }
                    val panelHeight = with(androidx.compose.ui.platform.LocalDensity.current) {
                        tallestFactPx.toDp() + 52.dp
                    }.coerceAtLeast(136.dp)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(panelHeight)
                            .clickable { showNextFact() },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Занимљивости".asScript(),
                                color = GoldBase.copy(alpha = 0.62f),
                                fontFamily = cyrillicFont,
                                fontSize = 11.sp
                            )
                            AnimatedContent(targetState = currentFact, transitionSpec = { fadeIn(tween(450)) togetherWith fadeOut(tween(450)) }, label = "FactAnim") { fact ->
                                Text(
                                    text = fact.asScript(),
                                    color = TextSilver.copy(alpha = 0.9f),
                                    style = factStyle,
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 10.dp)
                                )
                            }
                        }
                    }
                }
                
                Divider(color = TextSilver.copy(alpha = 0.4f), thickness = 1.dp)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
