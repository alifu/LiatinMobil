package com.alifu.liatinmobil.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alifu.liatinmobil.data.CarCatalog
import com.alifu.liatinmobil.data.LocalRouteRepository
import com.alifu.liatinmobil.domain.model.RideDuration
import com.alifu.liatinmobil.domain.model.RidePhase
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
    fun turn_banner_appears_shortly_before_a_turn() {
        val turnAnnouncedAt = (0..duration step 250).first { simulator.snapshotAt(it).upcomingTurn != null }
        showRide { RideMapScreen(stateAt(turnAnnouncedAt, isPaused = true), {}, {}, {}, {}) }

        composeRule.onNodeWithTag("turn_banner").assertIsDisplayed()
        composeRule.onNodeWithText("Belok kanan").assertIsDisplayed()
    }

    @Test
    fun turn_banner_is_hidden_when_no_turn_is_close() {
        showRide { RideMapScreen(stateAt(0, isPaused = true), {}, {}, {}, {}) }

        composeRule.onNodeWithTag("turn_banner").assertDoesNotExist()
    }

    @Test
    fun mute_button_switches_the_sound_off_and_on() {
        showRide { RideMapScreen(stateAt(0, isPaused = true), {}, {}, {}, {}) }

        composeRule.onNodeWithContentDescription("Matikan suara").performClick()
        composeRule.mainClock.advanceTimeBy(FRAME_STEP_MILLIS)
        composeRule.onNodeWithContentDescription("Nyalakan suara").assertIsDisplayed().performClick()
        composeRule.mainClock.advanceTimeBy(FRAME_STEP_MILLIS)
        composeRule.onNodeWithContentDescription("Matikan suara").assertIsDisplayed()
    }

    @Test
    fun paused_ride_offers_resume() {
        val driverComingAt = (0..duration step 1_000).first {
            simulator.snapshotAt(it).phase == RidePhase.DRIVER_COMING
        }
        showRide { RideMapScreen(stateAt(driverComingAt, isPaused = true), {}, {}, {}, {}) }

        composeRule.onNodeWithText("▶ Lanjut").assertIsDisplayed()
        composeRule.onNodeWithText("Driver sedang menuju kamu").assertIsDisplayed()
    }

    private companion object {
        const val FRAME_STEP_MILLIS = 500L
    }
}
