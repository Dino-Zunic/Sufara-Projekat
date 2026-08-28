package com.dino.sufara.feature.lesson.presentation.grid

import android.content.Context
import android.graphics.Paint
import android.graphics.Path as AndroidPath
import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.dino.sufara.core.designsystem.BlueMidnight
import com.dino.sufara.core.designsystem.BlueRoyal
import com.dino.sufara.core.designsystem.GoldBase
import com.dino.sufara.core.designsystem.GoldLight
import com.dino.sufara.core.designsystem.TextParchment
import com.dino.sufara.core.designsystem.TextSilver
import com.dino.sufara.core.designsystem.components.rememberGoldenWireAngle
import com.dino.sufara.feature.lesson.domain.model.Lesson
import com.dino.sufara.feature.lesson.domain.model.LessonStatus
import com.dino.sufara.feature.lesson.domain.util.mapDisplaySymbol
import com.dino.sufara.feature.lesson.domain.util.asScript
import com.dino.sufara.feature.lesson.presentation.settings.LessonMapBiomeStyle
import com.dino.sufara.feature.lesson.presentation.settings.LessonMapResumeMode
import com.dino.sufara.feature.lesson.presentation.settings.LocalSufaraSettings
import com.dino.sufara.feature.lesson.presentation.settings.SufaraFonts
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

private enum class CoursePart { READING, WRITING }
private data class CourseNode(val lesson: Lesson, val part: CoursePart)

@Composable
fun LessonGridScreen(
    viewModel: LessonGridViewModel,
    onLessonClick: (String) -> Unit,
    onWritingLessonClick: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val reading by viewModel.lessons.collectAsState()
    val writing by viewModel.writingLessons.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    UnifiedCourseMap(reading, writing, onLessonClick, onWritingLessonClick, onNavigateBack)
}

