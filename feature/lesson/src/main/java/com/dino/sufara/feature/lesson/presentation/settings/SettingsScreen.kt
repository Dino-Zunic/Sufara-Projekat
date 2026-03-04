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
                Icon(Icons.Default.ArrowBack, contentDescription = "Nazad".asScript(), tint = MaterialTheme.colorScheme.onBackground)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Podešavanja".asScript(), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
        }
        Spacer(modifier = Modifier.height(24.dp))

        Text("Pismo / Script".asScript(), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.secondary)
        Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.primary)
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = !settings.isCyrillic, onClick = { actions.updateScript(false) }, label = { Text("Latinica") })
            FilterChip(selected = settings.isCyrillic, onClick = { actions.updateScript(true) }, label = { Text("Ćirilica".asScript()) })
        }

        Spacer(modifier = Modifier.height(32.dp))

        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Prikaz uživo:".asScript(), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.height(8.dp))
                SufaraText(text = "Ovo je tekst.\nA ovo je arapski primer:\nبِسْمِ اللَّهِ".asScript())
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        Text("Boja običnog teksta".asScript(), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.secondary)
        Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.primary)
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BodyTextColorTheme.entries.forEach { theme ->
                FilterChip(selected = settings.bodyTextColorTheme == theme, onClick = { actions.updateBodyTextColor(theme) }, label = { Text(theme.name.lowercase().replaceFirstChar { it.uppercase() }) })
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Boja sjaja primjera (Glow)".asScript(), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.secondary)
        Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.primary)
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GlowColorTheme.entries.forEach { theme ->
                FilterChip(selected = settings.glowColorTheme == theme, onClick = { actions.updateGlowColor(theme) }, label = { Text(theme.name) })
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text("Font za tekst".asScript(), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.secondary)
        Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.primary)
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Lora", "Roboto Slab").forEach { font ->
                FilterChip(selected = settings.cyrillicFont == font, onClick = { actions.updateCyrillicFont(font) }, label = { Text(font) })
            }
        }
        
        Text("Veličina: ".asScript() + "${(settings.cyrillicSizeMultiplier * 100).toInt()}%", color = MaterialTheme.colorScheme.onBackground)
        Slider(value = settings.cyrillicSizeMultiplier, onValueChange = { actions.updateCyrillicSize(it) }, valueRange = 0.6f..1.5f)

        Spacer(modifier = Modifier.height(32.dp))

        Text("Arapski".asScript(), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.secondary)
        Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.primary)
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Amiri", "Noto Naskh", "KFGQPC").forEach { font ->
                FilterChip(selected = settings.arabicFont == font, onClick = { actions.updateArabicFont(font) }, label = { Text(font) })
            }
        }

        Text("Veličina: ".asScript() + "${(settings.arabicSizeMultiplier * 100).toInt()}%", color = MaterialTheme.colorScheme.onBackground)
        Slider(value = settings.arabicSizeMultiplier, onValueChange = { actions.updateArabicSize(it) }, valueRange = 0.6f..2.0f)

        Spacer(modifier = Modifier.height(32.dp))

        Text("Opcije prikaza".asScript(), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.secondary)
        Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.primary)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Prikaži transkripciju (IPA)".asScript(), color = MaterialTheme.colorScheme.onBackground)
            Switch(checked = settings.showIpa, onCheckedChange = { actions.toggleIpa(it) })
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Prikaži razdvojena slova".asScript(), color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.weight(1f))
            Switch(checked = settings.showSeparatedLetters, onCheckedChange = { actions.toggleSeparatedLetters(it) })
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text("Анимација картица".asScript(), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.secondary)
        Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.primary)
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            com.dino.sufara.feature.lesson.presentation.viewer.animations.CardAnimationType.entries.forEach { anim ->
                FilterChip(
                    selected = settings.cardAnimation == anim,
                    onClick = { actions.updateCardAnimation(anim) },
                    label = { Text(anim.name) }
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))

        Text("Развој и тестирање".asScript(), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.secondary)
        Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.primary)
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Експерт Мод (Откључај све)".asScript(), color = MaterialTheme.colorScheme.onBackground)
            Button(
                onClick = { actions.unlockExpertMode() },
                enabled = !settings.isExpertModeUnlocked,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(if (settings.isExpertModeUnlocked) "ОТКЉУЧАНО".asScript() else "ОТКЉУЧАЈ".asScript())
            }
        }
    }
}