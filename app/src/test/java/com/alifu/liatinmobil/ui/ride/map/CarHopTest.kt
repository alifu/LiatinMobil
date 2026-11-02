package com.alifu.liatinmobil.ui.ride.map

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CarHopTest {

    @Test
    fun `parked car stays on the ground`() {
        listOf(0L, 100L, 225L, 9_999L).forEach { elapsed ->
            assertEquals(0f, carHopHeight(elapsed, isMoving = false), EPS)
        }
    }

    @Test
    fun `moving car lands at the start of every hop`() {
        listOf(0L, HOP_PERIOD_MILLIS, 10 * HOP_PERIOD_MILLIS).forEach { elapsed ->
            assertEquals(0f, carHopHeight(elapsed, isMoving = true), EPS)
        }
    }

    @Test
    fun `moving car reaches full height halfway through a hop`() {
        assertEquals(1f, carHopHeight(HOP_PERIOD_MILLIS / 2, isMoving = true), 0.01f)
    }

    @Test
    fun `hop height always stays between ground and top`() {
        (0L..2_000L step 7).forEach { elapsed ->
            assertTrue(carHopHeight(elapsed, isMoving = true) in 0f..1f)
        }
    }

    private companion object {
        const val EPS = 0.001f
    }
}
