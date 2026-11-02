package com.alifu.liatinmobil.ui.ride.map

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.lerp

private val WindowColor = Color(0xFF263544)
private val WheelColor = Color(0xFF2B2B2B)
private val HeadlightColor = Color(0xFFFFF59D)
private val TaillightColor = Color(0xFFE53935)
private val CarShadow = Color(0x40000000)

/** How far the car lifts (as a fraction of its length) at the top of a hop. */
private const val HOP_LIFT = 0.07f

/** How much bigger the car looks at the top of a hop (it is closer to the camera). */
private const val HOP_GROW = 0.04f

/** How much lighter the shadow gets at the top of a hop. */
private const val HOP_SHADOW_FADE = 0.2f

/**
 * Draws a top-down car centred on [center], pointing up when [bearingDegrees] is 0.
 * [length] is the car's length in pixels; its width is half of that.
 * [hop] (0..1) lifts the car off the road while its shadow stays on the ground.
 */
internal fun DrawScope.drawTopDownCar(
    center: Offset,
    bearingDegrees: Float,
    body: Color,
    length: Float,
    hop: Float = 0f,
) {
    val width = length * 0.5f
    val topLeft = Offset(center.x - width / 2, center.y - length / 2)
    rotate(bearingDegrees, pivot = center) {
        val shadowOffset = Offset(length * 0.06f, length * 0.08f)
        val shadowColor = CarShadow.copy(alpha = CarShadow.alpha * (1f - HOP_SHADOW_FADE * hop))
        drawRoundRect(shadowColor, topLeft + shadowOffset, Size(width, length), CornerRadius(width * 0.4f))
    }
    val grow = 1f + HOP_GROW * hop
    withTransform({
        translate(top = -HOP_LIFT * length * hop)
        scale(grow, grow, pivot = center)
        rotate(bearingDegrees, pivot = center)
    }) {
        drawWheels(topLeft, width, length)
        drawRoundRect(body, topLeft, Size(width, length), CornerRadius(width * 0.4f))
        drawCabin(topLeft, width, length, body)
        drawLights(topLeft, width, length)
    }
}

private fun DrawScope.drawWheels(topLeft: Offset, width: Float, length: Float) {
    val wheelSize = Size(width * 0.16f, length * 0.2f)
    listOf(0.14f, 0.66f).forEach { y ->
        listOf(-wheelSize.width * 0.45f, width - wheelSize.width * 0.55f).forEach { x ->
            drawRoundRect(WheelColor, topLeft + Offset(x, length * y), wheelSize, CornerRadius(wheelSize.width / 2))
        }
    }
}

private fun DrawScope.drawCabin(topLeft: Offset, width: Float, length: Float, body: Color) {
    val inset = width * 0.14f
    val glassWidth = width - 2 * inset
    // Windshield (front = top), roof, rear window.
    drawRoundRect(WindowColor, topLeft + Offset(inset, length * 0.24f), Size(glassWidth, length * 0.17f), CornerRadius(width * 0.15f))
    drawRoundRect(lerp(body, Color.White, 0.25f), topLeft + Offset(inset, length * 0.43f), Size(glassWidth, length * 0.24f), CornerRadius(width * 0.12f))
    drawRoundRect(WindowColor, topLeft + Offset(inset, length * 0.69f), Size(glassWidth, length * 0.12f), CornerRadius(width * 0.12f))
}

private fun DrawScope.drawLights(topLeft: Offset, width: Float, length: Float) {
    val radius = width * 0.1f
    listOf(width * 0.25f, width * 0.75f).forEach { x ->
        drawCircle(HeadlightColor, radius, topLeft + Offset(x, length * 0.05f))
        drawCircle(TaillightColor, radius * 0.8f, topLeft + Offset(x, length * 0.96f))
    }
}
