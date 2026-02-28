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
    lessonId: String, // НОВО: Примамо ID
    symbol: String,
    onNextClick: () -> Unit
) {
    var isDodatakExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
        Text(
            text = step.title,
            fontSize = 28.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // ПРОСЛЕЂУЈЕМО ИД И СИМБОЛ
        SufaraText(text = step.text, lessonId = lessonId, symbol = symbol)

        if (step.dodatakText != null) {
            Spacer(modifier = Modifier.height(32.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().clickable { isDodatakExpanded = !isDodatakExpanded }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = "Инфо", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Занимљивости (Кликни да отвориш)", color = MaterialTheme.colorScheme.primary)
                    }
                    AnimatedVisibility(visible = isDodatakExpanded) {
                        Column {
                            Spacer(modifier = Modifier.height(16.dp))
                            // ПРОСЛЕЂУЈЕМО И ОВДЕ
                            SufaraText(text = step.dodatakText, color = Color.LightGray, lessonId = lessonId, symbol = symbol)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = onNextClick,
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Даље", fontSize = 18.sp)
        }
    }
}