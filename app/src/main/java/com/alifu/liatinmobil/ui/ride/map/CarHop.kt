package com.alifu.liatinmobil.ui.ride.map

import kotlin.math.PI
import kotlin.math.sin

/** One small hop every [HOP_PERIOD_MILLIS] while the car drives. */
internal const val HOP_PERIOD_MILLIS = 450L

/**
 * Height of the playful "jumping" car: 0 = on the road, 1 = top of the hop.
 * Uses half a sine wave per hop, so the car lifts softly and lands with a bounce.
 */
internal fun carHopHeight(elapsedMillis: Long, isMoving: Boolean): Float {
    if (!isMoving) return 0f
    val progress = Math.floorMod(elapsedMillis, HOP_PERIOD_MILLIS).toFloat() / HOP_PERIOD_MILLIS
    return sin(progress * PI).toFloat().coerceIn(0f, 1f)
}
