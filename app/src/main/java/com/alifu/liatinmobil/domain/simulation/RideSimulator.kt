package com.alifu.liatinmobil.domain.simulation

import com.alifu.liatinmobil.domain.geometry.Polyline
import com.alifu.liatinmobil.domain.model.RidePhase
import com.alifu.liatinmobil.domain.model.Route

/**
 * Pure, deterministic ride simulation: given the elapsed time it returns where the
 * car is, which way it faces and what phase the ride is in. No clocks, no threads,
 * so it is trivial to test and to resume after a pause or screen rotation.
 */
class RideSimulator(private val route: Route, durationMillis: Long) {

    val totalMillis: Long = durationMillis
    private val path = Polyline(route.fullPath)
    private val pickupDistance = Polyline(route.approachPath).length
    private val tripLength = path.length - pickupDistance
    private val steps: List<TimelineStep>
    private val pickupReachedAtMillis: Long

    init {
        val fixedWaitMillis = SEARCHING_MILLIS + PICKUP_MILLIS + route.trafficStops.sumOf { it.waitMillis }
        require(durationMillis >= fixedWaitMillis + MIN_DRIVING_MILLIS) {
            "Duration $durationMillis ms is too short for this route"
        }
        steps = buildTimeline(drivingMillis = durationMillis - fixedWaitMillis)
        pickupReachedAtMillis = steps.first { it.phase == RidePhase.PICKING_UP }.startMillis
    }

    fun snapshotAt(elapsedMillis: Long): RideSnapshot {
        val elapsed = elapsedMillis.coerceAtLeast(0)
        val step = steps.firstOrNull { elapsed < it.endMillis }
            ?: return snapshot(path.length, RidePhase.ARRIVED, elapsed, isRedLight = false)
        return when (step) {
            is TimelineStep.Wait -> snapshot(step.atDistance, step.phase, elapsed, step.isRedLight)
            is TimelineStep.Drive -> {
                val fraction = easedDriveFraction(elapsed - step.startMillis, step.durationMillis, RAMP_MILLIS)
                val distance = step.fromDistance + (step.toDistance - step.fromDistance) * fraction
                snapshot(distance, step.phase, elapsed, isRedLight = false)
            }
        }
    }

    private fun snapshot(distance: Float, phase: RidePhase, elapsed: Long, isRedLight: Boolean) =
        RideSnapshot(
            position = path.pointAt(distance),
            bearingDegrees = path.bearingAt(distance, TURN_RADIUS),
            phase = phase,
            travelledDistance = distance,
            isStoppedAtLight = isRedLight,
            millisToPickup = (pickupReachedAtMillis - elapsed).coerceAtLeast(0),
            millisToArrival = (totalMillis - elapsed).coerceAtLeast(0),
            tripProgress = ((distance - pickupDistance) / tripLength).coerceIn(0f, 1f),
        )

    /** Driving time is shared by distance, so the car cruises at one steady speed. */
    private fun buildTimeline(drivingMillis: Long): List<TimelineStep> {
        val millisPerUnit = drivingMillis.toDouble() / path.length
        val steps = mutableListOf<TimelineStep>()
        var clock = 0L

        fun addWait(phase: RidePhase, millis: Long, at: Float, isRedLight: Boolean) {
            steps += TimelineStep.Wait(phase, clock, millis, at, isRedLight)
            clock += millis
        }

        fun addDrive(phase: RidePhase, from: Float, to: Float) {
            val millis = ((to - from) * millisPerUnit).toLong()
            steps += TimelineStep.Drive(phase, clock, millis, from, to)
            clock += millis
        }

        addWait(RidePhase.SEARCHING_DRIVER, SEARCHING_MILLIS, at = 0f, isRedLight = false)
        addDrive(RidePhase.DRIVER_COMING, 0f, pickupDistance)
        addWait(RidePhase.PICKING_UP, PICKUP_MILLIS, at = pickupDistance, isRedLight = false)

        var from = pickupDistance
        route.trafficStops.sortedBy { it.tripFraction }.forEach { stop ->
            val stopAt = pickupDistance + tripLength * stop.tripFraction
            addDrive(RidePhase.ON_TRIP, from, stopAt)
            addWait(RidePhase.ON_TRIP, stop.waitMillis, at = stopAt, isRedLight = true)
            from = stopAt
        }
        // The last drive absorbs rounding so the ride ends exactly on time.
        val lastMillis = totalMillis - clock
        steps += TimelineStep.Drive(RidePhase.ON_TRIP, clock, lastMillis, from, path.length)
        return steps.toList()
    }

    companion object {
        const val SEARCHING_MILLIS = 8_000L
        const val PICKUP_MILLIS = 6_000L
        private const val MIN_DRIVING_MILLIS = 30_000L
        private const val RAMP_MILLIS = 3_000L
        private const val TURN_RADIUS = 25f
    }
}
