package com.dino.sufara

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.room.Room
import com.dino.sufara.core.designsystem.SufaraTheme
// ИСПРАВКА: Нова путања до базе
import com.dino.sufara.feature.lesson.data.local.AppDatabase
import com.dino.sufara.feature.lesson.data.repository.LocalAssetLessonRepository
import com.dino.sufara.feature.lesson.presentation.settings.SufaraSettingsProvider
import com.dino.sufara.navigation.SufaraNavGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen() 
        super.onCreate(savedInstanceState)
        
        val database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "sufara_database"
        ).fallbackToDestructiveMigration().build()

        val repository = LocalAssetLessonRepository(this, database.dao)

        setContent {
            SufaraTheme {
                SufaraSettingsProvider {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        SufaraNavGraph(repository)
                    }
                }
            }
        }
    }
}