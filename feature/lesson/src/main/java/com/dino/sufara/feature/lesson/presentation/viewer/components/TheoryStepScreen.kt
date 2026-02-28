package com.dino.sufara.feature.lesson.presentation.viewer.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dino.sufara.feature.lesson.domain.model.LessonStep

@Composable
fun TheoryStepScreen(
    step: LessonStep.Theory,
    lessonId: String, 
    symbol: String,
    onNextClick: () -> Unit
) {
    var isDodatakExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
        // НОВО: Наслов сада користи secondary (Златну) боју, уместо плаве
        Text(
            text = step.title,
            fontSize = 28.sp,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        SufaraText(text = step.text, lessonId = lessonId, symbol = symbol)

        if (step.dodatakText != null) {
            Spacer(modifier = Modifier.height(32.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().clickable { isDodatakExpanded = !isDodatakExpanded }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // НОВО: Икона и текст додатка су такође златни
                        Icon(Icons.Default.Info, contentDescription = "Инфо", tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Занимљивости (Кликни да отвориш)", color = MaterialTheme.colorScheme.secondary)
                    }
                    AnimatedVisibility(visible = isDodatakExpanded) {
                        Column {
                            Spacer(modifier = Modifier.height(16.dp))
                            // ИСПРАВКА ЗА БИЛД: Обрисан хардкодован 'color = Color.LightGray'
                            SufaraText(text = step.dodatakText, lessonId = lessonId, symbol = symbol)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = onNextClick,
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("Даље", fontSize = 18.sp)
        }
    }
}