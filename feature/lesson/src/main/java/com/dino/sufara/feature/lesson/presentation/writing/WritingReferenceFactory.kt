package com.dino.sufara.feature.lesson.presentation.writing

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import android.util.LruCache
import androidx.core.graphics.createBitmap
import androidx.core.graphics.withTranslation
import com.dino.sufara.feature.lesson.domain.util.WritingBounds
import com.dino.sufara.feature.lesson.domain.util.WritingPoint
import com.dino.sufara.feature.lesson.domain.util.WritingReference
import com.dino.sufara.feature.lesson.domain.util.WritingReferenceComponent
import com.dino.sufara.feature.lesson.domain.util.WritingReferenceSample
import com.dino.sufara.feature.lesson.presentation.settings.SufaraFonts
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.sin

/** Builds one cached canonical guide used by both the UI and the scorer. */
object WritingReferenceFactory {
    private const val GRID_SIZE = 320
    private const val RAW_CANVAS_SIZE = 768
    private const val RAW_INSET = 72
    private const val GRID_PADDING = 38f
    private const val CACHE_ENTRIES = 16
    private val cache = LruCache<String, WritingReference>(CACHE_ENTRIES)

    fun create(context: Context, text: String, fontName: String): WritingReference {
        val key = "$fontName\u0000$text"
        synchronized(cache) { cache.get(key) }?.let { return it }
        val created = renderAndSkeletonize(context, text, fontName)
        return synchronized(cache) {
            cache.get(key) ?: created.also { cache.put(key, it) }
        }
    }

    private fun renderAndSkeletonize(context: Context, text: String, fontName: String): WritingReference {
        val rawBitmap = createBitmap(RAW_CANVAS_SIZE, RAW_CANVAS_SIZE)
        val rawCanvas = Canvas(rawBitmap)
        val paint = TextPaint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            typeface = SufaraFonts.getArabicTypeface(context, fontName)
        }
        paint.textSize = ArabicTextSizer.fitTextSizePx(
            context = context,
            text = text,
            fontName = fontName,
            maximumWidthPx = RAW_CANVAS_SIZE - RAW_INSET * 2,
            maximumHeightPx = RAW_CANVAS_SIZE - RAW_INSET * 2
        )
        val layoutWidth = RAW_CANVAS_SIZE - RAW_INSET * 2
        val layout = makeLayout(text, paint, layoutWidth)
        rawCanvas.withTranslation(
            x = RAW_INSET.toFloat(),
            y = (RAW_CANVAS_SIZE - layout.height) / 2f
        ) { layout.draw(this) }

        val detectedBounds = alphaBounds(rawBitmap, threshold = 4)
        val sourceBounds = if (detectedBounds.isEmpty) {
            Rect()
        } else {
            Rect(
                (detectedBounds.left - 8).coerceAtLeast(0),
                (detectedBounds.top - 8).coerceAtLeast(0),
                (detectedBounds.right + 8).coerceAtMost(RAW_CANVAS_SIZE),
                (detectedBounds.bottom + 8).coerceAtMost(RAW_CANVAS_SIZE)
            )
        }
        val bitmap = createBitmap(GRID_SIZE, GRID_SIZE)
        if (!sourceBounds.isEmpty) {
            val available = GRID_SIZE - GRID_PADDING * 2f
            val scale = minOf(
                available / sourceBounds.width().coerceAtLeast(1),
                available / sourceBounds.height().coerceAtLeast(1)
            )
            val width = sourceBounds.width() * scale
            val height = sourceBounds.height() * scale
            val destination = RectF(
                (GRID_SIZE - width) / 2f,
                (GRID_SIZE - height) / 2f,
                (GRID_SIZE + width) / 2f,
                (GRID_SIZE + height) / 2f
            )
            Canvas(bitmap).drawBitmap(
                rawBitmap,
                sourceBounds,
                destination,
                Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            )
        }
        rawBitmap.recycle()

        val pixels = IntArray(GRID_SIZE * GRID_SIZE)
        bitmap.getPixels(pixels, 0, GRID_SIZE, 0, 0, GRID_SIZE, GRID_SIZE)
        bitmap.recycle()
        val guideArgb = IntArray(pixels.size) { index ->
            Color.argb(Color.alpha(pixels[index]), 255, 255, 255)
        }
        val mask = BooleanArray(pixels.size) { index -> Color.alpha(pixels[index]) >= 42 }
        val distanceField = insideDistanceField(mask, GRID_SIZE, GRID_SIZE)
        val skeleton = thin(mask, GRID_SIZE, GRID_SIZE)
        val fullComponents = connectedComponents(skeleton, GRID_SIZE, GRID_SIZE).filter { it.size >= 2 }
        val samples = mutableListOf<WritingReferenceSample>()
        val components = mutableListOf<WritingReferenceComponent>()

