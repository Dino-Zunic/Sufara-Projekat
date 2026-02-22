package com.dino.sufara

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.dino.sufara.core.designsystem.SufaraTheme
import com.dino.sufara.feature.lesson.data.repository.LocalAssetLessonRepository
import com.dino.sufara.feature.lesson.presentation.grid.LessonGridScreen
import com.dino.sufara.feature.lesson.presentation.grid.LessonGridViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val repository = LocalAssetLessonRepository(this)
        val viewModel = LessonGridViewModel(repository)

        setContent {
            SufaraTheme {
                LessonGridScreen(viewModel)
            }
        }
    }
}
