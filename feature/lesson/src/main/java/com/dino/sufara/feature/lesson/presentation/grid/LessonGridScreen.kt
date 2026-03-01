package com.dino.sufara.feature.lesson.presentation.grid

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dino.sufara.core.designsystem.TextMutedGold
import com.dino.sufara.core.designsystem.TextParchment
import com.dino.sufara.core.designsystem.TextSilver
import com.dino.sufara.feature.lesson.domain.model.Lesson
import com.dino.sufara.feature.lesson.presentation.settings.BodyTextColorTheme
import com.dino.sufara.feature.lesson.presentation.settings.LocalSufaraSettings
import com.dino.sufara.feature.lesson.presentation.settings.SufaraFonts
import kotlin.math.hypot
import kotlin.math.sin

@Composable
fun LessonGridScreen(
    viewModel: LessonGridViewModel,
    onLessonClick: (String) -> Unit
) {
    val lessons by viewModel.lessons.collectAsState()
    val settings = LocalSufaraSettings.current
    var showTrajectory by remember { mutableStateOf(false) }
    val density = LocalDensity.current

    val bodyColor = when(settings.bodyTextColorTheme) {
        BodyTextColorTheme.PARCHMENT -> TextParchment
        BodyTextColorTheme.SILVER -> TextSilver
        BodyTextColorTheme.MUTED_GOLD -> TextMutedGold
    }

    // --- МАТЕМАТИКА (ARC LENGTH INTEGRAL APPROXIMATION) ---
    // Овај блок се покреће само једном да израчуна савршене размаке по кривој!
    val nodeYs = remember(lessons.size, density) {
        val spacing = with(density) { 180.dp.toPx() } // Константна дужина лука (Arc Length) између лекција
        val amplitude = with(density) { 100.dp.toPx() }
        val freq = 1.6f / spacing // Фреквенција подешена за брже вијугање
        
        fun getSineX(y: Float) = amplitude * sin(y * freq)
        
        val ys = mutableListOf<Float>()
        var currY = 0f
        ys.add(currY)
        
        for (i in 1 until lessons.size) {
            var stepY = currY
            var arcLen = 0f
            var prevX = getSineX(currY)
            var prevY = currY
            
            // "Пешачимо" милиметар по милиметар док не пређемо жељену дужину лука
            while (arcLen < spacing) {
                stepY += 1f
                val newX = getSineX(stepY)
                arcLen += hypot(newX - prevX, stepY - prevY)
                prevX = newX
                prevY = stepY
            }
            currY = stepY
            ys.add(currY)
        }
        ys
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 64.dp, bottom = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(lessons.size) { index ->
                val lesson = lessons[index]
                val startGlobalY = nodeYs[index]
                // Последња лекција добија вештачки простор испод себе
                val endGlobalY = nodeYs.getOrNull(index + 1) ?: (startGlobalY + with(density) { 180.dp.toPx() })
                val deltaY = endGlobalY - startGlobalY
                
                LessonPathNode(
                    lesson = lesson,
                    index = index,
                    isLast = index == lessons.size - 1,
                    startGlobalY = startGlobalY,
                    endGlobalY = endGlobalY,
                    deltaY = deltaY,
                    bodyColor = bodyColor,
                    showTrajectory = showTrajectory,
                    onClick = { onLessonClick(lesson.id) }
                )
            }
        }

        if (settings.isDebugMode) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), shape = CircleShape)
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Путања", color = bodyColor, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Switch(checked = showTrajectory, onCheckedChange = { showTrajectory = it }, modifier = Modifier.scale(0.7f))
            }
        }
    }
}

@Composable
fun LessonPathNode(
    lesson: Lesson,
    index: Int,
    isLast: Boolean,
    startGlobalY: Float,
    endGlobalY: Float,
    deltaY: Float,
    bodyColor: Color,
    showTrajectory: Boolean,
    onClick: () -> Unit
) {
    val arabicFont = SufaraFonts.getArabicFont(LocalSufaraSettings.current.arabicFont)
    val density = LocalDensity.current
    
    val amplitude = with(density) { 100.dp.toPx() }
    val spacing = with(density) { 180.dp.toPx() }
    val freq = 1.6f / spacing
    fun getSineX(y: Float) = amplitude * sin(y * freq)
    
    val itemHeightDp = with(density) { deltaY.toDp() }
    val circleRadiusPx = with(density) { 38.dp.toPx() } // Пола од 76.dp
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(itemHeightDp),
        contentAlignment = Alignment.TopCenter // Центрирано на врх да би се лекције лепо настављале
    ) {
        // ДОЊИ СЛОЈ: Права синусоида и равномерне тачкице
        if (!isLast) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerX = size.width / 2

                // Цртамо црвену путању
                if (showTrajectory) {
                    val path = Path()
                    path.moveTo(centerX + getSineX(startGlobalY), circleRadiusPx)
                    val steps = 40
                    for (step in 1..steps) {
                        val f = step.toFloat() / steps
                        val globalY = startGlobalY + f * deltaY
                        val localY = circleRadiusPx + (globalY - startGlobalY)
                        path.lineTo(centerX + getSineX(globalY), localY)
                    }
                    drawPath(path = path, color = Color.Red.copy(alpha = 0.5f), style = Stroke(width = 4.dp.toPx()))
                }

                // Тачкице распоређене тачно по математичком луку!
                val dotSpacing = spacing / 5 // Желимо 4 тачке између лекција
                var nextDotTarget = dotSpacing
                var arcLen = 0f
                var currY = startGlobalY
                var prevX = getSineX(startGlobalY)
                var prevY = startGlobalY
                
                while(currY < endGlobalY) {
                    currY += 2f // Брзи корак за рачунање тачкица
                    val newX = getSineX(currY)
                    arcLen += hypot(newX - prevX, currY - prevY)
                    if (arcLen >= nextDotTarget && nextDotTarget < spacing) {
                        val localY = circleRadiusPx + (currY - startGlobalY)
                        drawCircle(
                            color = bodyColor.copy(alpha = 0.4f),
                            radius = 4.dp.toPx(),
                            center = Offset(centerX + newX, localY)
                        )
                        nextDotTarget += dotSpacing
                    }
                    prevX = newX
                    prevY = currY
                }
            }
        }

        // ГОРЊИ СЛОЈ: Круг лекције
        Column(
            modifier = Modifier
                .offset(x = with(density) { getSineX(startGlobalY).toDp() })
                .width(130.dp), 
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                if (lesson.symbol == "📖" || lesson.symbol == ".") {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Основа",
                        tint = bodyColor,
                        modifier = Modifier.size(36.dp)
                    )
                } else {
                    Text(
                        text = lesson.symbol,
                        fontFamily = arabicFont,
                        fontSize = 36.sp,
                        color = bodyColor,
                        modifier = Modifier.padding(bottom = 6.dp) 
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Текст са заштитном сенком да би се одвајао од путање
            Text(
                text = lesson.title,
                color = bodyColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    shadow = Shadow(
                        color = MaterialTheme.colorScheme.background, // Сенка је боје позадине
                        blurRadius = 16f
                    )
                )
            )
        }
    }
}