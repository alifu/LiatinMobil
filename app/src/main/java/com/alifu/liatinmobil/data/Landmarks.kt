package com.alifu.liatinmobil.data

import com.alifu.liatinmobil.data.CityLayout.blockCenter
import com.alifu.liatinmobil.domain.model.Landmark

/** Every named place in the city. Positions are block centers. */
object Landmarks {
    val HOME = Landmark("Rumah", "🏠", blockCenter(1, 1))
    val SCHOOL = Landmark("Sekolah", "🏫", blockCenter(4, 1))
    val PLAYGROUND = Landmark("Taman Bermain", "🎠", blockCenter(2, 2))
    val ZOO = Landmark("Kebun Binatang", "🦁", blockCenter(8, 8))
    val GRANDMA = Landmark("Rumah Nenek", "👵", blockCenter(1, 8))
    val MALL = Landmark("Mall", "🛍️", blockCenter(4, 4))
    val AIRPORT = Landmark("Bandara", "✈️", blockCenter(8, 1))
    val STATION = Landmark("Stasiun", "🚉", blockCenter(6, 3))
    val BEACH = Landmark("Pantai", "🏖️", blockCenter(8, 7))
    val HOSPITAL = Landmark("Rumah Sakit", "🏥", blockCenter(3, 7))
    val MARKET = Landmark("Pasar", "🧺", blockCenter(6, 8))
    val MOSQUE = Landmark("Masjid Raya", "🕌", blockCenter(2, 4))
    val LIBRARY = Landmark("Perpustakaan", "📚", blockCenter(7, 2))
    val POOL = Landmark("Kolam Renang", "🏊", blockCenter(5, 2))
    val FRIEND_HOUSE = Landmark("Rumah Teman", "🏡", blockCenter(4, 8))
    val ICE_CREAM = Landmark("Toko Es Krim", "🍦", blockCenter(8, 4))

    val all = listOf(
        HOME, SCHOOL, PLAYGROUND, ZOO, GRANDMA, MALL, AIRPORT, STATION,
        BEACH, HOSPITAL, MARKET, MOSQUE, LIBRARY, POOL, FRIEND_HOUSE, ICE_CREAM,
    )
}
