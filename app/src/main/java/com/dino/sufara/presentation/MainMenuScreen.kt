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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dino.sufara.core.designsystem.*
import com.dino.sufara.feature.lesson.domain.repository.LessonRepository
import com.dino.sufara.feature.lesson.domain.util.asScript
import com.dino.sufara.feature.lesson.presentation.settings.LocalSufaraSettings
import com.dino.sufara.feature.lesson.presentation.settings.SufaraFonts
import com.dino.sufara.core.designsystem.components.GoldenWireButton

@Composable
fun MainMenuScreen(
    repository: LessonRepository,
    onStartClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val settings = LocalSufaraSettings.current
    val cyrillicFont = SufaraFonts.getCyrillicFont(settings.cyrillicFont)
    
    var facts by remember { mutableStateOf(listOf("Учитавање...")) }
    var currentFact by remember { mutableStateOf("Учитавање...") }
    
    LaunchedEffect(Unit) {
        val loadedFacts = repository.getFunFacts()
        if (loadedFacts.isNotEmpty()) {
            facts = loadedFacts
            currentFact = facts.random()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
        ) {
            Icon(Icons.Default.Settings, contentDescription = "Подешавања".asScript(), tint = GoldBase)
        }

        Column(modifier = Modifier.fillMaxSize()) {
            
            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val metallicGoldBrush = remember { Brush.verticalGradient(listOf(GoldLight, GoldBase, GoldDark)) }
                val titleShadow = remember { Shadow(color = GoldBase.copy(alpha = 0.5f), blurRadius = 24f) }

                Text(
                    text = "Суфара".asScript(),
                    style = TextStyle(
                        brush = metallicGoldBrush,
                        shadow = titleShadow,
                        fontSize = 64.sp, 
                        fontFamily = cyrillicFont,
                        fontWeight = FontWeight.Bold
                    )
                )
                
                Spacer(modifier = Modifier.height(48.dp))

                GoldenWireButton(onClick = onStartClick, text = "Покрени лекције".asScript(), font = cyrillicFont)
            }

            Spacer(modifier = Modifier.height(64.dp))

            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Divider(color = TextSilver.copy(alpha = 0.4f), thickness = 1.dp)
                
                AnimatedContent(
                    targetState = currentFact,
                    transitionSpec = { fadeIn(tween(800)) togetherWith fadeOut(tween(800)) },
                    label = "FactAnimation"
                ) { fact ->
                    Text(
                        text = fact.asScript(),
                        color = TextSilver.copy(alpha = 0.9f),
                        fontSize = 16.sp,
                        fontFamily = cyrillicFont,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { 
                                var newFact = facts.random()
                                while(newFact == currentFact && facts.size > 1) {
                                    newFact = facts.random()
                                }
                                currentFact = newFact
                            }
                            .padding(vertical = 24.dp)
                    )
                }
                
                Divider(color = TextSilver.copy(alpha = 0.4f), thickness = 1.dp)
            }

            Spacer(modifier = Modifier.weight(2f)) 
        }
    }
}
