package com.alifu.liatinmobil.ui.ride

import androidx.lifecycle.SavedStateHandle
import com.alifu.liatinmobil.data.CarCatalog
import com.alifu.liatinmobil.data.LocalRouteRepository
import com.alifu.liatinmobil.domain.model.RideDuration
import com.alifu.liatinmobil.domain.model.RidePhase
import com.alifu.liatinmobil.domain.simulation.RideSoundCue
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RideViewModelTest {

    private val route = LocalRouteRepository.getRoutes().first()
    private val duration = RideDuration.toMillis(5)

    private fun viewModel(handle: SavedStateHandle = SavedStateHandle()) =
        RideViewModel(handle, route, CarCatalog.defaultCar, duration)

    @Test
    fun `frames advance the elapsed time`() {
        val vm = viewModel()
        vm.onFrame(16)
        vm.onFrame(16)
        assertEquals(32L, vm.state.value.elapsedMillis)
    }

    @Test
    fun `a huge frame gap is clamped so the car does not teleport`() {
        val vm = viewModel()
        vm.onFrame(10_000)
        assertEquals(100L, vm.state.value.elapsedMillis)
    }

    @Test
    fun `paused ride does not move`() {
        val vm = viewModel()
        vm.togglePause()
        vm.onFrame(16)
        assertTrue(vm.state.value.isPaused)
        assertEquals(0L, vm.state.value.elapsedMillis)
        vm.togglePause()
        vm.onFrame(16)
        assertEquals(16L, vm.state.value.elapsedMillis)
    }

    @Test
    fun `progress is restored from saved state`() {
        val handle = SavedStateHandle()
        val first = viewModel(handle)
        repeat(50) { first.onFrame(100) }
        val restored = viewModel(handle)
        assertEquals(5_000L, restored.state.value.elapsedMillis)
    }

    @Test
    fun `view model emits sound cues as the ride progresses`() {
        val vm = viewModel()
        val cues = mutableListOf<RideSoundCue>()
        val job = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Unconfined).launch {
            vm.soundCues.collect { cues += it }
        }
        repeat((duration / 100).toInt() + 10) { vm.onFrame(100) }
        job.cancel()

        assertTrue("turn cue missing: $cues", cues.any { it == RideSoundCue.TURN_LEFT || it == RideSoundCue.TURN_RIGHT })
        assertTrue("pickup cue missing: $cues", RideSoundCue.DRIVER_ARRIVED in cues)
        assertTrue("red light cue missing: $cues", RideSoundCue.RED_LIGHT in cues)
        assertEquals(RideSoundCue.ARRIVED, cues.last())
    }

    @Test
    fun `ride ends in the arrived phase and stays there`() {
        val vm = viewModel()
        repeat((duration / 100).toInt() + 10) { vm.onFrame(100) }
        assertEquals(RidePhase.ARRIVED, vm.state.value.snapshot.phase)
        assertEquals(duration, vm.state.value.elapsedMillis)
    }
}
