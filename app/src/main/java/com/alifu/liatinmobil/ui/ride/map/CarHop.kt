package com.alifu.liatinmobil.ui.ride.map

import kotlin.math.PI
import kotlin.math.cos

/** One small hop every [HOP_PERIOD_MILLIS] while the car drives. */
internal const val HOP_PERIOD_MILLIS = 600L

/**
 * Height of the playful "bouncing" car: 0 = on the road, 1 = top of the hop.
 *
 * A full cosine wave per hop eases out at the top *and* eases in before landing,
 * so there is no sharp bounce at the road. The height is scaled by [speedRatio]
 * (0 = standing still, 1 = cruising), so hops grow gently as the car pulls away
 * and fade out as it brakes for a red light.
 */
internal fun carHopHeight(elapsedMillis: Long, speedRatio: Float): Float {
    val amplitude = speedRatio.coerceIn(0f, 1f)
    if (amplitude == 0f) return 0f
    val progress = Math.floorMod(elapsedMillis, HOP_PERIOD_MILLIS).toDouble() / HOP_PERIOD_MILLIS
    val wave = (1.0 - cos(2 * PI * progress)) / 2
    return (wave * amplitude).toFloat().coerceIn(0f, 1f)
}
