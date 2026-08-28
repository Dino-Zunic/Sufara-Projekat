package com.dino.sufara.feature.lesson.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dino.sufara.core.designsystem.BackgroundLightMode
import com.dino.sufara.core.designsystem.BackgroundLightStrength
import com.dino.sufara.core.designsystem.BackgroundPatternStyle
import com.dino.sufara.core.designsystem.components.WireMotionStyle
import com.dino.sufara.feature.lesson.domain.util.asScript
import com.dino.sufara.feature.lesson.presentation.viewer.components.SufaraText

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onUnlockExpertMode: () -> Unit,
    onResetProgress: () -> Unit
) {
    val settings = LocalSufaraSettings.current
    val actions = LocalSufaraSettingsActions.current
    var confirmReset by remember { mutableStateOf(false) }
    var showGuiExperiments by remember { mutableStateOf(false) }
    var showDeveloperTools by remember { mutableStateOf(false) }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("Брисање напретка".asScript()) },
            text = { Text("Ово брише напредак читања, писања и понављања. Садржај и подешавања остају сачувани.".asScript()) },
            confirmButton = {
                TextButton(onClick = { confirmReset = false; onResetProgress() }) {
                    Text("Обриши".asScript(), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmReset = false }) { Text("Откажи".asScript()) }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Назад".asScript())
            }
            Spacer(Modifier.width(8.dp))
            Text("Подешавања".asScript(), style = MaterialTheme.typography.headlineMedium)
        }
        Spacer(Modifier.height(24.dp))

        SettingsSection("Писмо") {
            ChoiceRow(
                choices = listOf(false to "Латиница".asScript(), true to "Ћирилица".asScript()),
                selected = settings.isCyrillic,
                onSelect = actions::updateScript
            )
        }

        SettingsSection("Фонт за текст") {
            ChoiceRow(
                choices = listOf("Lora" to "Lora", "Roboto Slab" to "Roboto Slab"),
                selected = settings.cyrillicFont,
                onSelect = actions::updateCyrillicFont
            )
            Text("Величина: ${(settings.cyrillicSizeMultiplier * 100).toInt()}%".asScript())
            Slider(
                value = settings.cyrillicSizeMultiplier,
                onValueChange = actions::updateCyrillicSize,
                valueRange = 0.6f..1.5f
            )
        }

        SettingsSection("Арапски") {
            ChoiceRow(
                choices = listOf("KFGQPC" to "KFGQPC", "Amiri" to "Amiri", "Noto Naskh" to "Noto Naskh"),
                selected = settings.arabicFont,
                onSelect = actions::updateArabicFont
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Приказ уживо:".asScript(), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                Spacer(Modifier.height(8.dp))
                SufaraText(text = "Ово је текст.\nА ово је арапски пример:\nكِتَابٌ مُفِيدٌ".asScript())
            }
        }
        Spacer(Modifier.height(28.dp))

        SettingsSection("Опције приказа") {
            SettingToggle("Приказ транскрипције (IPA)", settings.showIpa, actions::toggleIpa)
            SettingToggle("Приказ раздвојених слова", settings.showSeparatedLetters, actions::toggleSeparatedLetters)
        }

        SettingsSection("Напредно · GUI експерименти") {
            OutlinedButton(
                onClick = { showGuiExperiments = !showGuiExperiments },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.secondary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (showGuiExperiments) "Сакриј експерименте".asScript() else "Прикажи експерименте".asScript())
            }
            if (showGuiExperiments) {
            Text(
                "Ефекти су намерно независни како би се комбинације могле поредити.".asScript(),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.62f),
                fontSize = 13.sp
            )
            Spacer(Modifier.height(12.dp))
            Text("Стил геометрије".asScript(), fontWeight = FontWeight.Bold)
            ChoiceRow(
                choices = listOf(
                    BackgroundPatternStyle.RIDGE to "3D рељеф".asScript(),
                    BackgroundPatternStyle.LIGHT to "Светлије".asScript(),
                    BackgroundPatternStyle.DARK to "Тамније".asScript(),
                    BackgroundPatternStyle.DUAL to "Двоструко".asScript()
                ),
                selected = settings.backgroundPatternStyle,
                onSelect = actions::updateBackgroundPatternStyle
            )
            Spacer(Modifier.height(8.dp))
            Text("Видљивост шаре: ${(settings.backgroundPatternVisibility * 100).toInt()}%".asScript())
            Slider(
                value = settings.backgroundPatternVisibility,
                onValueChange = actions::updateBackgroundPatternVisibility,
                valueRange = 0.04f..0.22f
            )

            Text("Боја светла".asScript(), fontWeight = FontWeight.Bold)
            ChoiceRow(
                choices = listOf(
                    BackgroundLightMode.SOFT_BLUE to "Плава".asScript(),
                    BackgroundLightMode.GOLD to "Златна".asScript(),
                    BackgroundLightMode.OFF to "Угашено".asScript()
                ),
                selected = settings.backgroundLightMode,
                onSelect = actions::updateBackgroundLightMode
            )
            Text("Јачина светла".asScript(), fontWeight = FontWeight.Bold)
            ChoiceRow(
                choices = listOf(
                    BackgroundLightStrength.SUBTLE to "Блага".asScript(),
                    BackgroundLightStrength.MEDIUM to "Средња".asScript(),
                    BackgroundLightStrength.STRONG to "Јака".asScript()
                ),
                selected = settings.backgroundLightStrength,
                onSelect = actions::updateBackgroundLightStrength
            )
            SettingToggle("Спора линија око розета", settings.backgroundOrbitEnabled, actions::toggleBackgroundOrbit)
            SettingToggle("Магловито светло иза линија", settings.backgroundBlobEnabled, actions::toggleBackgroundBlob)
            SettingToggle("Суптилне честице", settings.backgroundParticlesEnabled, actions::toggleBackgroundParticles)
            if (settings.backgroundParticlesEnabled) {
                Text("Видљивост честица: ${(settings.backgroundParticleVisibility * 100).toInt()}%".asScript())
                Slider(
                    value = settings.backgroundParticleVisibility,
                    onValueChange = actions::updateBackgroundParticleVisibility,
                    valueRange = 0.5f..2.2f
                )
            }
            Text(
                "Када је светло угашено, розета остаје видљива, а светлосна линија и blob мирују.".asScript(),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
                fontSize = 12.sp
            )

            Spacer(Modifier.height(20.dp))
            Text("Кретање златне жице".asScript(), fontWeight = FontWeight.Bold)
            ChoiceRow(
                choices = listOf(
                    WireMotionStyle.ORGANIC to "Живо".asScript(),
                    WireMotionStyle.CALM to "Мирно".asScript(),
                    WireMotionStyle.UNIFORM to "Равномерно".asScript()
                ),
                selected = settings.wireMotionStyle,
                onSelect = actions::updateWireMotionStyle
            )

            Spacer(Modifier.height(20.dp))
            Text("2D мапа курса".asScript(), fontWeight = FontWeight.Bold)
            Text(
                "Путања је стабилна, распоређена у четири области и без магле преко закључаних лекција.".asScript(),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.62f),
                fontSize = 13.sp
            )
            Text("Вајб биома".asScript(), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.68f))
            ChoiceRow(
                choices = listOf(
                    LessonMapBiomeStyle.NAVY_BLUE to "Тегет → плаво".asScript(),
                    LessonMapBiomeStyle.OCEAN to "Дубоки океан".asScript(),
                    LessonMapBiomeStyle.NIGHT_GARDEN to "Ноћни врт".asScript()
                ),
                selected = settings.lessonMapBiomeStyle,
                onSelect = actions::updateLessonMapBiomeStyle
            )
            Text("Повратак на мапу".asScript(), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.68f))
            ChoiceRow(
                choices = listOf(
                    LessonMapResumeMode.LAST_POSITION to "Последња позиција".asScript(),
                    LessonMapResumeMode.CURRENT_LESSON to "Тренутна лекција".asScript()
                ),
                selected = settings.lessonMapResumeMode,
                onSelect = actions::updateLessonMapResumeMode
            )
            Text("Звездице за успех".asScript(), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.68f))
            ChoiceRow(
                choices = listOf(
                    SuccessBurstStyle.OFF to "Угашено".asScript(),
                    SuccessBurstStyle.SUBTLE to "Суптилно".asScript(),
                    SuccessBurstStyle.LIVELY to "Живо".asScript()
                ),
                selected = settings.successBurstStyle,
                onSelect = actions::updateSuccessBurstStyle
            )

            Spacer(Modifier.height(20.dp))
            Text("Приказ исходишта".asScript(), fontWeight = FontWeight.Bold)
            ChoiceRow(
                choices = listOf(
                    MakhrajDiagramStyle.COMPACT to "У додатку".asScript(),
                    MakhrajDiagramStyle.FEATURED to "Истакнуто".asScript(),
                    MakhrajDiagramStyle.HIDDEN to "Сакривено".asScript()
                ),
                selected = settings.makhrajDiagramStyle,
                onSelect = actions::updateMakhrajDiagramStyle
            )
            Spacer(Modifier.height(20.dp))
            Text("Толеранција провере писања".asScript(), fontWeight = FontWeight.Bold)
            ChoiceRow(
                choices = listOf(
                    WritingStrictness.RELAXED to "Блага".asScript(),
                    WritingStrictness.BALANCED to "Уравнотежена".asScript(),
                    WritingStrictness.STRICT to "Строга".asScript()
                ),
                selected = settings.writingStrictness,
                onSelect = actions::updateWritingStrictness
            )
            }
        }

        SettingsSection("Развој и тестирање") {
            OutlinedButton(
                onClick = { showDeveloperTools = !showDeveloperTools },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.secondary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (showDeveloperTools) "Сакриј развојне алате".asScript() else "Прикажи развојне алате".asScript())
            }
            if (showDeveloperTools) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onUnlockExpertMode,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Откључај све".asScript(), fontSize = 12.sp)
                }
                Button(
                    onClick = { confirmReset = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Обриши напредак".asScript(), fontSize = 12.sp)
                }
            }
            }
        }
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Text(title.asScript(), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.secondary)
    Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.primary)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { content() }
    Spacer(Modifier.height(28.dp))
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun <T> ChoiceRow(choices: List<Pair<T, String>>, selected: T, onSelect: (T) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        choices.forEach { (value, label) ->
            FilterChip(selected = selected == value, onClick = { onSelect(value) }, label = { Text(label) })
        }
    }
}

@Composable
private fun SettingToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label.asScript(), modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onBackground)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
