package com.alifu.liatinmobil.data

import com.alifu.liatinmobil.domain.model.CarType
import com.alifu.liatinmobil.domain.model.Driver

object CarCatalog {
    val cars: List<CarType> = listOf(
        CarType("hemat", "Mobil Hemat", "Mobil kecil yang lincah", 0xFF00B14F, stars = 1),
        CarType("nyaman", "Mobil Nyaman", "Sedan yang empuk dan tenang", 0xFF1E88E5, stars = 2),
        CarType("keluarga", "Mobil Keluarga", "Muat banyak, cocok untuk sekeluarga", 0xFF8E24AA, stars = 3),
        CarType("taksi", "Taksi", "Taksi kuning yang ceria", 0xFFFFC107, stars = 2),
        CarType("listrik", "Mobil Listrik", "Senyap dan ramah lingkungan", 0xFF00ACC1, stars = 3),
    )

    val defaultCar: CarType = cars.first()

    fun findCar(id: String): CarType? = cars.firstOrNull { it.id == id }

    private val drivers = listOf(
        Driver("Pak Budi", "B 1234 LTN", "👨"),
        Driver("Bu Sari", "B 5678 MBL", "👩"),
        Driver("Pak Joko", "D 2468 ASK", "🧔"),
        Driver("Bu Rina", "B 1357 KUY", "👩‍🦱"),
        Driver("Pak Dedi", "F 9090 YUK", "👴"),
    )

    /** Picks a stable pretend driver for a route so it looks the same every time. */
    fun driverFor(routeId: Int): Driver = drivers[Math.floorMod(routeId, drivers.size)]
}
