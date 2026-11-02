package com.alifu.liatinmobil.ui.ride.map

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class CarHopTest {

    @Test
    fun `parked car stays on the ground`() {
        listOf(0L, 100L, HOP_PERIOD_MILLIS / 2, 9_999L).forEach { elapsed ->
            assertEquals(0f, carHopHeight(elapsed, speedRatio = 0f), EPS)
        }
    }

    @Test
    fun `car at cruise speed lands at the start of every hop`() {
        listOf(0L, HOP_PERIOD_MILLIS, 10 * HOP_PERIOD_MILLIS).forEach { elapsed ->
            assertEquals(0f, carHopHeight(elapsed, speedRatio = 1f), EPS)
        }
    }

    @Test
    fun `car at cruise speed reaches full height halfway through a hop`() {
        assertEquals(1f, carHopHeight(HOP_PERIOD_MILLIS / 2, speedRatio = 1f), 0.01f)
    }

    @Test
    fun `hop is smaller while the car speeds up or slows down`() {
        val top = HOP_PERIOD_MILLIS / 2
        assertEquals(0.5f, carHopHeight(top, speedRatio = 0.5f), 0.01f)
        assertTrue(carHopHeight(top, speedRatio = 0.2f) < carHopHeight(top, speedRatio = 0.8f))
    }

    @Test
    fun `hop moves gently with no sudden jumps between frames`() {
        // At 60 fps the height may only change a little per frame, including at landing.
        val frameMillis = 16L
        val heights = (0L..2 * HOP_PERIOD_MILLIS step frameMillis).map { carHopHeight(it, speedRatio = 1f) }
        heights.zipWithNext().forEach { (a, b) -> assertTrue("jump $a -> $b", abs(b - a) < MAX_STEP_PER_FRAME) }
    }

    @Test
    fun `landing is soft, the car slows down just before touching the road`() {
        val justBeforeLanding = carHopHeight(HOP_PERIOD_MILLIS - 16, speedRatio = 1f)
        assertTrue("height $justBeforeLanding", justBeforeLanding < 0.02f)
    }

    @Test
    fun `hop height always stays between ground and top`() {
        (0L..2_000L step 7).forEach { elapsed ->
            assertTrue(carHopHeight(elapsed, speedRatio = 1f) in 0f..1f)
        }
    }

    private companion object {
        const val EPS = 0.001f
        const val MAX_STEP_PER_FRAME = 0.1f
    }
}
