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
import com.dino.sufara.feature.lesson.domain.util.asScript
import com.dino.sufara.feature.lesson.presentation.viewer.components.SufaraText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onNavigateBack: () -> Unit) { 
    val settings = LocalSufaraSettings.current
    val actions = LocalSufaraSettingsActions.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Назад".asScript(), tint = MaterialTheme.colorScheme.onBackground)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Подешавања".asScript(), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
        }
        Spacer(modifier = Modifier.height(24.dp))

        // НОВО: Бирач писма
        Text("Писмо / Script".asScript(), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.secondary)
        Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.primary)
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = !settings.isCyrillic,
                onClick = { actions.updateScript(false) },
                label = { Text("Latinica") }
            )
            FilterChip(
                selected = settings.isCyrillic,
                onClick = { actions.updateScript(true) },
                label = { Text("Ћирилица") }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Приказ уживо:".asScript(), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.height(8.dp))
                SufaraText(text = "Ово је ћирилица.\nА ово је арапски пример:\nبِسْمِ اللَّهِ") // SufaraText сам преводи!
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        Text("Боја обичног текста".asScript(), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.secondary)
        Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.primary)
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BodyTextColorTheme.values().forEach { theme ->
                FilterChip(
                    selected = settings.bodyTextColorTheme == theme,
                    onClick = { actions.updateBodyTextColor(theme) },
                    label = { Text(theme.name.lowercase().replaceFirstChar { it.uppercase() }) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Боја сјаја примера (Glow)".asScript(), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.secondary)
        Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.primary)
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GlowColorTheme.values().forEach { theme ->
                FilterChip(
                    selected = settings.glowColorTheme == theme,
                    onClick = { actions.updateGlowColor(theme) },
                    label = { Text(theme.name) }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text("Фонт за текст".asScript(), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.secondary)
        Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.primary)
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Lora", "Roboto Slab").forEach { font ->
                FilterChip(
                    selected = settings.cyrillicFont == font,
                    onClick = { actions.updateCyrillicFont(font) },
                    label = { Text(font) }
                )
            }
        }
        
        Text("Величина: ".asScript() + "${(settings.cyrillicSizeMultiplier * 100).toInt()}%", color = MaterialTheme.colorScheme.onBackground)
        Slider(
            value = settings.cyrillicSizeMultiplier,
            onValueChange = { actions.updateCyrillicSize(it) },
            valueRange = 0.6f..1.5f
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text("Арапски".asScript(), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.secondary)
        Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.primary)
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Amiri", "Noto Naskh").forEach { font ->
                FilterChip(
                    selected = settings.arabicFont == font,
                    onClick = { actions.updateArabicFont(font) },
                    label = { Text(font) }
                )
            }
        }

        Text("Величина: ".asScript() + "${(settings.arabicSizeMultiplier * 100).toInt()}%", color = MaterialTheme.colorScheme.onBackground)
        Slider(
            value = settings.arabicSizeMultiplier,
            onValueChange = { actions.updateArabicSize(it) },
            valueRange = 0.6f..2.0f 
        )

        Spacer(modifier = Modifier.height(32.dp))
        Text("Развој".asScript(), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.secondary)
        Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.primary)
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Debug Mod (Омогућава прескакање квиза)".asScript(), color = MaterialTheme.colorScheme.onBackground)
            Switch(checked = settings.isDebugMode, onCheckedChange = { actions.toggleDebugMode(it) })
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}