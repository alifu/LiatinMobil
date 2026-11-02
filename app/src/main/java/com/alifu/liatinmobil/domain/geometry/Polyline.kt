package com.alifu.liatinmobil.domain.geometry

import com.alifu.liatinmobil.domain.model.MapPoint
import kotlin.math.atan2
import kotlin.math.hypot

/**
 * An immutable path that can be sampled by travelled distance, so a car moving
 * along it keeps a constant speed regardless of how long each segment is.
 */
class Polyline(rawPoints: List<MapPoint>) {

    val points: List<MapPoint> = rawPoints.fold(emptyList()) { acc, point ->
        if (acc.lastOrNull() == point) acc else acc + point
    }

    /** cumulative[i] = distance from the start to points[i]. */
    private val cumulative: FloatArray
    private val bearings: FloatArray

    val length: Float

    init {
        require(points.size >= 2) { "Polyline needs at least two distinct points" }
        cumulative = FloatArray(points.size)
        for (i in 1 until points.size) {
            cumulative[i] = cumulative[i - 1] + distance(points[i - 1], points[i])
        }
        length = cumulative.last()
        bearings = FloatArray(points.size - 1) { i -> bearingDegrees(points[i], points[i + 1]) }
    }

    fun pointAt(distance: Float): MapPoint {
        val d = distance.coerceIn(0f, length)
        val segment = segmentIndexAt(d)
        val start = points[segment]
        val end = points[segment + 1]
        val segmentLength = cumulative[segment + 1] - cumulative[segment]
        val t = (d - cumulative[segment]) / segmentLength
        return MapPoint(start.x + (end.x - start.x) * t, start.y + (end.y - start.y) * t)
    }

    /**
     * Heading at [distance]. Within [turnRadius] of a corner the heading is blended
     * between the two segments so the car visibly turns instead of snapping.
     */
    fun bearingAt(distance: Float, turnRadius: Float): Float {
        val d = distance.coerceIn(0f, length)
        val i = segmentIndexAt(d)
        val intoSegment = d - cumulative[i]
        val toSegmentEnd = cumulative[i + 1] - d
        val radiusIn = if (i > 0) cornerRadius(i - 1, turnRadius) else 0f
        val radiusOut = if (i < bearings.lastIndex) cornerRadius(i, turnRadius) else 0f
        return when {
            radiusIn > 0f && intoSegment < radiusIn ->
                lerpAngle(bearings[i - 1], bearings[i], 0.5f + intoSegment / (2 * radiusIn))
            radiusOut > 0f && toSegmentEnd < radiusOut ->
                lerpAngle(bearings[i], bearings[i + 1], 0.5f - toSegmentEnd / (2 * radiusOut))
            else -> bearings[i]
        }
    }

    /** Points of the sub-path between [from] and [to] (inclusive, clamped). */
    fun slice(from: Float, to: Float): List<MapPoint> {
        val start = from.coerceIn(0f, length)
        val end = to.coerceIn(start, length)
        val inner = points.indices
            .filter { cumulative[it] > start && cumulative[it] < end }
            .map { points[it] }
        return listOf(pointAt(start)) + inner + pointAt(end)
    }

    private fun segmentLength(i: Int) = cumulative[i + 1] - cumulative[i]

    /** Corner between segment [i] and segment [i]+1, limited so blends never overlap. */
    private fun cornerRadius(i: Int, turnRadius: Float): Float =
        minOf(turnRadius, segmentLength(i) / 2, segmentLength(i + 1) / 2)

    private fun segmentIndexAt(d: Float): Int {
        var low = 0
        var high = points.size - 2
        while (low < high) {
            val mid = (low + high + 1) / 2
            if (cumulative[mid] <= d) low = mid else high = mid - 1
        }
        return low
    }
}

fun distance(a: MapPoint, b: MapPoint): Float = hypot(b.x - a.x, b.y - a.y)

/** Compass-style heading on screen: 0 = up, 90 = right, 180 = down, 270 = left. */
fun bearingDegrees(from: MapPoint, to: MapPoint): Float {
    val degrees = Math.toDegrees(atan2((to.x - from.x).toDouble(), (from.y - to.y).toDouble()))
    return normalizeDegrees(degrees.toFloat())
}

/** Interpolates between two headings along the shortest arc. */
fun lerpAngle(from: Float, to: Float, t: Float): Float {
    val delta = ((to - from) % 360f + 540f) % 360f - 180f
    return normalizeDegrees(from + delta * t)
}

private fun normalizeDegrees(degrees: Float): Float = ((degrees % 360f) + 360f) % 360f
