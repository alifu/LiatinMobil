package com.alifu.liatinmobil.domain.simulation

import com.alifu.liatinmobil.domain.model.MapPoint
import com.alifu.liatinmobil.domain.model.RidePhase

/** Everything the UI needs to draw one frame of the ride. */
data class RideSnapshot(
    val position: MapPoint,
    val bearingDegrees: Float,
    val phase: RidePhase,
    /** Distance along the full path (approach + trip). */
    val travelledDistance: Float,
    val isStoppedAtLight: Boolean,
    /** True while the car is driving (not searching, picking up, at a red light or arrived). */
    val isMoving: Boolean,
    /** Current speed relative to cruise speed: 0 when standing still, 1 when cruising. */
    val speedRatio: Float,
    val millisToPickup: Long,
    val millisToArrival: Long,
    /** 0..1 progress of the trip part only (after pickup). */
    val tripProgress: Float,
)
