package com.dino.sufara.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dino.sufara.feature.lesson.domain.repository.LessonRepository
import com.dino.sufara.feature.lesson.presentation.grid.LessonGridScreen
import com.dino.sufara.feature.lesson.presentation.grid.LessonGridViewModel
import com.dino.sufara.feature.lesson.presentation.viewer.LessonViewerScreen
import com.dino.sufara.feature.lesson.presentation.viewer.LessonViewerViewModel
import com.dino.sufara.presentation.MainMenuScreen

@Composable
fun SufaraNavGraph(repository: LessonRepository) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "main_menu") {
        
        composable("main_menu") {
            MainMenuScreen(
                onNavigateToSufara = { navController.navigate("sufara_grid") }
            )
        }

        composable("sufara_grid") {
            // Predajemo onClick akciju Gridu kako bi znao šta da radi kad se klikne kartica
            val viewModel = LessonGridViewModel(repository)
            LessonGridScreen(
                viewModel = viewModel,
                onLessonClick = { lessonId ->
                    navController.navigate("lesson_viewer/$lessonId")
                }
            )
        }

        composable(
            route = "lesson_viewer/{lessonId}",
            arguments = listOf(navArgument("lessonId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getString("lessonId") ?: return@composable
            val viewModel = LessonViewerViewModel(repository, lessonId)
            
            LessonViewerScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}