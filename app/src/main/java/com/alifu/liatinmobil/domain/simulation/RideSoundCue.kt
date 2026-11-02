package com.alifu.liatinmobil.domain.simulation

import com.alifu.liatinmobil.domain.model.RidePhase
import com.alifu.liatinmobil.domain.model.TurnDirection

/** Something the ride should say out loud. */
enum class RideSoundCue { TURN_LEFT, TURN_RIGHT, DRIVER_ARRIVED, RED_LIGHT, ARRIVED }

/**
 * What to say (if anything) after the ride moved from [previous] to [current].
 * Each cue fires once, on the frame where the change happens. If several happen
 * at the same moment, the most important one wins: arriving, pickup, red light, turn.
 */
fun soundCueFor(previous: RideSnapshot, current: RideSnapshot): RideSoundCue? = when {
    previous.phase != RidePhase.ARRIVED && current.phase == RidePhase.ARRIVED -> RideSoundCue.ARRIVED
    previous.phase != RidePhase.PICKING_UP && current.phase == RidePhase.PICKING_UP -> RideSoundCue.DRIVER_ARRIVED
    !previous.isStoppedAtLight && current.isStoppedAtLight -> RideSoundCue.RED_LIGHT
    else -> newTurnCue(previous.upcomingTurn, current.upcomingTurn)
}

private fun newTurnCue(previous: UpcomingTurn?, current: UpcomingTurn?): RideSoundCue? {
    if (current == null || current.index == previous?.index) return null
    return when (current.direction) {
        TurnDirection.LEFT -> RideSoundCue.TURN_LEFT
        TurnDirection.RIGHT -> RideSoundCue.TURN_RIGHT
    }
}
