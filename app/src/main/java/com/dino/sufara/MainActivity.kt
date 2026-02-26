package com.dino.sufara

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.dino.sufara.core.designsystem.SufaraTheme
import com.dino.sufara.feature.lesson.data.repository.LocalAssetLessonRepository
import com.dino.sufara.feature.lesson.presentation.settings.SufaraSettingsProvider
import com.dino.sufara.navigation.SufaraNavGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val repository = LocalAssetLessonRepository(this)

        setContent {
            SufaraTheme {
                // SufaraSettingsProvider omogućava globalni pristup fontovima i veličinama
                SufaraSettingsProvider {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        // NavGraph sada preuzima kontrolu nad ekranima
                        SufaraNavGraph(repository)
                    }
                }
            }
        }
    }
}