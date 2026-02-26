package com.dino.sufara

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.dino.sufara.core.designsystem.SufaraTheme
import com.dino.sufara.feature.lesson.data.repository.LocalAssetLessonRepository
import com.dino.sufara.navigation.SufaraNavGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val repository = LocalAssetLessonRepository(this)

        setContent {
            SufaraTheme {
                // NavGraph sada preuzima kontrolu nad ekranima
                SufaraNavGraph(repository)
            }
        }
    }
}
