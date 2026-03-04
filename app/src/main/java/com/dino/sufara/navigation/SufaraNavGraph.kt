package com.dino.sufara.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.dino.sufara.feature.lesson.presentation.anki.AnkiQuizScreen
import com.dino.sufara.feature.lesson.presentation.anki.AnkiQuizViewModel
import kotlinx.coroutines.launch

@Composable
fun SufaraNavGraph(repository: LessonRepository) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "main_menu") {
        composable("main_menu") {
            MainMenuScreen(
                repository = repository,
                onStartClick = { navController.navigate("lesson_grid") },
                onSettingsClick = { navController.navigate("settings") },
                onAnkiClick = { navController.navigate("anki_quiz") }
            )
        }
        
        composable("settings") {
            val scope = rememberCoroutineScope()
            val actions = com.dino.sufara.feature.lesson.presentation.settings.LocalSufaraSettingsActions.current
            
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onUnlockExpertMode = {
                    scope.launch { 
                        repository.unlockAllLessons() 
                    }
                    actions.unlockExpertMode()
                }
            )
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

        composable("anki_quiz") {
            val viewModel = remember { AnkiQuizViewModel(repository) }
            AnkiQuizScreen(
                viewModel = viewModel, 
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}