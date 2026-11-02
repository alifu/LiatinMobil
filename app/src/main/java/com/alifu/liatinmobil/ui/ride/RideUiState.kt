package com.alifu.liatinmobil.ui.ride

import com.alifu.liatinmobil.domain.model.CarType
import com.alifu.liatinmobil.domain.model.Driver
import com.alifu.liatinmobil.domain.model.Route
import com.alifu.liatinmobil.domain.simulation.RideSnapshot

data class RideUiState(
    val route: Route,
    val car: CarType,
    val driver: Driver,
    val snapshot: RideSnapshot,
    val elapsedMillis: Long,
    val isPaused: Boolean,
)
