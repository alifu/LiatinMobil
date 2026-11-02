package com.alifu.liatinmobil.ui.ride.map

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.alifu.liatinmobil.domain.model.MapPoint

/** World units visible across the screen width while following the car. */
private const val FOLLOW_VIEW_WIDTH = 1200f

/** Maps world coordinates to screen pixels: world [center] is drawn at screen [focus]. */
internal data class MapCamera(val center: MapPoint, val scale: Float, val focus: Offset) {
    fun toScreen(point: MapPoint) = Offset(
        focus.x + (point.x - center.x) * scale,
        focus.y + (point.y - center.y) * scale,
    )
}

/** Keeps [target] in the middle of the area above the bottom sheet. */
internal fun followCamera(target: MapPoint, size: Size, bottomInset: Float) = MapCamera(
    center = target,
    scale = size.width / FOLLOW_VIEW_WIDTH,
    focus = Offset(size.width / 2, visibleHeight(size, bottomInset) / 2),
)

/** Fits every point between the top controls ([topInset]) and the bottom sheet. */
internal fun overviewCamera(
    points: List<MapPoint>,
    size: Size,
    topInset: Float,
    bottomInset: Float,
    padding: Float,
): MapCamera {
    val minX = points.minOf { it.x }
    val maxX = points.maxOf { it.x }
    val minY = points.minOf { it.y }
    val maxY = points.maxOf { it.y }
    val height = visibleHeight(size, bottomInset + topInset)
    val scaleX = (size.width - 2 * padding) / (maxX - minX).coerceAtLeast(1f)
    val scaleY = (height - 2 * padding) / (maxY - minY).coerceAtLeast(1f)
    return MapCamera(
        center = MapPoint((minX + maxX) / 2, (minY + maxY) / 2),
        scale = minOf(scaleX, scaleY).coerceAtLeast(MIN_SCALE),
        focus = Offset(size.width / 2, topInset + height / 2),
    )
}

internal fun lerpCamera(from: MapCamera, to: MapCamera, t: Float) = MapCamera(
    center = MapPoint(lerp(from.center.x, to.center.x, t), lerp(from.center.y, to.center.y, t)),
    scale = lerp(from.scale, to.scale, t),
    focus = Offset(lerp(from.focus.x, to.focus.x, t), lerp(from.focus.y, to.focus.y, t)),
)

private const val MIN_SCALE = 0.01f

private fun visibleHeight(size: Size, bottomInset: Float) = (size.height - bottomInset).coerceAtLeast(1f)

private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t
