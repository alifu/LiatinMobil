package com.alifu.liatinmobil.domain.simulation

import com.alifu.liatinmobil.domain.model.MapPoint
import com.alifu.liatinmobil.domain.model.RidePhase
import com.alifu.liatinmobil.domain.model.TurnDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RideSoundCueTest {

    private fun snapshot(
        phase: RidePhase = RidePhase.ON_TRIP,
        isStoppedAtLight: Boolean = false,
        turn: UpcomingTurn? = null,
    ) = RideSnapshot(
        position = MapPoint(0f, 0f),
        bearingDegrees = 0f,
        phase = phase,
        travelledDistance = 0f,
        isStoppedAtLight = isStoppedAtLight,
        isMoving = false,
        speedRatio = 0f,
        millisToPickup = 0,
        millisToArrival = 0,
        tripProgress = 0f,
        upcomingTurn = turn,
    )

    private fun turn(index: Int, direction: TurnDirection) = UpcomingTurn(index, direction, 100f)

    @Test
    fun `a newly announced left turn says turn left`() {
        val cue = soundCueFor(snapshot(), snapshot(turn = turn(0, TurnDirection.LEFT)))
        assertEquals(RideSoundCue.TURN_LEFT, cue)
    }

    @Test
    fun `a newly announced right turn says turn right`() {
        val cue = soundCueFor(snapshot(), snapshot(turn = turn(0, TurnDirection.RIGHT)))
        assertEquals(RideSoundCue.TURN_RIGHT, cue)
    }

    @Test
    fun `the same turn is announced only once`() {
        val announced = snapshot(turn = turn(0, TurnDirection.LEFT))
        assertNull(soundCueFor(announced, announced.copy(travelledDistance = 20f)))
    }

    @Test
    fun `the next turn is announced again`() {
        val first = snapshot(turn = turn(0, TurnDirection.LEFT))
        val second = snapshot(turn = turn(1, TurnDirection.RIGHT))
        assertEquals(RideSoundCue.TURN_RIGHT, soundCueFor(first, second))
    }

    @Test
    fun `reaching the pickup point says the driver has arrived`() {
        val cue = soundCueFor(snapshot(RidePhase.DRIVER_COMING), snapshot(RidePhase.PICKING_UP))
        assertEquals(RideSoundCue.DRIVER_ARRIVED, cue)
        assertNull(soundCueFor(snapshot(RidePhase.PICKING_UP), snapshot(RidePhase.PICKING_UP)))
    }

    @Test
    fun `stopping at a red light says red light once`() {
        val moving = snapshot()
        val stopped = snapshot(isStoppedAtLight = true)
        assertEquals(RideSoundCue.RED_LIGHT, soundCueFor(moving, stopped))
        assertNull(soundCueFor(stopped, stopped))
    }

    @Test
    fun `arriving says hooray`() {
        assertEquals(RideSoundCue.ARRIVED, soundCueFor(snapshot(), snapshot(RidePhase.ARRIVED)))
        assertNull(soundCueFor(snapshot(RidePhase.ARRIVED), snapshot(RidePhase.ARRIVED)))
    }

    @Test
    fun `arrival wins over a turn announced in the same frame`() {
        val cue = soundCueFor(snapshot(), snapshot(RidePhase.ARRIVED, turn = turn(0, TurnDirection.LEFT)))
        assertEquals(RideSoundCue.ARRIVED, cue)
    }

    @Test
    fun `nothing is said when nothing happens`() {
        assertNull(soundCueFor(snapshot(), snapshot()))
    }
}
