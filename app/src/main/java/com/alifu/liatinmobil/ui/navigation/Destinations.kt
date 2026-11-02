package com.alifu.liatinmobil.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
data object DashboardDestination

@Serializable
data class CarSelectionDestination(val routeId: Int)

@Serializable
data class RideDestination(val routeId: Int, val carId: String, val durationMinutes: Int)
