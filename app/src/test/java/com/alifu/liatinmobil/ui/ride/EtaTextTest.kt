package com.alifu.liatinmobil.ui.ride

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EtaTextTest {

    @Test
    fun `less than a minute has no minute count`() {
        assertNull(etaMinutes(0))
        assertNull(etaMinutes(59_999))
    }

    @Test
    fun `minutes are rounded up`() {
        assertEquals(1, etaMinutes(60_000))
        assertEquals(2, etaMinutes(60_001))
        assertEquals(6, etaMinutes(6 * 60_000))
    }
}
