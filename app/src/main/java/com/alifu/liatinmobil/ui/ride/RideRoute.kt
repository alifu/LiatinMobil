package com.alifu.liatinmobil.ui.ride

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alifu.liatinmobil.data.CarCatalog
import com.alifu.liatinmobil.data.RouteRepository
import com.alifu.liatinmobil.domain.model.RideDuration
import com.alifu.liatinmobil.ui.common.MissingContentScreen

/** Validates navigation arguments, then wires the ride screen to its ViewModel. */
@Composable
fun RideRoute(
    routeRepository: RouteRepository,
    routeId: Int,
    carId: String,
    durationMinutes: Int,
    onBack: () -> Unit,
    onPlayAgain: () -> Unit,
) {
    val route = routeRepository.findRoute(routeId)
    val car = CarCatalog.findCar(carId)
    if (route == null || car == null || !RideDuration.isValid(durationMinutes)) {
        MissingContentScreen(onBack = onBack)
        return
    }
    val viewModel = viewModel {
        RideViewModel(createSavedStateHandle(), route, car, RideDuration.toMillis(durationMinutes))
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    RideMapScreen(
        state = state,
        onFrame = viewModel::onFrame,
        onTogglePause = viewModel::togglePause,
        onBack = onBack,
        onPlayAgain = onPlayAgain,
        soundCues = viewModel.soundCues,
    )
}
