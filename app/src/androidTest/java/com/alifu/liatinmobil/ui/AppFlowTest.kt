package com.alifu.liatinmobil.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alifu.liatinmobil.ui.navigation.AppNavHost
import com.alifu.liatinmobil.ui.theme.LiatinMobilTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppFlowTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun dashboard_to_car_selection_to_ride_map() {
        composeRule.setContent { LiatinMobilTheme { AppNavHost() } }

        // Page 1: dashboard
        composeRule.onNodeWithText("Mau ke mana hari ini?").assertIsDisplayed()
        composeRule.onNodeWithTag("route_card_1").performClick()

        // Page 2: choose a car and a duration
        composeRule.onNodeWithText("Mau naik mobil apa?").assertIsDisplayed()
        selectInList("car_option_taksi")
        selectInList("duration_8")
        composeRule.onNodeWithTag("book_button").performClick()

        // Page 3: the ride animates forever, so drive the clock manually.
        composeRule.mainClock.autoAdvance = false
        composeRule.mainClock.advanceTimeBy(FRAME_STEP_MILLIS)
        composeRule.onNodeWithTag("ride_map").assertIsDisplayed()
        composeRule.onNodeWithText("Mencari driver terdekat…").assertIsDisplayed()
        composeRule.onNodeWithTag("pause_button").assertIsDisplayed()
    }

    private fun selectInList(tag: String) {
        composeRule.onNodeWithTag("car_selection_list").performScrollToNode(hasTestTag(tag))
        composeRule.onNodeWithTag(tag).performClick()
    }

    @Test
    fun back_from_car_selection_returns_to_dashboard() {
        composeRule.setContent { LiatinMobilTheme { AppNavHost() } }

        composeRule.onNodeWithTag("route_card_2").performClick()
        composeRule.onNodeWithText("Mau naik mobil apa?").assertIsDisplayed()
        composeRule.onNodeWithText("←").performClick()

        composeRule.onNodeWithText("Mau ke mana hari ini?").assertIsDisplayed()
    }

    private companion object {
        const val FRAME_STEP_MILLIS = 500L
    }
}
