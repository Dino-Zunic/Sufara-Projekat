package com.dino.sufara.feature.lesson.presentation.viewer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.ImageLoader
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.decode.SvgDecoder
import com.dino.sufara.core.designsystem.GoldBase
import com.dino.sufara.core.designsystem.TextSilver
import com.dino.sufara.feature.lesson.domain.model.MakhrajInfo
import com.dino.sufara.feature.lesson.domain.util.asScript
import com.dino.sufara.feature.lesson.presentation.settings.LocalSufaraSettings
import com.dino.sufara.feature.lesson.presentation.settings.MakhrajDiagramStyle
import com.dino.sufara.feature.lesson.presentation.settings.SufaraFonts

@Composable
fun MakhrajSection(items: List<MakhrajInfo>) {
    if (items.isEmpty()) return
    val settings = LocalSufaraSettings.current
    if (settings.makhrajDiagramStyle == MakhrajDiagramStyle.HIDDEN) return
    val cyrillicFont = SufaraFonts.getCyrillicFont(settings.cyrillicFont)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items.forEach { item ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Text(
                        text = "Исходиште ${item.number} · ${item.id.displayName()}".asScript(),
                        color = GoldBase,
                        fontFamily = cyrillicFont,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = item.description.asScript(),
                        color = TextSilver,
                        fontFamily = cyrillicFont,
                        textAlign = TextAlign.Start,
                        fontSize = 13.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun ExpandableLessonImage(
    imagePath: String,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember(imagePath) { mutableStateOf(false) }
    val context = LocalContext.current
    val imageLoader = remember(context.applicationContext) {
        ImageLoader.Builder(context.applicationContext)
            .components { add(SvgDecoder.Factory()) }
            .build()
    }
    DisposableEffect(imageLoader) { onDispose { imageLoader.shutdown() } }
    SubcomposeAsyncImage(
        model = imagePath,
        imageLoader = imageLoader,
        contentDescription = contentDescription,
        modifier = modifier.clickable { expanded = true },
        contentScale = ContentScale.Fit,
        loading = { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = GoldBase) } },
        error = {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Слика није могла да се учита.".asScript(), color = TextSilver, textAlign = TextAlign.Center)
            }
        },
        success = { SubcomposeAsyncImageContent() }
    )

    if (expanded) {
        Dialog(
            onDismissRequest = { expanded = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xF2020A18), RoundedCornerShape(24.dp))
                    .clickable { expanded = false },
                contentAlignment = Alignment.Center
            ) {
                SubcomposeAsyncImage(
                    model = imagePath,
                    imageLoader = imageLoader,
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize().padding(20.dp).clickable { },
                    contentScale = ContentScale.Fit
                )
                IconButton(
                    onClick = { expanded = false },
                    modifier = Modifier.align(Alignment.TopEnd).padding(6.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Затвори".asScript(), tint = Color.White)
                }
            }
        }
    }
}

private fun com.dino.sufara.feature.lesson.domain.model.MakhrajId.displayName(): String = when (this) {
    com.dino.sufara.feature.lesson.domain.model.MakhrajId.ORAL_CAVITY_MADD -> "усна и грлена шупљина"
    com.dino.sufara.feature.lesson.domain.model.MakhrajId.THROAT_DEEPEST -> "дно грла"
    com.dino.sufara.feature.lesson.domain.model.MakhrajId.THROAT_MIDDLE -> "средина грла"
    com.dino.sufara.feature.lesson.domain.model.MakhrajId.THROAT_UPPER -> "врх грла"
    com.dino.sufara.feature.lesson.domain.model.MakhrajId.TONGUE_REARMOST_QAF -> "задњи део језика · каф"
    com.dino.sufara.feature.lesson.domain.model.MakhrajId.TONGUE_REAR_KAF -> "задњи део језика · кеф"
    com.dino.sufara.feature.lesson.domain.model.MakhrajId.TONGUE_MIDDLE -> "средина језика"
    com.dino.sufara.feature.lesson.domain.model.MakhrajId.TONGUE_SIDE_DAD -> "бочна страна језика"
    com.dino.sufara.feature.lesson.domain.model.MakhrajId.TONGUE_SIDE_TO_TIP_LAM -> "ивица језика до врха"
    com.dino.sufara.feature.lesson.domain.model.MakhrajId.TONGUE_TIP_NUN -> "врх језика · нун"
    com.dino.sufara.feature.lesson.domain.model.MakhrajId.TONGUE_TIP_RA -> "врх језика · ра"
    com.dino.sufara.feature.lesson.domain.model.MakhrajId.TONGUE_TIP_UPPER_INCISOR_ROOTS -> "коренови горњих секутића"
    com.dino.sufara.feature.lesson.domain.model.MakhrajId.TONGUE_TIP_LOWER_INCISORS -> "доњи секутићи"
    com.dino.sufara.feature.lesson.domain.model.MakhrajId.TONGUE_TIP_UPPER_INCISOR_EDGES -> "ивице горњих секутића"
    com.dino.sufara.feature.lesson.domain.model.MakhrajId.LOWER_LIP_UPPER_INCISORS -> "доња усна и горњи секутићи"
    com.dino.sufara.feature.lesson.domain.model.MakhrajId.BETWEEN_LIPS -> "између усана"
    com.dino.sufara.feature.lesson.domain.model.MakhrajId.NASAL_CAVITY_GHUNNAH -> "носна шупљина"
}
