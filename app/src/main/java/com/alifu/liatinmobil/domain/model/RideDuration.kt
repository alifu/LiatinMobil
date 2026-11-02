package com.alifu.liatinmobil.domain.model

/** Ride lengths a child can pick. Every option is at least five minutes long. */
object RideDuration {
    val optionsMinutes: List<Int> = listOf(5, 6, 8, 10)
    const val DEFAULT_MINUTES = 6

    fun isValid(minutes: Int): Boolean = minutes in optionsMinutes

    fun toMillis(minutes: Int): Long = minutes * 60_000L
}
