package com.alifu.liatinmobil.domain.geometry

import com.alifu.liatinmobil.domain.model.MapPoint
import org.junit.Assert.assertEquals
import org.junit.Test

class PolylineTest {

    private val lShape = Polyline(
        listOf(MapPoint(0f, 0f), MapPoint(100f, 0f), MapPoint(100f, 100f)),
    )

    @Test
    fun `length is the sum of segment lengths`() {
        assertEquals(200f, lShape.length, EPS)
    }

    @Test
    fun `duplicate consecutive points are ignored`() {
        val polyline = Polyline(listOf(MapPoint(0f, 0f), MapPoint(0f, 0f), MapPoint(0f, 50f)))
        assertEquals(50f, polyline.length, EPS)
        assertEquals(MapPoint(0f, 25f), polyline.pointAt(25f))
    }

    @Test
    fun `pointAt interpolates by distance along the path`() {
        assertEquals(MapPoint(0f, 0f), lShape.pointAt(0f))
        assertEquals(MapPoint(50f, 0f), lShape.pointAt(50f))
        assertEquals(MapPoint(100f, 0f), lShape.pointAt(100f))
        assertEquals(MapPoint(100f, 50f), lShape.pointAt(150f))
        assertEquals(MapPoint(100f, 100f), lShape.pointAt(200f))
    }

    @Test
    fun `pointAt clamps distances outside the path`() {
        assertEquals(MapPoint(0f, 0f), lShape.pointAt(-10f))
        assertEquals(MapPoint(100f, 100f), lShape.pointAt(999f))
    }

    @Test
    fun `bearing follows screen compass where 0 is up and 90 is right`() {
        assertEquals(90f, bearingDegrees(MapPoint(0f, 0f), MapPoint(10f, 0f)), EPS)
        assertEquals(180f, bearingDegrees(MapPoint(0f, 0f), MapPoint(0f, 10f)), EPS)
        assertEquals(270f, bearingDegrees(MapPoint(0f, 0f), MapPoint(-10f, 0f)), EPS)
        assertEquals(0f, bearingDegrees(MapPoint(0f, 0f), MapPoint(0f, -10f)), EPS)
    }

    @Test
    fun `lerpAngle takes the shortest way around the circle`() {
        assertEquals(0f, lerpAngle(350f, 10f, 0.5f), EPS)
        assertEquals(355f, lerpAngle(350f, 10f, 0.25f), EPS)
        assertEquals(135f, lerpAngle(90f, 180f, 0.5f), EPS)
    }

    @Test
    fun `bearingAt is straight away from corners and blended at a corner`() {
        val polyline = lShape
        assertEquals(90f, polyline.bearingAt(20f, turnRadius = 10f), EPS)
        assertEquals(180f, polyline.bearingAt(180f, turnRadius = 10f), EPS)
        assertEquals(135f, polyline.bearingAt(100f, turnRadius = 10f), EPS)
        assertEquals(112.5f, polyline.bearingAt(95f, turnRadius = 10f), EPS)
        assertEquals(157.5f, polyline.bearingAt(105f, turnRadius = 10f), EPS)
    }

    @Test
    fun `slice returns the points between two distances`() {
        val slice = lShape.slice(50f, 150f)
        assertEquals(
            listOf(MapPoint(50f, 0f), MapPoint(100f, 0f), MapPoint(100f, 50f)),
            slice,
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `polyline needs at least two distinct points`() {
        Polyline(listOf(MapPoint(1f, 1f), MapPoint(1f, 1f)))
    }

    private companion object {
        const val EPS = 0.01f
    }
}
