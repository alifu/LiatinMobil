package com.alifu.liatinmobil.domain.simulation

import com.alifu.liatinmobil.domain.model.Landmark
import com.alifu.liatinmobil.domain.model.MapPoint
import com.alifu.liatinmobil.domain.model.RidePhase
import com.alifu.liatinmobil.domain.model.Route
import com.alifu.liatinmobil.domain.model.TrafficStop
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RideSimulatorTest {

    // Approach: 100 units east. Trip: 300 units south. Stop halfway through the trip.
    private val route = Route(
        id = 1,
        origin = Landmark("A", "🏠", MapPoint(0f, 0f)),
        destination = Landmark("B", "🏫", MapPoint(100f, 300f)),
        approachPath = listOf(MapPoint(0f, 0f), MapPoint(100f, 0f)),
        tripPath = listOf(MapPoint(100f, 0f), MapPoint(100f, 300f)),
        trafficStops = listOf(TrafficStop(tripFraction = 0.5f, waitMillis = STOP_MS)),
    )
    private val duration = 5 * 60_000L
    private val simulator = RideSimulator(route, duration)

    // Driving time is shared by distance: 100/400 to approach, 300/400 to trip.
    private val drivingMs = duration - RideSimulator.SEARCHING_MILLIS - RideSimulator.PICKUP_MILLIS - STOP_MS
    private val approachEnd = RideSimulator.SEARCHING_MILLIS + drivingMs / 4
    private val pickupEnd = approachEnd + RideSimulator.PICKUP_MILLIS
    private val stopStart = pickupEnd + drivingMs * 3 / 8

    @Test
    fun `total duration equals the chosen duration`() {
        assertEquals(duration, simulator.totalMillis)
    }

    @Test
    fun `starts searching for a driver at the approach start`() {
        val snapshot = simulator.snapshotAt(0)
        assertEquals(RidePhase.SEARCHING_DRIVER, snapshot.phase)
        assertEquals(MapPoint(0f, 0f), snapshot.position)
        assertEquals(0f, snapshot.tripProgress, EPS)
    }

    @Test
    fun `driver drives to pickup at constant cruise speed`() {
        val midApproach = (RideSimulator.SEARCHING_MILLIS + approachEnd) / 2
        val snapshot = simulator.snapshotAt(midApproach)
        assertEquals(RidePhase.DRIVER_COMING, snapshot.phase)
        assertEquals(50f, snapshot.position.x, 1f)
        assertEquals(90f, snapshot.bearingDegrees, EPS)
        assertEquals(approachEnd - midApproach, snapshot.millisToPickup)
    }

    @Test
    fun `car accelerates gently instead of jumping to full speed`() {
        val justStarted = simulator.snapshotAt(RideSimulator.SEARCHING_MILLIS + 500)
        val cruiseDistanceIn500ms = 100f / (approachEnd - RideSimulator.SEARCHING_MILLIS) * 500
        assertTrue(justStarted.position.x < cruiseDistanceIn500ms)
        assertTrue(justStarted.position.x > 0f)
    }

    @Test
    fun `waits at the pickup point`() {
        val snapshot = simulator.snapshotAt(approachEnd + 1_000)
        assertEquals(RidePhase.PICKING_UP, snapshot.phase)
        assertEquals(MapPoint(100f, 0f), snapshot.position)
        assertEquals(0L, snapshot.millisToPickup)
    }

    @Test
    fun `stops at a red light halfway through the trip`() {
        val snapshot = simulator.snapshotAt(stopStart + STOP_MS / 2)
        assertEquals(RidePhase.ON_TRIP, snapshot.phase)
        assertTrue(snapshot.isStoppedAtLight)
        assertEquals(150f, snapshot.position.y, 0.5f)
        assertEquals(0.5f, snapshot.tripProgress, 0.01f)
    }

    @Test
    fun `is moving again after the red light`() {
        val snapshot = simulator.snapshotAt(stopStart + STOP_MS + 10_000)
        assertEquals(RidePhase.ON_TRIP, snapshot.phase)
        assertFalse(snapshot.isStoppedAtLight)
        assertTrue(snapshot.position.y > 150f)
        assertEquals(180f, snapshot.bearingDegrees, EPS)
    }

    @Test
    fun `arrives at the destination when time is up`() {
        listOf(duration, duration + 60_000).forEach { elapsed ->
            val snapshot = simulator.snapshotAt(elapsed)
            assertEquals(RidePhase.ARRIVED, snapshot.phase)
            assertEquals(MapPoint(100f, 300f), snapshot.position)
            assertEquals(1f, snapshot.tripProgress, EPS)
            assertEquals(0L, snapshot.millisToArrival)
        }
    }

    @Test
    fun `car is moving only while driving`() {
        val midApproach = (RideSimulator.SEARCHING_MILLIS + approachEnd) / 2
        assertTrue(simulator.snapshotAt(midApproach).isMoving)
        assertTrue(simulator.snapshotAt(stopStart + STOP_MS + 10_000).isMoving)
        assertFalse(simulator.snapshotAt(0).isMoving)
        assertFalse(simulator.snapshotAt(approachEnd + 1_000).isMoving)
        assertFalse(simulator.snapshotAt(stopStart + STOP_MS / 2).isMoving)
        assertFalse(simulator.snapshotAt(duration).isMoving)
    }

    @Test
    fun `remaining time counts down to arrival`() {
        assertEquals(duration - 10_000, simulator.snapshotAt(10_000).millisToArrival)
    }

    @Test
    fun `travelled distance never goes backwards`() {
        val distances = (0..duration step 1_000).map { simulator.snapshotAt(it).travelledDistance }
        distances.zipWithNext().forEach { (a, b) -> assertTrue("$a > $b", b >= a) }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects durations too short for the fixed waiting time`() {
        RideSimulator(route, durationMillis = 10_000)
    }

    private companion object {
        const val STOP_MS = 6_000L
        const val EPS = 0.01f
    }
}
