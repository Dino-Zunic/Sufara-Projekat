package com.dino.sufara.feature.lesson.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dino.sufara.feature.lesson.presentation.viewer.components.SufaraText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onNavigateBack: () -> Unit) { // Dodana funkcija za povratak
    val settings = LocalSufaraSettings.current
    val actions = LocalSufaraSettingsActions.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // GORNJA TRAKA SA DUGMETOM NAZAD
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Назад", tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Подешавања", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.height(24.dp))

        // 1. LIVE PREVIEW
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Приказ уживо:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                SufaraText(text = "Ово је **ћирилица**.\nА ово је арапски: بِسْمِ اللَّهِ")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text("Ћирилица", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.secondary)
        Divider(modifier = Modifier.padding(vertical = 8.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Lora", "Roboto Slab").forEach { font ->
                FilterChip(
                    selected = settings.cyrillicFont == font,
                    onClick = { actions.updateCyrillicFont(font) },
                    label = { Text(font) }
                )
            }
        }
        
        Text("Величина: ${(settings.cyrillicSizeMultiplier * 100).toInt()}%")
        Slider(
            value = settings.cyrillicSizeMultiplier,
            onValueChange = { actions.updateCyrillicSize(it) },
            valueRange = 0.8f..2.0f
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text("Арапски", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.secondary)
        Divider(modifier = Modifier.padding(vertical = 8.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Amiri", "Noto Naskh").forEach { font ->
                FilterChip(
                    selected = settings.arabicFont == font,
                    onClick = { actions.updateArabicFont(font) },
                    label = { Text(font) }
                )
            }
        }

        Text("Величина: ${(settings.arabicSizeMultiplier * 100).toInt()}%")
        Slider(
            value = settings.arabicSizeMultiplier,
            onValueChange = { actions.updateArabicSize(it) },
            valueRange = 0.8f..3.0f 
        )
    }
}