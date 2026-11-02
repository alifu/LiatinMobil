package com.alifu.liatinmobil.ui.ride.map

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alifu.liatinmobil.data.Landmarks
import com.alifu.liatinmobil.domain.geometry.Polyline
import com.alifu.liatinmobil.domain.model.Landmark
import com.alifu.liatinmobil.domain.model.MapPoint
import com.alifu.liatinmobil.domain.model.RidePhase
import com.alifu.liatinmobil.domain.model.Route
import com.alifu.liatinmobil.domain.simulation.RideSnapshot
import com.alifu.liatinmobil.ui.theme.DestinationOrange
import com.alifu.liatinmobil.ui.theme.GreenPrimary
import com.alifu.liatinmobil.ui.theme.TextPrimary

private val CAR_LENGTH = 46.dp
private val ROUTE_WIDTH = 7.dp
private val ROUTE_CASING = 4.dp
private val MARKER_RADIUS = 9.dp
private val OVERVIEW_PADDING = 40.dp
private val PULSE_MAX_RADIUS = 70.dp

private class RouteGeometry(route: Route) {
    val path = Polyline(route.fullPath)
    val pickupDistance = Polyline(route.approachPath).length
    val pickup: MapPoint = route.tripPath.first()
    val destination: MapPoint = route.tripPath.last()
    val tripPoints: List<MapPoint> = route.tripPath
}

/**
 * The offline map. Without a [snapshot] it shows a static overview of the trip
 * (used as a preview); with one it draws the moving car and the remaining route.
 *
 * @param overviewBlend 0 = camera follows the car, 1 = whole route visible.
 * @param topInsetPx height covered by top controls, kept free in the overview.
 * @param bottomInsetPx height covered by the bottom sheet, kept free of map focus.
 * @param searchingPulse 0..1 animation value for the "looking for driver" radar.
 */
@Composable
fun CityMapCanvas(
    route: Route,
    snapshot: RideSnapshot?,
    carColor: Color,
    contentDescription: String,
    modifier: Modifier = Modifier,
    overviewBlend: Float = 1f,
    topInsetPx: Float = 0f,
    bottomInsetPx: Float = 0f,
    searchingPulse: Float = 0f,
) {
    val textMeasurer = rememberTextMeasurer()
    val labels = remember(textMeasurer) {
        val style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        Landmarks.all.map { it to textMeasurer.measure("${it.emoji} ${it.name}", style) }
    }
    val geometry = remember(route) { RouteGeometry(route) }

    Canvas(modifier.clipToBounds().semantics { this.contentDescription = contentDescription }) {
        val overview = overviewCamera(geometry.path.points, size, topInsetPx, bottomInsetPx, OVERVIEW_PADDING.toPx())
        val camera = if (snapshot == null) {
            overview
        } else {
            lerpCamera(followCamera(snapshot.position, size, bottomInsetPx), overview, overviewBlend)
        }

        drawRect(MapColors.background)
        withTransform({
            translate(camera.focus.x - camera.center.x * camera.scale, camera.focus.y - camera.center.y * camera.scale)
            scale(camera.scale, camera.scale, pivot = Offset.Zero)
        }) {
            drawCity()
            drawRouteLines(geometry, snapshot, camera.scale)
        }
        drawLandmarkLabels(labels, camera)
        drawEndpoints(geometry, camera)
        if (snapshot != null) {
            val carCenter = camera.toScreen(snapshot.position)
            if (snapshot.phase == RidePhase.SEARCHING_DRIVER) drawSearchingPulse(carCenter, searchingPulse)
            drawTopDownCar(carCenter, snapshot.bearingDegrees, carColor, CAR_LENGTH.toPx())
        }
    }
}