        fullComponents.forEachIndexed { componentId, fullComponent ->
            val points = stableDownsample(fullComponent, 220)
            val first = samples.size
            points.forEach { point ->
                val index = point.y.toInt() * GRID_SIZE + point.x.toInt()
                samples += WritingReferenceSample(
                    point = point,
                    tangent = localTangent(point, points),
                    radius = distanceField[index].coerceIn(2f, GRID_SIZE * 0.075f),
                    componentId = componentId
                )
            }
            components += WritingReferenceComponent(
                id = componentId,
                sampleIndices = first until samples.size,
                approximateLength = fullComponent.size.toFloat()
            )
        }

        if (samples.isEmpty()) {
            val fallback = mask.indices
                .filter { mask[it] }
                .filterIndexed { index, _ -> index % 3 == 0 }
                .map { WritingPoint((it % GRID_SIZE).toFloat(), (it / GRID_SIZE).toFloat()) }
            return WritingReference.fromComponents(GRID_SIZE, GRID_SIZE, listOf(fallback))
                .copy(guideArgb = guideArgb, contentBounds = contentBounds(mask, GRID_SIZE, GRID_SIZE))
        }

        return WritingReference(
            width = GRID_SIZE,
            height = GRID_SIZE,
            samples = samples,
            components = components,
            guideArgb = guideArgb,
            contentBounds = contentBounds(mask, GRID_SIZE, GRID_SIZE)
        )
    }

    private fun makeLayout(text: String, paint: TextPaint, width: Int): StaticLayout =
        StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setIncludePad(true)
            .setMaxLines(1)
            .setTextDirection(TextDirectionHeuristics.RTL)
            .build()

    private fun alphaBounds(bitmap: android.graphics.Bitmap, threshold: Int): Rect {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        var left = width
        var top = height
        var right = 0
        var bottom = 0
        pixels.indices.forEach { index ->
            if (Color.alpha(pixels[index]) <= threshold) return@forEach
            val x = index % width
            val y = index / width
            left = minOf(left, x)
            top = minOf(top, y)
            right = maxOf(right, x + 1)
            bottom = maxOf(bottom, y + 1)
        }
        return if (right <= left || bottom <= top) Rect() else Rect(left, top, right, bottom)
    }

    private fun insideDistanceField(mask: BooleanArray, width: Int, height: Int): FloatArray {
        val diagonal = 1.4142135f
        val distance = FloatArray(mask.size) { index -> if (mask[index]) 10_000f else 0f }
        for (y in 0 until height) for (x in 0 until width) {
            val index = y * width + x
            if (!mask[index]) continue
            if (x > 0) distance[index] = minOf(distance[index], distance[index - 1] + 1f)
            if (y > 0) distance[index] = minOf(distance[index], distance[index - width] + 1f)
            if (x > 0 && y > 0) distance[index] = minOf(distance[index], distance[index - width - 1] + diagonal)
            if (x < width - 1 && y > 0) distance[index] = minOf(distance[index], distance[index - width + 1] + diagonal)
        }
        for (y in height - 1 downTo 0) for (x in width - 1 downTo 0) {
            val index = y * width + x
            if (!mask[index]) continue
            if (x < width - 1) distance[index] = minOf(distance[index], distance[index + 1] + 1f)
            if (y < height - 1) distance[index] = minOf(distance[index], distance[index + width] + 1f)
            if (x < width - 1 && y < height - 1) distance[index] = minOf(distance[index], distance[index + width + 1] + diagonal)
            if (x > 0 && y < height - 1) distance[index] = minOf(distance[index], distance[index + width - 1] + diagonal)
        }
        return distance
    }

    private fun thin(source: BooleanArray, width: Int, height: Int): BooleanArray {
        val pixels = source.copyOf()
        val remove = BooleanArray(pixels.size)
        repeat(128) {
            var changed = markRemovals(pixels, remove, width, height, firstPass = true)
            removeMarked(pixels, remove)
            changed = markRemovals(pixels, remove, width, height, firstPass = false) || changed
            removeMarked(pixels, remove)
            if (!changed) return pixels
        }
        return pixels
    }

    private fun markRemovals(
        pixels: BooleanArray,
        remove: BooleanArray,
        width: Int,
        height: Int,
        firstPass: Boolean
    ): Boolean {
        remove.fill(false)
        var changed = false
        for (y in 1 until height - 1) for (x in 1 until width - 1) {
            val index = y * width + x
            if (!pixels[index]) continue
            val neighbors = booleanArrayOf(
                pixels[(y - 1) * width + x], pixels[(y - 1) * width + x + 1],
                pixels[y * width + x + 1], pixels[(y + 1) * width + x + 1],
                pixels[(y + 1) * width + x], pixels[(y + 1) * width + x - 1],
                pixels[y * width + x - 1], pixels[(y - 1) * width + x - 1]
            )
            if (neighbors.count { it } !in 2..6) continue
            var transitions = 0
            repeat(8) { neighbor -> if (!neighbors[neighbor] && neighbors[(neighbor + 1) % 8]) transitions++ }
            if (transitions != 1) continue
            val firstCondition = if (firstPass) !(neighbors[0] && neighbors[2] && neighbors[4])
                else !(neighbors[0] && neighbors[2] && neighbors[6])
            val secondCondition = if (firstPass) !(neighbors[2] && neighbors[4] && neighbors[6])
                else !(neighbors[0] && neighbors[4] && neighbors[6])
            if (firstCondition && secondCondition) {
                remove[index] = true
                changed = true
            }
        }
        return changed
    }

    private fun removeMarked(pixels: BooleanArray, remove: BooleanArray) {
        remove.indices.forEach { index -> if (remove[index]) pixels[index] = false }
    }

    private fun connectedComponents(mask: BooleanArray, width: Int, height: Int): List<List<WritingPoint>> {
        val visited = BooleanArray(mask.size)
        val components = mutableListOf<List<WritingPoint>>()
        val queue = IntArray(mask.size)
        mask.indices.forEach { start ->
            if (!mask[start] || visited[start]) return@forEach
            var head = 0
            var tail = 0
            queue[tail++] = start
            visited[start] = true
            val component = mutableListOf<WritingPoint>()
            while (head < tail) {
                val index = queue[head++]
                val x = index % width
                val y = index / width
                component += WritingPoint(x.toFloat(), y.toFloat())
                for (dy in -1..1) for (dx in -1..1) {
                    if (dx == 0 && dy == 0) continue
                    val nx = x + dx
                    val ny = y + dy
                    if (nx !in 0 until width || ny !in 0 until height) continue
                    val neighbor = ny * width + nx
                    if (mask[neighbor] && !visited[neighbor]) {
                        visited[neighbor] = true
                        queue[tail++] = neighbor
                    }
                }
            }
            components += component
        }
        return components
    }

    private fun stableDownsample(points: List<WritingPoint>, maximum: Int): List<WritingPoint> {
        if (points.size <= maximum) return points
        val step = ceil(points.size.toFloat() / maximum).toInt()
        return points.filterIndexed { index, _ -> index % step == 0 }
    }

    /** Principal axis of nearby skeleton pixels; independent of BFS ordering. */
    private fun localTangent(point: WritingPoint, component: List<WritingPoint>): WritingPoint {
        val nearby = component.filter {
            val dx = it.x - point.x
            val dy = it.y - point.y
            dx * dx + dy * dy <= 100f
        }
        if (nearby.size < 2) return WritingPoint(0f, 0f)
        val meanX = nearby.sumOf { it.x.toDouble() }.toFloat() / nearby.size
        val meanY = nearby.sumOf { it.y.toDouble() }.toFloat() / nearby.size
        var xx = 0f
        var yy = 0f
        var xy = 0f
        nearby.forEach {
            val dx = it.x - meanX
            val dy = it.y - meanY
            xx += dx * dx
            yy += dy * dy
            xy += dx * dy
        }
        val angle = 0.5f * atan2(2f * xy, xx - yy)
        return WritingPoint(cos(angle), sin(angle))
    }

    private fun contentBounds(mask: BooleanArray, width: Int, height: Int): WritingBounds {
        var left = width
        var top = height
        var right = 0
        var bottom = 0
        mask.indices.forEach { index ->
            if (!mask[index]) return@forEach
            val x = index % width
            val y = index / width
            left = minOf(left, x)
            top = minOf(top, y)
            right = maxOf(right, x + 1)
            bottom = maxOf(bottom, y + 1)
        }
        return if (right <= left || bottom <= top) WritingBounds(0f, 0f, width.toFloat(), height.toFloat())
        else WritingBounds(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())
    }
}
