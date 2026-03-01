package com.dino.sufara

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.dino.sufara.core.designsystem.SufaraTheme
import com.dino.sufara.feature.lesson.data.repository.LocalAssetLessonRepository
import com.dino.sufara.feature.lesson.presentation.settings.SufaraSettingsProvider
import com.dino.sufara.navigation.SufaraNavGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Иницијализација Splash екрана мора ићи пре super.onCreate
        installSplashScreen() 
        
        super.onCreate(savedInstanceState)
        
        val repository = LocalAssetLessonRepository(this)

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