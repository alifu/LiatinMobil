package com.alifu.liatinmobil.data

import com.alifu.liatinmobil.data.CityLayout.roundaboutArc
import com.alifu.liatinmobil.domain.model.Landmark
import com.alifu.liatinmobil.domain.model.MapPoint
import com.alifu.liatinmobil.domain.model.Route
import com.alifu.liatinmobil.domain.model.TrafficStop

/**
 * Ten hand-made dummy routes. Every segment runs along a road of [CityLayout]
 * (checked by LocalRouteRepositoryTest), so the car never drives through houses.
 */
object LocalRouteRepository : RouteRepository {

    private const val RED_LIGHT_MILLIS = 6_000L

    private val routes: List<Route> = listOf(
        route(
            id = 1, from = Landmarks.HOME, to = Landmarks.SCHOOL,
            approach = path(300 to 900, 300 to 600, 450 to 600),
            trip = path(
                450 to 600, 600 to 600, 600 to 1500, 1200 to 1500, 1200 to 900,
                900 to 900, 900 to 300, 1500 to 300, 1500 to 600, 1350 to 600,
            ),
            stops = listOf(0.3f, 0.7f),
        ),
        route(
            id = 2, from = Landmarks.HOME, to = Landmarks.PLAYGROUND,
            approach = path(300 to 900, 300 to 300, 600 to 300, 600 to 450),
            trip = path(
                600 to 450, 600 to 1500, 1200 to 1500, 1200 to 1200, 1800 to 1200,
                1800 to 300, 900 to 300, 900 to 750,
            ),
            stops = listOf(0.3f, 0.7f),
        ),
        route(
            id = 3, from = Landmarks.SCHOOL, to = Landmarks.ZOO,
            approach = path(1200 to 900, 1200 to 600, 1350 to 600),
            trip = path(
                1350 to 600, 2700 to 600, 2700 to 1500, 2100 to 1500, 2100 to 1800,
                900 to 1800, 900 to 2100, 2100 to 2100, 2100 to 2700, 2400 to 2700,
                2400 to 2400, 2550 to 2400,
            ),
            stops = listOf(0.25f, 0.6f),
        ),
        route(
            id = 4, from = Landmarks.HOME, to = Landmarks.GRANDMA,
            approach = path(300 to 300, 300 to 600, 450 to 600),
            trip = path(
                450 to 600, 900 to 600, 900 to 900, 300 to 900, 300 to 1200,
                900 to 1200, 900 to 1500, 300 to 1500, 300 to 1800, 900 to 1800,
                900 to 2100, 300 to 2100, 300 to 2700, 600 to 2700, 600 to 2400,
                450 to 2400,
            ),
            stops = listOf(0.35f, 0.75f),
        ),
        route(
            id = 5, from = Landmarks.MALL, to = Landmarks.AIRPORT,
            approach = path(900 to 2100, 900 to 1200, 1350 to 1200),
            trip = path(
                1350 to 1200, 1500 to 1200, 1500 to 900, 600 to 900, 600 to 300,
                2700 to 300, 2700 to 600, 2550 to 600,
            ),
            stops = listOf(0.35f, 0.7f),
        ),
        route(
            id = 6, from = Landmarks.STATION, to = Landmarks.BEACH,
            approach = path(2700 to 900, 2700 to 600, 1800 to 600, 1800 to 1050),
            trip = path(1800 to 1050, 1800 to 1200, 1500 to 1200) +
                roundaboutArc(fromDegrees = -90, toDegrees = 90) +
                path(
                    1500 to 2400, 900 to 2400, 900 to 2700, 1800 to 2700, 2700 to 1800,
                    2700 to 2100, 2550 to 2100,
                ),
            stops = listOf(0.2f, 0.8f),
        ),
        route(
            id = 7, from = Landmarks.HOME, to = Landmarks.HOSPITAL,
            approach = path(300 to 1200, 300 to 300, 450 to 300),
            trip = path(
                450 to 300, 1200 to 300, 1200 to 900, 2400 to 900, 2400 to 1500,
                2100 to 1500, 2100 to 2400, 1200 to 2400, 1200 to 2100, 1050 to 2100,
            ),
            stops = listOf(0.4f, 0.7f),
        ),
        route(
            id = 8, from = Landmarks.MARKET, to = Landmarks.MOSQUE,
            approach = path(2400 to 2100, 2100 to 2100, 2100 to 2400, 1950 to 2400),
            trip = path(
                1950 to 2400, 1800 to 2400, 1800 to 2700, 1200 to 2700, 1200 to 2400,
                900 to 2400, 900 to 2100, 300 to 2100, 300 to 1500, 1200 to 1500,
                1200 to 1200, 900 to 1200, 900 to 1350,
            ),
            stops = listOf(0.3f, 0.65f),
        ),
        route(
            id = 9, from = Landmarks.LIBRARY, to = Landmarks.POOL,
            approach = path(2700 to 300, 2700 to 900, 2250 to 900),
            trip = path(
                2250 to 900, 2100 to 900, 2100 to 1200, 1800 to 1200, 1800 to 1500,
                2700 to 1500, 2700 to 1200, 2400 to 1200, 2400 to 300, 1200 to 300,
                1200 to 900, 1500 to 900, 1500 to 600, 1650 to 600,
            ),
            stops = listOf(0.35f, 0.7f),
        ),
        route(
            id = 10, from = Landmarks.FRIEND_HOUSE, to = Landmarks.ICE_CREAM,
            approach = path(1200 to 1500, 1200 to 2400, 1350 to 2400),
            trip = path(
                1350 to 2400, 1500 to 2400, 1500 to 2100, 600 to 2100, 600 to 2700,
                1800 to 2700, 2700 to 1800, 2700 to 1500, 2550 to 1500,
            ),
            stops = listOf(0.25f, 0.8f),
        ),
    )

    override fun getRoutes(): List<Route> = routes

    override fun findRoute(id: Int): Route? = routes.firstOrNull { it.id == id }

    private fun route(
        id: Int,
        from: Landmark,
        to: Landmark,
        approach: List<MapPoint>,
        trip: List<MapPoint>,
        stops: List<Float>,
    ) = Route(
        id = id,
        origin = from,
        destination = to,
        approachPath = approach,
        tripPath = trip,
        trafficStops = stops.map { TrafficStop(tripFraction = it, waitMillis = RED_LIGHT_MILLIS) },
    )

    private fun path(vararg points: Pair<Int, Int>): List<MapPoint> =
        points.map { (x, y) -> MapPoint(x.toFloat(), y.toFloat()) }
}
