package com.alifu.liatinmobil.ui.ride.map

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.alifu.liatinmobil.data.CityLayout
import com.alifu.liatinmobil.data.CityLayout.BLOCK_SIZE
import com.alifu.liatinmobil.data.CityLayout.WORLD_SIZE

private const val ROAD_WIDTH = 40f
private const val MAIN_ROAD_WIDTH = 60f
private const val ROAD_BORDER = 8f
private const val BLOCK_INSET = MAIN_ROAD_WIDTH / 2 + 6f
private const val BUILDING_GAP = 18f
private const val FLYOVER_WIDTH = 52f
private const val SHADOW_OFFSET = 14f

/** Extra blocks drawn around the city so the camera never shows the world's edge. */
private const val EDGE_BLOCKS = 4
private val drawnCells = -EDGE_BLOCKS until CityLayout.GRID_COUNT + EDGE_BLOCKS
private val drawnRoads = -EDGE_BLOCKS..CityLayout.GRID_COUNT + EDGE_BLOCKS
private const val DRAWN_MIN = -EDGE_BLOCKS * BLOCK_SIZE
private const val DRAWN_MAX = WORLD_SIZE + EDGE_BLOCKS * BLOCK_SIZE

/** Draws the static city in world coordinates (call inside the camera transform). */
internal fun DrawScope.drawCity() {
    drawBlocks()
    drawRiver()
    drawRoads()
    drawRoundabout()
    drawFlyover()
}

private fun DrawScope.drawBlocks() {
    val blockSize = BLOCK_SIZE - 2 * BLOCK_INSET
    for (i in drawnCells) {
        for (j in drawnCells) {
            if (j == CityLayout.RIVER_ROW) continue
            val topLeft = Offset(i * BLOCK_SIZE + BLOCK_INSET, j * BLOCK_SIZE + BLOCK_INSET)
            if ((i to j) in CityLayout.parkCells) {
                drawPark(topLeft, blockSize)
            } else {
                drawRoundRect(MapColors.block, topLeft, Size(blockSize, blockSize), CornerRadius(16f))
                drawBuildings(topLeft, blockSize)
            }
        }
    }
}

private fun DrawScope.drawBuildings(topLeft: Offset, blockSize: Float) {
    val size = (blockSize - 3 * BUILDING_GAP) / 2
    for (col in 0..1) {
        for (row in 0..1) {
            val offset = Offset(
                topLeft.x + BUILDING_GAP + col * (size + BUILDING_GAP),
                topLeft.y + BUILDING_GAP + row * (size + BUILDING_GAP),
            )
            drawRoundRect(MapColors.building, offset, Size(size, size), CornerRadius(10f))
        }
    }
}

private fun DrawScope.drawPark(topLeft: Offset, blockSize: Float) {
    drawRoundRect(MapColors.park, topLeft, Size(blockSize, blockSize), CornerRadius(24f))
    val step = blockSize / 4
    for (col in 1..3) {
        for (row in 1..3) {
            if ((col + row) % 2 == 0) {
                drawCircle(MapColors.tree, radius = 22f, center = Offset(topLeft.x + col * step, topLeft.y + row * step))
            }
        }
    }
}

private fun DrawScope.drawRiver() {
    val top = CityLayout.RIVER_ROW * BLOCK_SIZE
    drawRect(MapColors.water, Offset(DRAWN_MIN, top), Size(DRAWN_MAX - DRAWN_MIN, BLOCK_SIZE))
}

private fun DrawScope.drawRoads() {
    // All borders first, then all fills, so intersections look seamless.
    listOf(true, false).forEach { isBorder ->
        drawnRoads.forEach { index ->
            val isMain = index in CityLayout.mainRoadIndices
            val width = (if (isMain) MAIN_ROAD_WIDTH else ROAD_WIDTH) + if (isBorder) ROAD_BORDER else 0f
            val color = when {
                isBorder -> MapColors.roadBorder
                isMain -> MapColors.mainRoad
                else -> MapColors.road
            }
            val p = index * BLOCK_SIZE
            drawLine(color, Offset(p, DRAWN_MIN), Offset(p, DRAWN_MAX), width)
            drawLine(color, Offset(DRAWN_MIN, p), Offset(DRAWN_MAX, p), width)
        }
    }
}

private fun DrawScope.drawRoundabout() {
    val center = Offset(CityLayout.roundaboutCenter.x, CityLayout.roundaboutCenter.y)
    val radius = CityLayout.ROUNDABOUT_RADIUS
    drawCircle(MapColors.roadBorder, radius, center, style = Stroke(MAIN_ROAD_WIDTH + ROAD_BORDER))
    drawCircle(MapColors.mainRoad, radius, center, style = Stroke(MAIN_ROAD_WIDTH))
    drawCircle(MapColors.park, radius - MAIN_ROAD_WIDTH / 2, center)
    drawCircle(MapColors.tree, 26f, center)
}

private fun DrawScope.drawFlyover() {
    val start = Offset(CityLayout.flyoverStart.x, CityLayout.flyoverStart.y)
    val end = Offset(CityLayout.flyoverEnd.x, CityLayout.flyoverEnd.y)
    val shadow = Offset(SHADOW_OFFSET, SHADOW_OFFSET)
    drawLine(MapColors.shadow, start + shadow, end + shadow, FLYOVER_WIDTH + ROAD_BORDER, StrokeCap.Round)
    drawLine(MapColors.flyoverBorder, start, end, FLYOVER_WIDTH + ROAD_BORDER, StrokeCap.Round)
    drawLine(MapColors.flyover, start, end, FLYOVER_WIDTH, StrokeCap.Round)
}
