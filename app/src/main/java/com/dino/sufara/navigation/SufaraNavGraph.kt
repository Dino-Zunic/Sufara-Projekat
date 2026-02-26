package com.dino.sufara.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dino.sufara.feature.lesson.domain.repository.LessonRepository
import com.dino.sufara.feature.lesson.presentation.grid.LessonGridScreen
import com.dino.sufara.feature.lesson.presentation.grid.LessonGridViewModel
import com.dino.sufara.feature.lesson.presentation.viewer.LessonViewerScreen
import com.dino.sufara.feature.lesson.presentation.viewer.LessonViewerViewModel
import com.dino.sufara.feature.lesson.presentation.settings.SettingsScreen
import com.dino.sufara.presentation.MainMenuScreen

@Composable
fun SufaraNavGraph(repository: LessonRepository) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "main_menu") {
        composable("main_menu") {
            MainMenuScreen(
                onStartClick = { navController.navigate("lesson_grid") },
                onSettingsClick = { navController.navigate("settings") } // Vodi na settings ekran
            )
        }
        
        // RUTA ZA PODEŠAVANJA
        composable("settings") {
            SettingsScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable("lesson_grid") {
            val viewModel = remember { LessonGridViewModel(repository) }
            LessonGridScreen(
                viewModel = viewModel,
                onLessonClick = { lessonId ->
                    navController.navigate("lesson_viewer/$lessonId")
                }
            )
        }

        composable("lesson_viewer/{lessonId}") { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getString("lessonId") ?: return@composable
            val viewModel = remember { LessonViewerViewModel(repository, lessonId) }
            LessonViewerScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}