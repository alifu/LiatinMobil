package com.alifu.liatinmobil.data

import com.alifu.liatinmobil.domain.geometry.distance
import com.alifu.liatinmobil.domain.model.MapPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class LocalRouteRepositoryTest {

    private val routes = LocalRouteRepository.getRoutes()

    @Test
    fun `there are exactly ten routes with unique ids`() {
        assertEquals(10, routes.size)
        assertEquals(10, routes.map { it.id }.toSet().size)
    }

    @Test
    fun `every route has a distinct name`() {
        assertEquals(10, routes.map { it.name }.toSet().size)
    }

    @Test
    fun `findRoute returns the route or null`() {
        assertEquals(3, LocalRouteRepository.findRoute(3)?.id)
        assertNull(LocalRouteRepository.findRoute(99))
    }

    @Test
    fun `approach ends where the trip starts`() {
        routes.forEach { route ->
            assertEquals("route ${route.id}", route.approachPath.last(), route.tripPath.first())
        }
    }

    @Test
    fun `paths stay inside the map`() {
        routes.flatMap { it.fullPath }.forEach { point ->
            assertTrue("$point outside map", point.x in 0f..CityLayout.WORLD_SIZE)
            assertTrue("$point outside map", point.y in 0f..CityLayout.WORLD_SIZE)
        }
    }

    @Test
    fun `car never makes a u-turn at the pickup point`() {
        routes.forEach { route ->
            val beforePickup = route.approachPath[route.approachPath.size - 2]
            val afterPickup = route.tripPath[1]
            assertNotEquals("route ${route.id}", beforePickup, afterPickup)
        }
    }

    @Test
    fun `every segment runs along a road, the flyover or the roundabout`() {
        routes.forEach { route ->
            route.fullPath.zipWithNext().forEach { (a, b) ->
                assertTrue("route ${route.id}: $a -> $b is off-road", isOnRoad(a, b))
            }
        }
    }

    private fun isOnRoad(a: MapPoint, b: MapPoint): Boolean =
        isAlongGridRoad(a, b) || isOnFlyover(a, b) || isOnRoundabout(a, b)

    private fun isAlongGridRoad(a: MapPoint, b: MapPoint): Boolean {
        val vertical = a.x == b.x && isRoadLine(a.x)
        val horizontal = a.y == b.y && isRoadLine(a.y)
        return vertical || horizontal
    }

    private fun isRoadLine(value: Float): Boolean {
        val index = value / CityLayout.BLOCK_SIZE
        return index == index.toInt().toFloat() && index.toInt() in CityLayout.roadIndices
    }

    private fun isOnFlyover(a: MapPoint, b: MapPoint): Boolean =
        setOf(a, b) == setOf(CityLayout.flyoverStart, CityLayout.flyoverEnd)

    private fun isOnRoundabout(a: MapPoint, b: MapPoint): Boolean {
        val center = CityLayout.roundaboutCenter
        val radius = CityLayout.ROUNDABOUT_RADIUS
        return listOf(a, b).all { abs(distance(it, center) - radius) < 0.5f }
    }
}
