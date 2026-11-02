package com.alifu.liatinmobil.domain.simulation

import com.alifu.liatinmobil.domain.model.RidePhase

/** One step of the ride: either standing still or driving between two distances. */
internal sealed interface TimelineStep {
    val phase: RidePhase
    val startMillis: Long
    val durationMillis: Long
    val endMillis: Long get() = startMillis + durationMillis

    data class Wait(
        override val phase: RidePhase,
        override val startMillis: Long,
        override val durationMillis: Long,
        val atDistance: Float,
        val isRedLight: Boolean,
    ) : TimelineStep

    data class Drive(
        override val phase: RidePhase,
        override val startMillis: Long,
        override val durationMillis: Long,
        val fromDistance: Float,
        val toDistance: Float,
    ) : TimelineStep
}

/**
 * Fraction (0..1) of a drive covered after [elapsed] of [total] milliseconds, using a
 * trapezoid speed profile: speed ramps up for [rampMillis], cruises, then ramps down.
 * This is what makes the car glide into red lights and away from them.
 */
internal fun easedDriveFraction(elapsed: Long, total: Long, rampMillis: Long): Float {
    if (total <= 0) return 1f
    val t = elapsed.coerceIn(0, total).toDouble()
    val ramp = minOf(rampMillis.toDouble(), total / 2.0)
    val cruiseSpeed = 1.0 / (total - ramp)
    val fraction = when {
        ramp == 0.0 -> t / total
        t < ramp -> 0.5 * cruiseSpeed * t * t / ramp
        t <= total - ramp -> 0.5 * cruiseSpeed * ramp + cruiseSpeed * (t - ramp)
        else -> {
            val remaining = total - t
            1.0 - 0.5 * cruiseSpeed * remaining * remaining / ramp
        }
    }
    return fraction.toFloat().coerceIn(0f, 1f)
}
