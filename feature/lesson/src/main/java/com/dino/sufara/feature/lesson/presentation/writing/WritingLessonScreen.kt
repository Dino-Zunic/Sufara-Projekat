package com.dino.sufara.feature.lesson.presentation.writing

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.dino.sufara.core.designsystem.BlueMidnight
import com.dino.sufara.core.designsystem.GoldBase
import com.dino.sufara.core.designsystem.TextSilver
import com.dino.sufara.core.designsystem.components.GoldenWireButton
import com.dino.sufara.core.designsystem.components.SuccessBurst
import com.dino.sufara.feature.lesson.domain.model.LessonStep
import com.dino.sufara.feature.lesson.domain.util.WritingMatchResult
import com.dino.sufara.feature.lesson.domain.util.WritingMatcher
import com.dino.sufara.feature.lesson.domain.util.WritingPoint
import com.dino.sufara.feature.lesson.domain.util.WritingReference
import com.dino.sufara.feature.lesson.domain.util.asScript
import com.dino.sufara.feature.lesson.presentation.settings.LocalSufaraSettings
import com.dino.sufara.feature.lesson.presentation.settings.SufaraFonts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun WritingLessonScreen(
    viewModel: WritingLessonViewModel,
    onExitToMain: () -> Unit,
    onFinished: () -> Unit
) {
    val lesson by viewModel.lesson.collectAsState()
    val settings = LocalSufaraSettings.current
    val cyrillicFont = SufaraFonts.getCyrillicFont(settings.cyrillicFont)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var currentIndex by remember { mutableIntStateOf(0) }
    var showExitDialog by remember { mutableStateOf(false) }
    var successTrigger by remember { mutableIntStateOf(0) }

    val examples = lesson?.steps?.filterIsInstance<LessonStep.Example>().orEmpty()
    if (lesson == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = GoldBase)
        }
        return
    }
    if (examples.isEmpty()) {
        LaunchedEffect(lesson?.id) { onFinished() }
        return
    }

    val example = examples[currentIndex]
    var strokes by remember(currentIndex, example.text) { mutableStateOf<List<List<WritingPoint>>>(emptyList()) }
    var currentStroke by remember(currentIndex, example.text) { mutableStateOf<List<WritingPoint>>(emptyList()) }
    var reference by remember(currentIndex, example.text, settings.arabicFont) { mutableStateOf<WritingReference?>(null) }
    var result by remember(currentIndex, example.text) { mutableStateOf<WritingMatchResult?>(null) }
    var checking by remember(currentIndex, example.text) { mutableStateOf(false) }

    LaunchedEffect(currentIndex, example.text, settings.arabicFont) {
        reference = withContext(Dispatchers.Default) {
            WritingReferenceFactory.create(context.applicationContext, example.text, settings.arabicFont)
        }
    }

    BackHandler { showExitDialog = true }
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Напуштање вежбе".asScript(), fontFamily = cyrillicFont) },
            text = { Text("Да ли сте сигурни да желите да изађете на главни мени?".asScript(), fontFamily = cyrillicFont) },
            confirmButton = {
                TextButton(onClick = { showExitDialog = false; onExitToMain() }) {
                    Text("Напусти вежбу".asScript(), color = MaterialTheme.colorScheme.error, fontFamily = cyrillicFont)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Остани".asScript(), color = GoldBase, fontWeight = FontWeight.Bold, fontFamily = cyrillicFont)
                }
            }
        )
    }

    val passed = result?.score?.let { it >= settings.writingStrictness.passingScore } == true
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { showExitDialog = true }) {
                Icon(Icons.Default.Close, contentDescription = "Изађи из вежбе".asScript())
            }
            LinearProgressIndicator(
                progress = (currentIndex + 1f) / examples.size,
                modifier = Modifier.weight(1f).height(4.dp),
                color = GoldBase,
                trackColor = BlueMidnight
            )
            Spacer(Modifier.width(12.dp))
            Text("${currentIndex + 1}/${examples.size}", color = TextSilver, fontFamily = cyrillicFont)
        }

        Spacer(Modifier.height(10.dp))
        Text(
            text = "Испишите облик преко водича".asScript(),
            color = TextSilver.copy(alpha = 0.78f),
            fontFamily = cyrillicFont,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))

        BoxWithConstraints(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            val side = minOf(maxWidth, maxHeight, 460.dp)
            Card(
                modifier = Modifier.width(side).height(side),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = BlueMidnight)
            ) {
                WritingCanvas(
                    reference = reference,
                    strokes = strokes,
                    currentStroke = currentStroke,
                    inputEnabled = !checking && !passed,
                    onStrokeStarted = {
                        result = null
                        currentStroke = it
                    },
                    onStrokeChanged = { currentStroke = it },
                    onStrokeFinished = {
                        strokes = strokes + listOf(it)
                        currentStroke = emptyList()
                    },
                    successTrigger = successTrigger,
                    successParticleCount = settings.successBurstStyle.count
                )
            }
        }

        result?.let { match ->
            val isPassed = match.score >= settings.writingStrictness.passingScore
            Text(
                text = if (isPassed) "Одлично · ${match.score}%".asScript() else "Још мало · ${match.score}%".asScript(),
                color = if (isPassed) Color(0xFF9FE0AE) else Color(0xFFFFA3A3),
                fontFamily = cyrillicFont,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )
        } ?: Spacer(Modifier.height(36.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = { if (strokes.isNotEmpty()) strokes = strokes.dropLast(1); result = null },
                enabled = strokes.isNotEmpty() && !checking && !passed,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSilver),
                modifier = Modifier.weight(1f).height(48.dp)
            ) { Text("Опозови".asScript(), fontFamily = cyrillicFont) }
            Button(
                onClick = { strokes = emptyList(); currentStroke = emptyList(); result = null },
                enabled = strokes.isNotEmpty() && !checking && !passed,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.weight(1f).height(48.dp)
            ) { Text("Обриши".asScript(), fontFamily = cyrillicFont) }
        }
        Spacer(Modifier.height(10.dp))
        GoldenWireButton(
            text = when {
                passed && currentIndex == examples.lastIndex -> "Заврши".asScript()
                passed -> "Следећи пример".asScript()
                checking -> "Провера...".asScript()
                else -> "Провери".asScript()
            },
            onClick = {
                if (passed) {
                    if (currentIndex == examples.lastIndex) viewModel.complete(onFinished) else currentIndex++
                    return@GoldenWireButton
                }
                val preparedReference = reference ?: return@GoldenWireButton
                val preparedStrokes = strokes
                if (preparedStrokes.isEmpty()) return@GoldenWireButton
                checking = true
                scope.launch {
                    val match = withContext(Dispatchers.Default) {
                        WritingMatcher.score(preparedReference, preparedStrokes)
                    }
                    result = match
                    checking = false
                    if (match.score >= settings.writingStrictness.passingScore) successTrigger++
                }
            },
            enabled = passed || (strokes.isNotEmpty() && reference != null && !checking),
            font = cyrillicFont,
            modifier = Modifier.fillMaxWidth().height(54.dp)
        )
    }
}

