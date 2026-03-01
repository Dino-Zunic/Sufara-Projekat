package com.dino.sufara.feature.lesson.presentation.viewer.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.dino.sufara.feature.lesson.domain.model.LessonStep
import com.dino.sufara.feature.lesson.domain.util.asScript

@Composable
fun ImageInfoStepScreen(
    step: LessonStep.ImageInfo,
    onNextClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AsyncImage(
            model = step.imagePath,
            contentDescription = "Илустрација лекције",
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentScale = ContentScale.Fit
        )

        Button(
            onClick = onNextClick,
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
        ) {
            Text("Даље".asScript(), fontSize = 18.sp)
        }
    }
}