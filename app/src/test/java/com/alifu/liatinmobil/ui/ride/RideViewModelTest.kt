package com.alifu.liatinmobil.ui.ride

import androidx.lifecycle.SavedStateHandle
import com.alifu.liatinmobil.data.CarCatalog
import com.alifu.liatinmobil.data.LocalRouteRepository
import com.alifu.liatinmobil.domain.model.RideDuration
import com.alifu.liatinmobil.domain.model.RidePhase
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
    fun `ride ends in the arrived phase and stays there`() {
        val vm = viewModel()
        repeat((duration / 100).toInt() + 10) { vm.onFrame(100) }
        assertEquals(RidePhase.ARRIVED, vm.state.value.snapshot.phase)
        assertEquals(duration, vm.state.value.elapsedMillis)
    }
}