/** Route widths are given in pixels, so they are divided by the camera scale. */
private fun DrawScope.drawRouteLines(geometry: RouteGeometry, snapshot: RideSnapshot?, scale: Float) {
    val width = ROUTE_WIDTH.toPx() / scale
    val casing = width + ROUTE_CASING.toPx() / scale
    if (snapshot == null) {
        drawPolyline(geometry.tripPoints, MapColors.routeCasing, casing)
        drawPolyline(geometry.tripPoints, GreenPrimary, width)
        return
    }
    val travelled = snapshot.travelledDistance
    val tripStart = maxOf(travelled, geometry.pickupDistance)
    if (travelled > geometry.pickupDistance) {
        drawPolyline(geometry.path.slice(geometry.pickupDistance, travelled), MapColors.routeTravelled, width * 0.7f)
    }
    if (travelled < geometry.pickupDistance) {
        val dash = PathEffect.dashPathEffect(floatArrayOf(width * 1.6f, width * 1.2f))
        drawPolyline(geometry.path.slice(travelled, geometry.pickupDistance), MapColors.approach, width * 0.7f, dash)
    }
    val remainingTrip = geometry.path.slice(tripStart, geometry.path.length)
    drawPolyline(remainingTrip, MapColors.routeCasing, casing)
    drawPolyline(remainingTrip, GreenPrimary, width)
}

private fun DrawScope.drawPolyline(points: List<MapPoint>, color: Color, width: Float, effect: PathEffect? = null) {
    if (points.size < 2) return
    val path = Path().apply {
        moveTo(points.first().x, points.first().y)
        points.drop(1).forEach { lineTo(it.x, it.y) }
    }
    drawPath(path, color, style = Stroke(width, cap = StrokeCap.Round, join = StrokeJoin.Round, pathEffect = effect))
}

private fun DrawScope.drawLandmarkLabels(labels: List<Pair<Landmark, TextLayoutResult>>, camera: MapCamera) {
    val padH = 6.dp.toPx()
    val padV = 3.dp.toPx()
    labels.forEach { (landmark, text) ->
        val anchor = camera.toScreen(landmark.position)
        val textSize = Size(text.size.width.toFloat(), text.size.height.toFloat())
        val topLeft = Offset(anchor.x - textSize.width / 2, anchor.y - textSize.height / 2)
        val isVisible = topLeft.x < size.width && topLeft.y < size.height &&
            topLeft.x + textSize.width > 0 && topLeft.y + textSize.height > 0
        if (!isVisible) return@forEach
        drawRoundRect(
            MapColors.labelBackground,
            topLeft - Offset(padH, padV),
            Size(textSize.width + 2 * padH, textSize.height + 2 * padV),
            CornerRadius(textSize.height),
        )
        drawText(text, topLeft = topLeft)
    }
}

private fun DrawScope.drawEndpoints(geometry: RouteGeometry, camera: MapCamera) {
    val radius = MARKER_RADIUS.toPx()
    val pickup = camera.toScreen(geometry.pickup)
    drawCircle(Color.White, radius * 1.4f, pickup)
    drawCircle(GreenPrimary, radius, pickup)
    drawCircle(Color.White, radius * 0.4f, pickup)

    val destination = camera.toScreen(geometry.destination)
    val pinHead = destination - Offset(0f, radius * 2.2f)
    val tail = Path().apply {
        moveTo(destination.x, destination.y)
        lineTo(pinHead.x - radius * 0.8f, pinHead.y + radius * 0.5f)
        lineTo(pinHead.x + radius * 0.8f, pinHead.y + radius * 0.5f)
        close()
    }
    drawPath(tail, DestinationOrange)
    drawCircle(Color.White, radius * 1.35f, pinHead)
    drawCircle(DestinationOrange, radius * 1.1f, pinHead)
    drawCircle(Color.White, radius * 0.45f, pinHead)
}

private fun DrawScope.drawSearchingPulse(center: Offset, pulse: Float) {
    val maxRadius = PULSE_MAX_RADIUS.toPx()
    listOf(0f, 0.5f).forEach { phaseShift ->
        val t = (pulse + phaseShift) % 1f
        drawCircle(GreenPrimary.copy(alpha = 0.35f * (1f - t)), maxRadius * t, center)
    }
}