@Composable
private fun WritingCanvas(
    reference: WritingReference?,
    strokes: List<List<WritingPoint>>,
    currentStroke: List<WritingPoint>,
    inputEnabled: Boolean,
    onStrokeStarted: (List<WritingPoint>) -> Unit,
    onStrokeChanged: (List<WritingPoint>) -> Unit,
    onStrokeFinished: (List<WritingPoint>) -> Unit,
    successTrigger: Int,
    successParticleCount: Int
) {
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    val guide = remember(reference) {
        reference?.takeIf { it.guideArgb.size == it.width * it.height }?.let {
            Bitmap.createBitmap(it.guideArgb, it.width, it.height, Bitmap.Config.ARGB_8888).asImageBitmap()
        }
    }

    Box(Modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { canvasSize = it }
                .pointerInput(reference, inputEnabled, canvasSize) {
                    val activeReference = reference ?: return@pointerInput
                    if (!inputEnabled || canvasSize == IntSize.Zero) return@pointerInput
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        down.consume()
                        val stroke = mutableListOf(screenToCanonical(down.position, canvasSize, activeReference))
                        onStrokeStarted(stroke.toList())
                        var pressed = true
                        while (pressed) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            val point = screenToCanonical(change.position, canvasSize, activeReference)
                            val last = stroke.last()
                            if (hypot(point.x - last.x, point.y - last.y) >= 1.35f) {
                                stroke += point
                                onStrokeChanged(stroke.toList())
                            }
                            pressed = change.pressed
                            change.consume()
                        }
                        if (stroke.size == 1) stroke += stroke.first()
                        onStrokeFinished(stroke.toList())
                    }
                }
        ) {
            val activeReference = reference ?: return@Canvas
            val transform = GuideTransform.create(size.width, size.height, activeReference)
            guide?.let { image ->
                drawImage(
                    image = image,
                    srcOffset = IntOffset.Zero,
                    srcSize = IntSize(activeReference.width, activeReference.height),
                    dstOffset = IntOffset(transform.left.roundToInt(), transform.top.roundToInt()),
                    dstSize = IntSize(transform.side.roundToInt(), transform.side.roundToInt()),
                    colorFilter = ColorFilter.tint(Color(0xFF315D9B).copy(alpha = 0.96f))
                )
            }
            (strokes + listOf(currentStroke)).forEach { stroke ->
                if (stroke.isEmpty()) return@forEach
                if (stroke.size == 1) {
                    drawCircle(GoldBase, radius = 3.dp.toPx(), center = transform.toScreen(stroke.first()))
                    return@forEach
                }
                val path = Path().apply {
                    val first = transform.toScreen(stroke.first())
                    moveTo(first.x, first.y)
                    stroke.drop(1).forEach { point ->
                        val mapped = transform.toScreen(point)
                        lineTo(mapped.x, mapped.y)
                    }
                }
                drawPath(
                    path = path,
                    color = GoldBase,
                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
        }
        SuccessBurst(
            trigger = successTrigger,
            enabled = successParticleCount > 0,
            particleCount = successParticleCount,
            modifier = Modifier.fillMaxSize()
        )
    }
}

private data class GuideTransform(val left: Float, val top: Float, val side: Float, val reference: WritingReference) {
    fun toScreen(point: WritingPoint): Offset = Offset(
        x = left + point.x / reference.width * side,
        y = top + point.y / reference.height * side
    )

    companion object {
        fun create(width: Float, height: Float, reference: WritingReference): GuideTransform {
            val side = min(width, height).coerceAtLeast(1f)
            return GuideTransform((width - side) / 2f, (height - side) / 2f, side, reference)
        }
    }
}

private fun screenToCanonical(position: Offset, canvasSize: IntSize, reference: WritingReference): WritingPoint {
    val transform = GuideTransform.create(canvasSize.width.toFloat(), canvasSize.height.toFloat(), reference)
    return WritingPoint(
        x = ((position.x - transform.left) / transform.side * reference.width).coerceIn(-8f, reference.width + 8f),
        y = ((position.y - transform.top) / transform.side * reference.height).coerceIn(-8f, reference.height + 8f)
    )
}
