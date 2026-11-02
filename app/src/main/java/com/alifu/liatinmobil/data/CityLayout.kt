package com.alifu.liatinmobil.data

import com.alifu.liatinmobil.domain.model.MapPoint
import kotlin.math.cos
import kotlin.math.sin

/**
 * The single fictional city every route drives through. It is a grid of roads
 * with a river, a park, a roundabout and one elevated flyover. Everything here
 * is static data, so the app never needs network map tiles.
 */
object CityLayout {
    const val BLOCK_SIZE = 300f
    const val GRID_COUNT = 10
    const val WORLD_SIZE = BLOCK_SIZE * GRID_COUNT

    /** Road lines exist at x (and y) = BLOCK_SIZE * index for these indices. */
    val roadIndices: IntRange = 1 until GRID_COUNT
    val mainRoadIndices: Set<Int> = setOf(5)

    /** The river fills every block of this row; vertical roads cross it as bridges. */
    const val RIVER_ROW = 6

    val parkCells: Set<Pair<Int, Int>> = setOf(2 to 2, 3 to 2, 2 to 3, 3 to 3, 8 to 8)

    val roundaboutCenter = MapPoint(1500f, 1500f)
    const val ROUNDABOUT_RADIUS = 90f

    val flyoverStart = MapPoint(1800f, 2700f)
    val flyoverEnd = MapPoint(2700f, 1800f)

    /** Grid intersection (i, j). */
    fun node(i: Int, j: Int) = MapPoint(i * BLOCK_SIZE, j * BLOCK_SIZE)

    /** Center of block (i, j), used for landmark labels. */
    fun blockCenter(i: Int, j: Int) = MapPoint((i + 0.5f) * BLOCK_SIZE, (j + 0.5f) * BLOCK_SIZE)

    /**
     * Clockwise arc around the roundabout (clockwise on screen, as traffic flows
     * when driving on the left). Angles: 0 = east, 90 = south, -90 = north.
     */
    fun roundaboutArc(fromDegrees: Int, toDegrees: Int, stepDegrees: Int = 15): List<MapPoint> =
        (fromDegrees..toDegrees step stepDegrees).map { degrees ->
            val radians = Math.toRadians(degrees.toDouble())
            MapPoint(
                roundaboutCenter.x + ROUNDABOUT_RADIUS * cos(radians).toFloat(),
                roundaboutCenter.y + ROUNDABOUT_RADIUS * sin(radians).toFloat(),
            )
        }
}
