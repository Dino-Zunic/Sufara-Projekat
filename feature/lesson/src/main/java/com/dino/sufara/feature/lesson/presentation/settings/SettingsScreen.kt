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
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onUnlockExpertMode: () -> Unit
) { 
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

        // ПИСМО
        Text("Писмо".asScript(), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.secondary)
        Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.primary)
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Латиница се не преводи преко .asScript() јер нема смисла, али Ћирилица да.
            FilterChip(selected = !settings.isCyrillic, onClick = { actions.updateScript(false) }, label = { Text("Латиница".asScript()) })
            FilterChip(selected = settings.isCyrillic, onClick = { actions.updateScript(true) }, label = { Text("Ћирилица".asScript()) })
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ФОНТ ЗА ТЕКСТ
        Text("Фонт за текст".asScript(), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.secondary)
        Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.primary)
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Lora", "Roboto Slab").forEach { font ->
                FilterChip(selected = settings.cyrillicFont == font, onClick = { actions.updateCyrillicFont(font) }, label = { Text(font) })
            }
        }
        
        Text("Величина: ".asScript() + "${(settings.cyrillicSizeMultiplier * 100).toInt()}%", color = MaterialTheme.colorScheme.onBackground)
        Slider(value = settings.cyrillicSizeMultiplier, onValueChange = { actions.updateCyrillicSize(it) }, valueRange = 0.6f..1.5f)

        Spacer(modifier = Modifier.height(32.dp))

        // АРАПСКИ ФОНТ
        Text("Арапски".asScript(), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.secondary)
        Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.primary)
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("KFGQPC", "Amiri", "Noto Naskh").forEach { font ->
                FilterChip(selected = settings.arabicFont == font, onClick = { actions.updateArabicFont(font) }, label = { Text(font) })
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // LIVE ПРЕВЈУ
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Приказ уживо:".asScript(), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.height(8.dp))
                SufaraText(text = "Ово је текст.\nА ово је арапски пример:\nكِتَابٌ مُفِيدٌ".asScript())
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ОПЦИЈЕ ПРИКАЗА
        Text("Опције приказа".asScript(), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.secondary)
        Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.primary)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Приказ транскрипције (IPA)".asScript(), color = MaterialTheme.colorScheme.onBackground)
            Switch(checked = settings.showIpa, onCheckedChange = { actions.toggleIpa(it) })
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Приказ раздвојених слова".asScript(), color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.weight(1f))
            Switch(checked = settings.showSeparatedLetters, onCheckedChange = { actions.toggleSeparatedLetters(it) })
        }

        Spacer(modifier = Modifier.height(32.dp))

        // РАЗВОЈ (ЕКСПЕРТ МОД)
        Text("Развој и тестирање".asScript(), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.secondary)
        Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.primary)
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Експерт Мод (Заврши све)".asScript(), color = MaterialTheme.colorScheme.onBackground)
            Button(
                onClick = { onUnlockExpertMode() },
                enabled = !settings.isExpertModeUnlocked,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(if (settings.isExpertModeUnlocked) "ОТКЉУЧАНО".asScript() else "ОТКЉУЧАЈ".asScript())
            }
        }
        Spacer(modifier = Modifier.height(48.dp))
    }
}