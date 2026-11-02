package com.alifu.liatinmobil.domain.simulation

import com.alifu.liatinmobil.domain.model.Landmark
import com.alifu.liatinmobil.domain.model.MapPoint
import com.alifu.liatinmobil.domain.model.RidePhase
import com.alifu.liatinmobil.domain.model.Route
import com.alifu.liatinmobil.domain.model.TurnDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RideSimulatorTurnTest {

    // Approach: 400 east. Then the trip turns right (south, 300), then left (east, 300).
    private val route = Route(
        id = 1,
        origin = Landmark("A", "🏠", MapPoint(0f, 0f)),
        destination = Landmark("B", "🏫", MapPoint(700f, 300f)),
        approachPath = listOf(MapPoint(0f, 0f), MapPoint(400f, 0f)),
        tripPath = listOf(MapPoint(400f, 0f), MapPoint(400f, 300f), MapPoint(700f, 300f)),
        trafficStops = emptyList(),
    )
    private val duration = 5 * 60_000L
    private val simulator = RideSimulator(route, duration)
    private val snapshots = (0..duration step 250).map { simulator.snapshotAt(it) }

    @Test
    fun `no turn is announced while the corner is still far away`() {
        val justStarted = simulator.snapshotAt(RideSimulator.SEARCHING_MILLIS + 1_000)
        assertEquals(RidePhase.DRIVER_COMING, justStarted.phase)
        assertNull(justStarted.upcomingTurn)
    }

    @Test
    fun `the right turn is announced shortly before the corner`() {
        val announced = snapshots.filter { it.upcomingTurn?.index == 0 }
        assertTrue(announced.isNotEmpty())
        announced.forEach { snapshot ->
            assertEquals(TurnDirection.RIGHT, snapshot.upcomingTurn?.direction)
            assertTrue(snapshot.upcomingTurn!!.distanceAhead in 0f..RideSimulator.TURN_ANNOUNCE_DISTANCE)
        }
    }

    @Test
    fun `the left turn on the trip is announced after the right turn`() {
        val turns = snapshots.mapNotNull { it.upcomingTurn }
        assertEquals(listOf(0, 1), turns.map { it.index }.distinct())
        assertEquals(TurnDirection.LEFT, turns.last().direction)
    }

    @Test
    fun `turn guidance only runs while the car is on its way`() {
        snapshots
            .filter { it.phase !in listOf(RidePhase.DRIVER_COMING, RidePhase.ON_TRIP) }
            .forEach { assertNull("phase ${it.phase}", it.upcomingTurn) }
    }

    @Test
    fun `distance ahead shrinks as the car approaches the corner`() {
        val distances = snapshots.mapNotNull { it.upcomingTurn }.filter { it.index == 0 }.map { it.distanceAhead }
        distances.zipWithNext().forEach { (a, b) -> assertTrue("$a < $b", b <= a) }
    }
}
