package com.dino.sufara.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dino.sufara.core.designsystem.components.SufaraButton

@Composable
fun MainMenuScreen(
    onNavigateToSufara: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Kursevi",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 32.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        SufaraButton(
            text = "Sufara",
            onClick = onNavigateToSufara,
            modifier = Modifier.fillMaxWidth()
        )
    }
}