package com.dino.sufara.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MainMenuScreen(
    onStartClick: () -> Unit,
    onSettingsClick: () -> Unit // Nova funkcija za navigaciju ka podešavanjima
) {
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        
        // Dugme za Podešavanja u gornjem desnom uglu
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
        ) {
            Icon(Icons.Default.Settings, contentDescription = "Подешавања", tint = MaterialTheme.colorScheme.primary)
        }

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Суфара", style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onStartClick) {
                Text("Покрени лекције")
            }
        }
    }
}