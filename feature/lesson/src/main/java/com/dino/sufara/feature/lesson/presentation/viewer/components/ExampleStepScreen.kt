package com.dino.sufara.feature.lesson.presentation.viewer.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dino.sufara.core.designsystem.IpaFontFamily
import com.dino.sufara.core.designsystem.TextSilver
import com.dino.sufara.feature.lesson.domain.model.LessonStep
import com.dino.sufara.feature.lesson.domain.util.ArabicIpaTranscriber
import com.dino.sufara.feature.lesson.domain.util.ArabicWordSplitter
import com.dino.sufara.feature.lesson.domain.util.asScript
import com.dino.sufara.feature.lesson.presentation.settings.LocalSufaraSettings
import com.dino.sufara.feature.lesson.presentation.settings.SufaraFonts

@Composable
fun ExampleStepScreen(
    step: LessonStep.Example,
    lessonId: String, 
    symbol: String,
    onNextClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val settings = LocalSufaraSettings.current
    val arabicFont = SufaraFonts.getArabicFont(settings.arabicFont)

    val ipaTranscription = remember(step.text) {
        ArabicIpaTranscriber.transcribe(step.text)
    }
    
    // НОВО: Стање за проширење картице
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize() // Ово чини да се картица глатко прошири
                .clip(RoundedCornerShape(24.dp))
                .clickable { isExpanded = !isExpanded }, // Отвара/затвара олакшицу
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally, 
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 350.dp) // Обезбеђујемо минималну висину уместо фиксног aspectRatio
                    .padding(vertical = 32.dp, horizontal = 16.dp)
            ) {
                SufaraText(
                    text = step.text,
                    modifier = Modifier.wrapContentSize(unbounded = true), 
                    baseFontSize = 32.sp,
                    arabicFontSize = 100.sp,
                    lineHeight = 180.sp, 
                    lessonId = lessonId, 
                    symbol = symbol
                )

                if (settings.showIpa && ipaTranscription.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "[ $ipaTranscription ]",
                        color = TextSilver.copy(alpha = 0.6f),
                        fontFamily = IpaFontFamily,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Light,
                        letterSpacing = 2.sp
                    )
                }

                AnimatedVisibility(visible = isExpanded) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(modifier = Modifier.height(16.dp)) // Смањено са 24
                        Divider(color = TextSilver.copy(alpha = 0.2f), modifier = Modifier.fillMaxWidth(0.5f))
                        Spacer(modifier = Modifier.height(12.dp)) // Смањено са 24
                        
                        Text(
                            text = ArabicWordSplitter.splitWord(step.text),
                            fontFamily = arabicFont,
                            fontSize = 36.sp, // Мало мањи фонт да не гута простор
                            color = TextSilver.copy(alpha = 0.8f) 
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .clickable { haptic.performHapticFeedback(HapticFeedbackType.LongPress) },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = "Звук", tint = Color.White, modifier = Modifier.size(32.dp))
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(onClick = onNextClick, modifier = Modifier.fillMaxWidth()) {
            Text("Даље".asScript())
        }
    }
}