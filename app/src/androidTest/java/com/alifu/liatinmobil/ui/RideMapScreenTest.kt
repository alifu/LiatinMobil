package com.alifu.liatinmobil.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alifu.liatinmobil.data.CarCatalog
import com.alifu.liatinmobil.data.LocalRouteRepository
import com.alifu.liatinmobil.domain.model.RideDuration
import com.alifu.liatinmobil.domain.simulation.RideSimulator
import com.alifu.liatinmobil.ui.ride.RideMapScreen
import com.alifu.liatinmobil.ui.ride.RideUiState
import com.alifu.liatinmobil.ui.theme.LiatinMobilTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RideMapScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val route = LocalRouteRepository.getRoutes().first()
    private val duration = RideDuration.toMillis(5)
    private val simulator = RideSimulator(route, duration)

    private fun showRide(content: @Composable () -> Unit) {
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent { LiatinMobilTheme { content() } }
        composeRule.mainClock.advanceTimeBy(FRAME_STEP_MILLIS)
    }

    private fun stateAt(elapsed: Long, isPaused: Boolean = false) = RideUiState(
        route = route,
        car = CarCatalog.defaultCar,
        driver = CarCatalog.driverFor(route.id),
        snapshot = simulator.snapshotAt(elapsed),
        elapsedMillis = elapsed,
        isPaused = isPaused,
    )

    @Test
    fun arrived_ride_shows_play_again() {
        var playAgainClicked = false
        showRide {
            RideMapScreen(
                state = stateAt(duration, isPaused = true),
                onFrame = {},
                onTogglePause = {},
                onBack = {},
                onPlayAgain = { playAgainClicked = true },
            )
        }

        composeRule.onNodeWithText("Hore! Sudah sampai di Sekolah 🎉").assertIsDisplayed()
        composeRule.onNodeWithTag("play_again_button").performClick()
        assertTrue(playAgainClicked)
    }

    @Test
    fun paused_ride_offers_resume() {
        showRide { RideMapScreen(stateAt(60_000, isPaused = true), {}, {}, {}, {}) }

        composeRule.onNodeWithText("▶ Lanjut").assertIsDisplayed()
        composeRule.onNodeWithText("Driver sedang menuju kamu").assertIsDisplayed()
    }

    private companion object {
        const val FRAME_STEP_MILLIS = 500L
    }
}
