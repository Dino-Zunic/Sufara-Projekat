package com.dino.sufara.feature.lesson.presentation.grid

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dino.sufara.core.designsystem.GoldBase
import com.dino.sufara.core.designsystem.GoldLight
import com.dino.sufara.core.designsystem.TextMutedGold
import com.dino.sufara.core.designsystem.TextParchment
import com.dino.sufara.core.designsystem.TextSilver
import com.dino.sufara.feature.lesson.domain.model.Lesson
import com.dino.sufara.feature.lesson.domain.model.LessonStatus
import com.dino.sufara.feature.lesson.domain.util.asScript
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
    val density = LocalDensity.current

    val bodyColor = when(settings.bodyTextColorTheme) {
        BodyTextColorTheme.PARCHMENT -> TextParchment
        BodyTextColorTheme.SILVER -> TextSilver
        BodyTextColorTheme.MUTED_GOLD -> TextMutedGold
    }

    val nodeYs = remember(lessons.size, density) {
        val spacing = with(density) { 180.dp.toPx() } 
        val amplitude = with(density) { 100.dp.toPx() }
        val freq = 1.6f / spacing 
        
        fun getSineX(y: Float) = amplitude * sin(y * freq)
        
        val ys = mutableListOf<Float>()
        var currY = 0f
        ys.add(currY)
        
        for (i in 1 until lessons.size) {
            var stepY = currY
            var arcLen = 0f
            var prevX = getSineX(currY)
            var prevY = currY
            
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
            itemsIndexed(
                items = lessons,
                key = { _, lesson -> lesson.id }
            ) { index, lesson ->
                
                val startGlobalY = nodeYs.getOrElse(index) { 0f }
                val endGlobalY = nodeYs.getOrNull(index + 1) ?: (startGlobalY + with(density) { 180.dp.toPx() })
                val deltaY = endGlobalY - startGlobalY
                
                LessonPathNode(
                    lesson = lesson,
                    isLast = index == lessons.size - 1,
                    startGlobalY = startGlobalY,
                    endGlobalY = endGlobalY,
                    deltaY = deltaY,
                    bodyColor = bodyColor,
                    onClick = { onLessonClick(lesson.id) }
                )
            }
        }
    }
}

@Composable
fun LessonPathNode(
    lesson: Lesson,
    isLast: Boolean,
    startGlobalY: Float,
    endGlobalY: Float,
    deltaY: Float,
    bodyColor: Color,
    onClick: () -> Unit
) {
    val arabicFont = SufaraFonts.getArabicFont("Noto Naskh")
    val density = LocalDensity.current
    
    val amplitude = with(density) { 100.dp.toPx() }
    val spacing = with(density) { 180.dp.toPx() }
    val freq = 1.6f / spacing
    fun getSineX(y: Float) = amplitude * sin(y * freq)
    
    val itemHeightDp = with(density) { deltaY.toDp() }
    val circleRadiusPx = with(density) { 38.dp.toPx() } 
    
    // --- ЛОГИКА ЗА СТАТУСЕ ЛЕКЦИЈА ---
    val isLocked = lesson.status == LessonStatus.LOCKED
    val isUnlocked = lesson.status == LessonStatus.UNLOCKED
    
    val visualAlpha = if (isLocked) 0.3f else 1f
    val currentBodyColor = bodyColor.copy(alpha = visualAlpha)

    // Анимација жице (ИДЕНТИЧНО КАО КОД ДУГМЕТА)
    val infiniteTransition = rememberInfiniteTransition(label = "map_wire")
    val wireAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing) // LinearEasing за константно вртење
        ),
        label = "map_wire_angle"
    )
    
    val baseGold = GoldBase.copy(alpha = 0.5f)
    val borderGradient = remember(baseGold) {
        Brush.sweepGradient(
            0f to baseGold,
            0.6f to baseGold,
            0.95f to GoldLight,
            1f to baseGold
        )
    }
    // ----------------------------------

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(itemHeightDp),
        contentAlignment = Alignment.TopCenter 
    ) {
        if (!isLast) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerX = size.width / 2

                val dotSpacing = spacing / 5 
                var nextDotTarget = dotSpacing
                var arcLen = 0f
                var currY = startGlobalY
                var prevX = getSineX(startGlobalY)
                var prevY = startGlobalY
                
                while(currY < endGlobalY) {
                    currY += 2f 
                    val newX = getSineX(currY)
                    arcLen += hypot(newX - prevX, currY - prevY)
                    
                    if (arcLen >= nextDotTarget && nextDotTarget < spacing) {
                        val localY = circleRadiusPx + (currY - startGlobalY)
                        drawCircle(
                            color = currentBodyColor.copy(alpha = 0.4f * visualAlpha),
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

        Column(
            modifier = Modifier
                .offset(x = with(density) { getSineX(startGlobalY).toDp() })
                .width(130.dp), 
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Овде је исправљена "коцкаста" жица! Контејнер је сада 84.dp
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(CircleShape) // Овај clip штити спољне ивице да не постану коцкасте
                    .then(if (!isLocked) Modifier.clickable(onClick = onClick) else Modifier)
                    .then(
                        if (isUnlocked) {
                            Modifier.drawBehind {
                                rotate(wireAngle) {
                                    // radius = size.width / 2 гарантује да жица не излази ван круга!
                                    drawCircle(brush = borderGradient, radius = size.width / 2) 
                                }
                            }
                            // Падинг који прави дебљину жице. Након падинга долази унутрашњи круг.
                            .padding(2.dp) 
                        } else Modifier
                    )
                    // Унутрашњи круг
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = visualAlpha), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (lesson.symbol == "📖" || lesson.symbol == ".") {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Osnova",
                        tint = currentBodyColor, 
                        modifier = Modifier.size(36.dp)
                    )
                } else {
                    Text(
                        text = lesson.symbol,
                        fontFamily = arabicFont,
                        fontSize = 36.sp,
                        color = currentBodyColor, 
                        modifier = Modifier.padding(bottom = 6.dp) 
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = lesson.title.asScript(),
                color = currentBodyColor, 
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    shadow = Shadow(
                        color = MaterialTheme.colorScheme.background,
                        blurRadius = 16f
                    )
                )
            )
        }
    }
}