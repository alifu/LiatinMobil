package com.alifu.liatinmobil.ui.ride

import kotlin.math.ceil

private const val MILLIS_PER_MINUTE = 60_000.0

/** Whole minutes shown to the child, rounded up; null means "less than a minute". */
fun etaMinutes(millis: Long): Int? {
    if (millis < MILLIS_PER_MINUTE) return null
    return ceil(millis / MILLIS_PER_MINUTE).toInt()
}
