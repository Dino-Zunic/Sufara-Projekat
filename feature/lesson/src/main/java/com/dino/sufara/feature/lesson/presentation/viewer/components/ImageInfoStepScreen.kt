package com.dino.sufara.feature.lesson.presentation.viewer.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.dino.sufara.feature.lesson.domain.model.LessonStep

@Composable
fun ImageInfoStepScreen(step: LessonStep.ImageInfo) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        AsyncImage(
            model = step.imagePath,
            contentDescription = "Ilustracija lekcije",
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            contentScale = ContentScale.Fit
        )
    }
}