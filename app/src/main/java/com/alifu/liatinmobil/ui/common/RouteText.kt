package com.alifu.liatinmobil.ui.common

import com.alifu.liatinmobil.domain.geometry.Polyline
import com.alifu.liatinmobil.domain.model.Route
import java.util.Locale

/** One world unit is about two meters in the pretend city. */
const val METERS_PER_UNIT = 2f
private val IndonesianLocale: Locale = Locale.forLanguageTag("id-ID")

/** Pretend trip distance, e.g. "4,2 km". */
fun Route.distanceText(): String {
    val km = Polyline(tripPath).length * METERS_PER_UNIT / 1_000f
    return String.format(IndonesianLocale, "%.1f km", km)
}
