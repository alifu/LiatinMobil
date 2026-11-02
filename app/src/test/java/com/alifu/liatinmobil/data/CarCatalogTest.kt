package com.alifu.liatinmobil.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CarCatalogTest {

    @Test
    fun `car ids are unique`() {
        assertEquals(CarCatalog.cars.size, CarCatalog.cars.map { it.id }.toSet().size)
    }

    @Test
    fun `findCar returns the car or null`() {
        assertEquals("taksi", CarCatalog.findCar("taksi")?.id)
        assertNull(CarCatalog.findCar("roket"))
    }

    @Test
    fun `driverFor is stable for the same route`() {
        assertEquals(CarCatalog.driverFor(4), CarCatalog.driverFor(4))
    }
}
