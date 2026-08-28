package com.dino.sufara

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.room.Room
import com.dino.sufara.core.designsystem.SufaraGeometricBackground
import com.dino.sufara.core.designsystem.SufaraTheme
import com.dino.sufara.core.designsystem.components.LocalWireMotionStyle
import com.dino.sufara.feature.lesson.data.local.AppDatabase
import com.dino.sufara.feature.lesson.data.repository.LocalAssetLessonRepository
import com.dino.sufara.feature.lesson.presentation.settings.SufaraSettingsProvider
import com.dino.sufara.feature.lesson.presentation.settings.LocalSufaraSettings
import com.dino.sufara.navigation.SufaraNavGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen() 
        super.onCreate(savedInstanceState)
        
        val database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "sufara_database"
        ).addMigrations(AppDatabase.MIGRATION_1_2).build()

        val repository = LocalAssetLessonRepository(this, database.dao)

        setContent {
            SufaraTheme {
                SufaraSettingsProvider {
                    val settings = LocalSufaraSettings.current
                    CompositionLocalProvider(LocalWireMotionStyle provides settings.wireMotionStyle) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                SufaraGeometricBackground(
                                    modifier = Modifier.fillMaxSize(),
                                    patternStyle = settings.backgroundPatternStyle,
                                    lightMode = settings.backgroundLightMode,
                                    lightStrength = settings.backgroundLightStrength,
                                    patternVisibility = settings.backgroundPatternVisibility,
                                    orbitEnabled = settings.backgroundOrbitEnabled,
                                    blobEnabled = settings.backgroundBlobEnabled,
                                    particlesEnabled = settings.backgroundParticlesEnabled,
                                    particleVisibility = settings.backgroundParticleVisibility
                                )
                                SufaraNavGraph(repository)
                            }
                        }
                    }
                }
            }
        }
    }
}
