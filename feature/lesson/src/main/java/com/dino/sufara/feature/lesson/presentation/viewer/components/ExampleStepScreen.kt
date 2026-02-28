package com.dino.sufara.feature.lesson.presentation.viewer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dino.sufara.feature.lesson.domain.model.LessonStep

@Composable
fun ExampleStepScreen(
    step: LessonStep.Example,
    lessonId: String, // НОВО: Примамо ID
    symbol: String,
    onNextClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Card(
            modifier = Modifier.fillMaxWidth().aspectRatio(1f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                SufaraText(
                    text = step.text,
                    modifier = Modifier.wrapContentSize(unbounded = true), 
                    baseFontSize = 32.sp,
                    arabicFontSize = 100.sp,
                    lineHeight = 180.sp, 
                    lessonId = lessonId, // ПРОСЛЕЂУЈЕМО ID
                    symbol = symbol
                )
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
            Text("Даље")
        }
    }
}