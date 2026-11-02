package com.alifu.liatinmobil.domain.simulation

import com.alifu.liatinmobil.data.LocalRouteRepository
import com.alifu.liatinmobil.domain.model.RideDuration
import com.alifu.liatinmobil.domain.model.RidePhase
import org.junit.Assert.assertEquals
import org.junit.Test

class DummyRoutesSimulationTest {

    @Test
    fun `every dummy route can be simulated for every duration option`() {
        LocalRouteRepository.getRoutes().forEach { route ->
            RideDuration.optionsMinutes.forEach { minutes ->
                val millis = RideDuration.toMillis(minutes)
                val end = RideSimulator(route, millis).snapshotAt(millis)
                assertEquals(RidePhase.ARRIVED, end.phase)
                assertEquals(route.tripPath.last(), end.position)
            }
        }
    }
}
