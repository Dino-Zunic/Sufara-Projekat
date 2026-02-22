package com.dino.sufara.feature.lesson.presentation.grid

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dino.sufara.feature.lesson.domain.model.Lesson

@Composable
fun LessonGridScreen(viewModel: LessonGridViewModel) {
    val lessons by viewModel.lessons.collectAsState()

    LazyVerticalGrid(
        columns = GridCells.Fixed(2), // Dve kolone
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        items(lessons) { lesson ->
            LessonCard(lesson)
        }
    }
}

@Composable
fun LessonCard(lesson: Lesson) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface // MysticTeal
        ),
        modifier = Modifier.fillMaxWidth().aspectRatio(1f) // Kvadratne kartice
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = lesson.id,
                color = MaterialTheme.colorScheme.primary, // OrangeTerra
                fontWeight = FontWeight.Bold
            )
            
            // Prikazujemo prvi primer iz .md fajla kao pregled
            Text(
                text = lesson.examples.firstOrNull() ?: "",
                fontSize = 32.sp,
                color = Color.White
            )

            Text(
                text = lesson.title,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
        }
    }
}