@Composable
private fun UnifiedCourseMap(
    reading: List<Lesson>,
    writing: List<Lesson>,
    onReadingClick: (String) -> Unit,
    onWritingClick: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val settings = LocalSufaraSettings.current
    val context = LocalContext.current
    val density = LocalDensity.current
    val prefs = remember { context.getSharedPreferences("sufara_map", Context.MODE_PRIVATE) }
    val nodes = remember(reading, writing) {
        reading.map { CourseNode(it, CoursePart.READING) } +
            writing.map { CourseNode(it, CoursePart.WRITING) }
    }
    if (nodes.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = GoldBase)
        }
        return
    }

    val geometry = remember(nodes.size) {
        CourseMapGenerator.generate(CourseMapConfig(lessonCount = nodes.size))
    }
    val displayTitles = nodes.map { it.lesson.title.asScript() }
    val readingLabel = "ЧИТАЊЕ".asScript()
    val writingLabel = "ПИСАЊЕ".asScript()
    val completedStateLabel = "Завршено".asScript()
    val availableStateLabel = "Доступно".asScript()
    val lockedStateLabel = "Закључано".asScript()
    val openLessonLabel = "Отвори лекцију".asScript()
    val arabicTypeface = remember(context.applicationContext, settings.arabicFont) {
        SufaraFonts.getArabicTypeface(context.applicationContext, settings.arabicFont)
    }
    val labelTypeface = remember(context.applicationContext, settings.cyrillicFont) {
        SufaraFonts.getCyrillicTypeface(context.applicationContext, settings.cyrillicFont)
    }
    val arabicPaint = remember(arabicTypeface) {
        Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = arabicTypeface }
    }
    val labelPaint = remember(labelTypeface) {
        Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = labelTypeface }
    }
    val currentIndex = currentNodeIndex(nodes)
    val currentWireAngle = rememberGoldenWireAngle(
        enabled = true,
        baseDurationMillis = 5200,
        phaseKey = "course-current"
    )

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val viewportWidth = with(density) { maxWidth.toPx() }.coerceAtLeast(1f)
        val viewportHeight = with(density) { maxHeight.toPx() }.coerceAtLeast(1f)
        val screenCenter = Offset(viewportWidth / 2f, viewportHeight / 2f)
        val baseScale = viewportHeight / (geometry.lessonSpacing * 5.1f)
        var zoom by remember { mutableFloatStateOf(1f) }
        var cameraCenter by remember { mutableStateOf(MapPoint(geometry.width / 2f, geometry.height / 2f)) }
        var initialized by remember(settings.lessonMapResumeMode, nodes.size) { mutableStateOf(false) }
        val latestCenter by rememberUpdatedState(cameraCenter)
        val latestZoom by rememberUpdatedState(zoom)

        fun constrained(center: MapPoint, requestedZoom: Float): MapPoint {
            val safeZoom = requestedZoom.coerceIn(MIN_ZOOM, MAX_ZOOM)
            val visibleWidth = viewportWidth / (baseScale * safeZoom)
            val visibleHeight = viewportHeight / (baseScale * safeZoom)
            val x = if (visibleWidth >= geometry.width) geometry.width / 2f else {
                center.x.coerceIn(visibleWidth / 2f, geometry.width - visibleWidth / 2f)
            }
            val y = if (visibleHeight >= geometry.height) geometry.height / 2f else {
                center.y.coerceIn(visibleHeight / 2f, geometry.height - visibleHeight / 2f)
            }
            return MapPoint(x, y)
        }

        fun currentLessonCamera(): MapPoint {
            val current = geometry.lessons[currentIndex.coerceIn(0, geometry.lessons.lastIndex)]
            val aheadIndex = (currentIndex + 3).coerceAtMost(geometry.lessons.lastIndex)
            val ahead = geometry.lessons[aheadIndex]
            return constrained(
                MapPoint(
                    x = current.x * 0.72f + ahead.x * 0.28f,
                    y = current.y * 0.72f + ahead.y * 0.28f
                ),
                zoom
            )
        }

        LaunchedEffect(
            geometry,
            viewportWidth,
            viewportHeight,
            settings.lessonMapResumeMode,
            currentIndex
        ) {
            if (initialized) return@LaunchedEffect
            val useSaved = settings.lessonMapResumeMode == LessonMapResumeMode.LAST_POSITION &&
                prefs.getInt("layout_version", 0) == MAP_LAYOUT_VERSION &&
                prefs.contains("center_x") && prefs.contains("center_y")
            if (useSaved) {
                zoom = prefs.getFloat("zoom", 1f).coerceIn(MIN_ZOOM, MAX_ZOOM)
                cameraCenter = constrained(
                    MapPoint(prefs.getFloat("center_x", 0f), prefs.getFloat("center_y", 0f)),
                    zoom
                )
            } else {
                zoom = 1f
                cameraCenter = currentLessonCamera()
            }
            initialized = true
        }

        DisposableEffect(Unit) {
            onDispose {
                prefs.edit {
                    putInt("layout_version", MAP_LAYOUT_VERSION)
                    putFloat("center_x", latestCenter.x)
                    putFloat("center_y", latestCenter.y)
                    putFloat("zoom", latestZoom)
                }
            }
        }

        val fullPath = remember(geometry) { geometry.sampledPath.toComposePath() }
        val litPath = remember(geometry, currentIndex) {
            geometry.pathThroughNode((currentIndex + 1).coerceAtMost(nodes.lastIndex))
        }
        val scale = baseScale * zoom

        Box(
            Modifier
                .fillMaxSize()
                .pointerInput(geometry, viewportWidth, viewportHeight, baseScale) {
                    detectTransformGestures { centroid, pan, zoomChange, _ ->
                        val oldScale = baseScale * zoom
                        val pivot = MapPoint(
                            x = cameraCenter.x + (centroid.x - screenCenter.x) / oldScale,
                            y = cameraCenter.y + (centroid.y - screenCenter.y) / oldScale
                        )
                        val newZoom = (zoom * zoomChange).coerceIn(MIN_ZOOM, MAX_ZOOM)
                        val newScale = baseScale * newZoom
                        val zoomedCenter = MapPoint(
                            x = pivot.x - (centroid.x - screenCenter.x) / newScale,
                            y = pivot.y - (centroid.y - screenCenter.y) / newScale
                        )
                        zoom = newZoom
                        cameraCenter = constrained(
                            MapPoint(
                                x = zoomedCenter.x - pan.x / newScale,
                                y = zoomedCenter.y - pan.y / newScale
                            ),
                            newZoom
                        )
                    }
                }
                .pointerInput(nodes, geometry, viewportWidth, viewportHeight, baseScale) {
                    detectTapGestures { tap ->
                        val liveScale = baseScale * zoom
                        val world = MapPoint(
                            x = cameraCenter.x + (tap.x - screenCenter.x) / liveScale,
                            y = cameraCenter.y + (tap.y - screenCenter.y) / liveScale
                        )
                        val touchRadius = max(geometry.lessonRadius, 28.dp.toPx() / liveScale)
                        val hit = geometry.lessons.indices
                            .asSequence()
                            .filter { nodes[it].lesson.status != LessonStatus.LOCKED }
                            .map { index -> index to distanceSquared(geometry.lessons[index], world) }
                            .filter { it.second <= touchRadius * touchRadius }
                            .minByOrNull { it.second }
                            ?.first
                        hit?.let { index ->
                            val node = nodes[index]
                            if (node.part == CoursePart.READING) onReadingClick(node.lesson.id)
                            else onWritingClick(node.lesson.id)
                        }
                    }
                }
        ) {
            Canvas(Modifier.fillMaxSize()) {
                withTransform({
                    translate(screenCenter.x, screenCenter.y)
                    scale(scale, scale, pivot = Offset.Zero)
                    translate(-cameraCenter.x, -cameraCenter.y)
                }) {
                    drawBiomes(geometry.regions, settings.lessonMapBiomeStyle)
                    drawPath(
                        path = fullPath,
                        color = Color.Black.copy(alpha = 0.40f),
                        style = Stroke(width = 16f)
                    )
                    drawPath(
                        path = fullPath,
                        color = BlueRoyal.copy(alpha = 0.42f),
                        style = Stroke(width = 4f)
                    )
                    drawPath(
                        path = litPath,
                        color = GoldBase.copy(alpha = 0.10f),
                        style = Stroke(width = 26f)
                    )
                    drawPath(
                        path = litPath,
                        color = GoldBase.copy(alpha = 0.55f),
                        style = Stroke(width = 3f)
                    )

                    val visibleHalfWidth = viewportWidth / (2f * scale) + geometry.lessonRadius * 2f
                    val visibleHalfHeight = viewportHeight / (2f * scale) + geometry.lessonRadius * 2f
                    geometry.lessons.forEachIndexed { index, point ->
                        if (abs(point.x - cameraCenter.x) > visibleHalfWidth ||
                            abs(point.y - cameraCenter.y) > visibleHalfHeight
                        ) return@forEachIndexed
                        drawCourseNode(
                            node = nodes[index],
                            point = point,
                            title = displayTitles[index],
                            radius = geometry.lessonRadius,
                            isCurrent = index == currentIndex,
                            wireAngle = currentWireAngle,
                            arabicPaint = arabicPaint,
                            labelPaint = labelPaint
                        )
                    }

                    geometry.lessons.firstOrNull()?.let { point ->
                        drawWorldLabel(
                            readingLabel,
                            point.x + geometry.lessonRadius * 2.8f,
                            point.y - geometry.lessonRadius * 0.2f,
                            labelTypeface
                        )
                    }
                    geometry.lessons.getOrNull(reading.size)?.let { point ->
                        drawWorldLabel(
                            writingLabel,
                            point.x,
                            point.y - geometry.lessonRadius - 64f,
                            labelTypeface,
                            GoldBase
                        )
                    }
                }
            }

            if (initialized) {
                val semanticNodeSize = 48.dp
                val semanticNodeSizePx = with(density) { semanticNodeSize.toPx() }
                geometry.lessons.forEachIndexed { index, point ->
                    val screenX = screenCenter.x + (point.x - cameraCenter.x) * scale
                    val screenY = screenCenter.y + (point.y - cameraCenter.y) * scale
                    if (screenX !in -semanticNodeSizePx..viewportWidth + semanticNodeSizePx ||
                        screenY !in -semanticNodeSizePx..viewportHeight + semanticNodeSizePx
                    ) return@forEachIndexed
                    val node = nodes[index]
                    val isLocked = node.lesson.status == LessonStatus.LOCKED
                    Box(
                        Modifier
                            .offset {
                                IntOffset(
                                    (screenX - semanticNodeSizePx / 2f).toInt(),
                                    (screenY - semanticNodeSizePx / 2f).toInt()
                                )
                            }
                            .size(semanticNodeSize)
                            .semantics {
                                role = Role.Button
                                contentDescription = displayTitles[index]
                                stateDescription = when (node.lesson.status) {
                                    LessonStatus.COMPLETED -> completedStateLabel
                                    LessonStatus.UNLOCKED -> availableStateLabel
                                    LessonStatus.LOCKED -> lockedStateLabel
                                }
                                if (isLocked) disabled() else onClick(label = openLessonLabel) {
                                    if (node.part == CoursePart.READING) onReadingClick(node.lesson.id)
                                    else onWritingClick(node.lesson.id)
                                    true
                                }
                            }
                    )
                }
            }

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(MaterialTheme.colorScheme.background.copy(alpha = 0.97f), Color.Transparent)
                        )
                    )
            ) {
                Row(
                    Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад".asScript())
                    }
                    Text(
                        "Мапа курса".asScript(),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "Превуците · приближите".asScript(),
                        color = TextSilver.copy(alpha = 0.48f),
                        fontSize = 10.sp
                    )
                    IconButton(onClick = { cameraCenter = currentLessonCamera() }) {
                        Icon(Icons.Default.Home, contentDescription = "Тренутна лекција".asScript(), tint = GoldBase)
                    }
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBiomes(
    regions: List<MapRegion>,
    style: LessonMapBiomeStyle
) {
    val colors = when (style) {
        LessonMapBiomeStyle.NAVY_BLUE -> listOf(Color(0x18172A50), Color(0x18204468))
        LessonMapBiomeStyle.OCEAN -> listOf(Color(0x18112642), Color(0x1C2A6574))
        LessonMapBiomeStyle.NIGHT_GARDEN -> listOf(Color(0x18162A46), Color(0x18294E47))
    }
    regions.forEachIndexed { index, region ->
        val center = Offset((region.left + region.right) / 2f, (region.top + region.bottom) / 2f)
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(colors[index % 2], Color.Transparent),
                center = center,
                radius = max(region.right - region.left, region.bottom - region.top) * 0.72f
            ),
            topLeft = Offset(region.left, region.top),
            size = Size(region.right - region.left, region.bottom - region.top)
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCourseNode(
    node: CourseNode,
    point: MapPoint,
    title: String,
    radius: Float,
    isCurrent: Boolean,
    wireAngle: Float,
    arabicPaint: Paint,
    labelPaint: Paint
) {
    val center = Offset(point.x, point.y)
    val enabled = node.lesson.status != LessonStatus.LOCKED
    val completed = node.lesson.status == LessonStatus.COMPLETED
    val alpha = if (enabled) 1f else 0.25f
    if (isCurrent) {
        drawCircle(GoldBase.copy(alpha = 0.15f), radius = radius * 1.32f, center = center)
        rotate(wireAngle, center) {
            drawArc(
                brush = Brush.sweepGradient(
                    0f to GoldBase.copy(alpha = 0.35f),
                    0.82f to GoldBase.copy(alpha = 0.45f),
                    0.96f to GoldLight,
                    1f to GoldBase.copy(alpha = 0.35f)
                ),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(center.x - radius * 1.12f, center.y - radius * 1.12f),
                size = Size(radius * 2.24f, radius * 2.24f),
                style = Stroke(width = 5f)
            )
        }
    }
    drawCircle(
        color = if (node.part == CoursePart.READING) BlueMidnight.copy(alpha = alpha) else Color(0xFF123052).copy(alpha = alpha),
        radius = radius,
        center = center
    )
    drawCircle(
        color = when {
            isCurrent -> GoldLight.copy(alpha = 0.9f)
            completed -> GoldBase.copy(alpha = 0.62f)
            enabled -> TextSilver.copy(alpha = 0.36f)
            else -> BlueRoyal.copy(alpha = 0.24f)
        },
        radius = radius,
        center = center,
        style = Stroke(width = if (isCurrent) 3.5f else 2.5f)
    )

    if (node.lesson.symbol == "📖" || node.lesson.symbol == ".") {
        drawBook(center, radius * 0.42f, TextParchment.copy(alpha = alpha))
    } else {
        drawTightlyCenteredGlyph(
            mapDisplaySymbol(node.lesson.symbol),
            center,
            radius * 1.12f,
            arabicPaint,
            TextParchment.copy(alpha = alpha)
        )
    }
    drawCenteredLabel(title, center.x, center.y + radius + 33f, radius * 2.25f, labelPaint, TextSilver.copy(alpha = alpha))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTightlyCenteredGlyph(
    text: String,
    center: Offset,
    available: Float,
    paint: Paint,
    color: Color
) {
    paint.color = color.toArgb()
    paint.textSize = available
    val path = AndroidPath()
    paint.getTextPath(text, 0, text.length, 0f, 0f, path)
    val bounds = RectF()
    path.computeBounds(bounds, true)
    if (bounds.width() <= 0f || bounds.height() <= 0f) return
    paint.textSize *= min(available / bounds.width(), available / bounds.height())
    path.reset()
    paint.getTextPath(text, 0, text.length, 0f, 0f, path)
    path.computeBounds(bounds, true)
    val x = center.x - (bounds.left + bounds.right) / 2f
    val baseline = center.y - (bounds.top + bounds.bottom) / 2f
    drawContext.canvas.nativeCanvas.drawText(text, x, baseline, paint)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCenteredLabel(
    text: String,
    centerX: Float,
    baselineY: Float,
    maximumWidth: Float,
    paint: Paint,
    color: Color
) {
    paint.color = color.toArgb()
    paint.textSize = 22f
    paint.textAlign = Paint.Align.CENTER
    paint.isFakeBoldText = true
    if (paint.measureText(text) <= maximumWidth) {
        drawContext.canvas.nativeCanvas.drawText(text, centerX, baselineY, paint)
        return
    }
    val words = text.split(' ')
    if (words.size == 1) {
        var shortened = text
        while (shortened.length > 2 && paint.measureText("$shortened…") > maximumWidth) shortened = shortened.dropLast(1)
        drawContext.canvas.nativeCanvas.drawText("$shortened…", centerX, baselineY, paint)
        return
    }
    val split = (words.size + 1) / 2
    val first = words.take(split).joinToString(" ")
    val second = words.drop(split).joinToString(" ")
    drawContext.canvas.nativeCanvas.drawText(first, centerX, baselineY - 11f, paint)
    drawContext.canvas.nativeCanvas.drawText(second, centerX, baselineY + 15f, paint)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawWorldLabel(
    text: String,
    x: Float,
    y: Float,
    typeface: android.graphics.Typeface,
    color: Color = TextSilver
) {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.typeface = typeface
        textAlign = Paint.Align.CENTER
        textSize = 24f
        letterSpacing = 0.12f
        this.color = color.copy(alpha = 0.42f).toArgb()
    }
    drawContext.canvas.nativeCanvas.drawText(text, x, y, paint)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBook(center: Offset, radius: Float, color: Color) {
    val leftPage = Path().apply {
        moveTo(center.x, center.y - radius * 0.72f)
        quadraticBezierTo(center.x - radius * 0.54f, center.y - radius, center.x - radius, center.y - radius * 0.66f)
        lineTo(center.x - radius, center.y + radius * 0.68f)
        quadraticBezierTo(center.x - radius * 0.46f, center.y + radius * 0.42f, center.x, center.y + radius * 0.84f)
        close()
    }
    val rightPage = Path().apply {
        moveTo(center.x, center.y - radius * 0.72f)
        quadraticBezierTo(center.x + radius * 0.54f, center.y - radius, center.x + radius, center.y - radius * 0.66f)
        lineTo(center.x + radius, center.y + radius * 0.68f)
        quadraticBezierTo(center.x + radius * 0.46f, center.y + radius * 0.42f, center.x, center.y + radius * 0.84f)
        close()
    }
    drawPath(leftPage, color)
    drawPath(rightPage, color.copy(alpha = color.alpha * 0.82f))
    drawLine(BlueMidnight.copy(alpha = 0.72f), Offset(center.x, center.y - radius * 0.68f), Offset(center.x, center.y + radius * 0.72f), 2f)
}

private fun List<MapPoint>.toComposePath(): Path = Path().apply {
    firstOrNull()?.let { first ->
        moveTo(first.x, first.y)
        drop(1).forEach { lineTo(it.x, it.y) }
    }
}

private fun CourseMapGeometry.pathThroughNode(nodeIndex: Int): Path {
    if (sampledPath.isEmpty()) return Path()
    val fraction = if (lessons.size <= 1) 1f else nodeIndex.coerceIn(0, lessons.lastIndex) / lessons.lastIndex.toFloat()
    val target = totalLength * fraction
    val result = mutableListOf(sampledPath.first())
    for (index in 1 until sampledPath.size) {
        if (cumulativeLengths[index] <= target) {
            result += sampledPath[index]
            continue
        }
        val previousLength = cumulativeLengths[index - 1]
        val local = ((target - previousLength) / (cumulativeLengths[index] - previousLength).coerceAtLeast(0.001f)).coerceIn(0f, 1f)
        val previous = sampledPath[index - 1]
        val next = sampledPath[index]
        result += MapPoint(previous.x + (next.x - previous.x) * local, previous.y + (next.y - previous.y) * local)
        break
    }
    return result.toComposePath()
}

private fun currentNodeIndex(nodes: List<CourseNode>): Int {
    val unlocked = nodes.indexOfFirst { it.lesson.status == LessonStatus.UNLOCKED }
    if (unlocked >= 0) return unlocked
    return nodes.indexOfLast { it.lesson.status == LessonStatus.COMPLETED }.coerceAtLeast(0)
}

private fun distanceSquared(first: MapPoint, second: MapPoint): Float {
    val dx = first.x - second.x
    val dy = first.y - second.y
    return dx * dx + dy * dy
}

private fun Color.toArgb(): Int = android.graphics.Color.argb(
    (alpha * 255f).toInt(),
    (red * 255f).toInt(),
    (green * 255f).toInt(),
    (blue * 255f).toInt()
)

private const val MIN_ZOOM = 0.5f
private const val MAX_ZOOM = 1.5f
private const val MAP_LAYOUT_VERSION = 5
