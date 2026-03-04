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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dino.sufara.feature.lesson.domain.model.LessonStep
import com.dino.sufara.feature.lesson.domain.util.asScript
import com.dino.sufara.feature.lesson.presentation.settings.LocalSufaraSettings
import com.dino.sufara.feature.lesson.presentation.settings.SufaraFonts

@Composable
fun TheoryStepScreen(step: LessonStep.Theory, lessonId: String, lessonTitle: String, symbol: String) {
    val settings = LocalSufaraSettings.current
    val cyrillicFont = SufaraFonts.getCyrillicFont(settings.cyrillicFont)
    var isDodatakExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).verticalScroll(rememberScrollState())) {
        Text(text = lessonTitle.asScript(), fontSize = 32.sp, fontWeight = FontWeight.Bold, fontFamily = cyrillicFont, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(bottom = 24.dp))
        
        val isHarf = (symbol.length == 1 && symbol != ".") || symbol == "لا"
        if (isHarf) HarfShowcase(symbol = symbol)
        
        SufaraText(text = step.text, lessonId = lessonId, symbol = symbol)

        if (step.dodatakText != null) {
            Spacer(modifier = Modifier.height(32.dp))
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth().clickable { isDodatakExpanded = !isDodatakExpanded }) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = "Инфо", tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Занимљивости".asScript(), color = MaterialTheme.colorScheme.secondary)
                    }
                    AnimatedVisibility(visible = isDodatakExpanded) {
                        Column { Spacer(modifier = Modifier.height(16.dp)); SufaraText(text = step.dodatakText, lessonId = lessonId, symbol = symbol) }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp)) 
    }
}