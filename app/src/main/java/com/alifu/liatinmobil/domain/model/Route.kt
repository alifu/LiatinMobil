package com.alifu.liatinmobil.domain.model

/** A named place on the map, drawn as an emoji label. */
data class Landmark(
    val name: String,
    val emoji: String,
    val position: MapPoint,
)

/** A traffic-light stop placed at a fraction (0..1) of the trip distance. */
data class TrafficStop(
    val tripFraction: Float,
    val waitMillis: Long,
) {
    init {
        require(tripFraction in 0f..1f) { "tripFraction must be within 0..1, was $tripFraction" }
        require(waitMillis > 0) { "waitMillis must be positive, was $waitMillis" }
    }
}

/**
 * A dummy ride. The driver first follows [approachPath] to the pickup point,
 * then follows [tripPath] from the pickup point to the destination.
 */
data class Route(
    val id: Int,
    val origin: Landmark,
    val destination: Landmark,
    val approachPath: List<MapPoint>,
    val tripPath: List<MapPoint>,
    val trafficStops: List<TrafficStop>,
) {
    val name: String get() = "${origin.name} → ${destination.name}"

    /** Approach + trip joined into one continuous path for the car. */
    val fullPath: List<MapPoint> get() = approachPath + tripPath.drop(1)
}
