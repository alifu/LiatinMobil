package com.alifu.liatinmobil.ui.ride

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.alifu.liatinmobil.data.CarCatalog
import com.alifu.liatinmobil.domain.model.CarType
import com.alifu.liatinmobil.domain.model.RidePhase
import com.alifu.liatinmobil.domain.model.Route
import com.alifu.liatinmobil.domain.simulation.RideSimulator
import com.alifu.liatinmobil.domain.simulation.RideSoundCue
import com.alifu.liatinmobil.domain.simulation.soundCueFor
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

/**
 * Owns the ride clock. The screen feeds it frame deltas ([onFrame]); the elapsed
 * time lives in [SavedStateHandle] so the ride survives rotation and process death.
 */
class RideViewModel(
    private val savedStateHandle: SavedStateHandle,
    route: Route,
    car: CarType,
    durationMillis: Long,
) : ViewModel() {

    private val simulator = RideSimulator(route, durationMillis)

    private val _state = MutableStateFlow(
        initialState(route, car, savedStateHandle[KEY_ELAPSED] ?: 0L, savedStateHandle[KEY_PAUSED] ?: false),
    )
    val state: StateFlow<RideUiState> = _state.asStateFlow()

    private val _soundCues = Channel<RideSoundCue>(capacity = SOUND_CUE_BUFFER, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    /** Things to say out loud (turns, pickup, red light, arrival), each emitted once. */
    val soundCues: Flow<RideSoundCue> = _soundCues.receiveAsFlow()

    fun onFrame(deltaMillis: Long) {
        val current = _state.value
        if (current.isPaused || current.snapshot.phase == RidePhase.ARRIVED || deltaMillis <= 0) return
        val elapsed = (current.elapsedMillis + deltaMillis.coerceAtMost(MAX_FRAME_MILLIS))
            .coerceAtMost(simulator.totalMillis)
        val snapshot = simulator.snapshotAt(elapsed)
        savedStateHandle[KEY_ELAPSED] = elapsed
        _state.value = current.copy(elapsedMillis = elapsed, snapshot = snapshot)
        soundCueFor(current.snapshot, snapshot)?.let { _soundCues.trySend(it) }
    }

    fun togglePause() {
        _state.update { it.copy(isPaused = !it.isPaused) }
        savedStateHandle[KEY_PAUSED] = _state.value.isPaused
    }

    private fun initialState(route: Route, car: CarType, elapsed: Long, isPaused: Boolean) = RideUiState(
        route = route,
        car = car,
        driver = CarCatalog.driverFor(route.id),
        snapshot = simulator.snapshotAt(elapsed),
        elapsedMillis = elapsed,
        isPaused = isPaused,
    )

    private companion object {
        const val KEY_ELAPSED = "ride_elapsed_millis"
        const val KEY_PAUSED = "ride_paused"
        const val SOUND_CUE_BUFFER = 4

        /** A long frame (e.g. app returning from background) must not teleport the car. */
        const val MAX_FRAME_MILLIS = 100L
    }
}
