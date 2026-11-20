package com.example

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.example.model.AngleMode
import com.example.model.AppSettings
import com.example.model.HistoryEntry
import com.example.model.ThemeMode
import com.example.navigation.AppRoutes
import com.example.state.OmniCalcViewModel
import com.example.ui.components.LocalButtonSoundPlayer
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.CalculatorScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.OmniCalcTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h915dp")
class Phase6UiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `history screen filters searches deletes reuses and clears entries with confirmation`() {
        val viewModel = OmniCalcViewModel()
        viewModel.addHistoryEntry("2+3", "5", "Standard", AppRoutes.STANDARD_CALCULATOR)
        viewModel.addHistoryEntry("₹1,000 + 18% GST", "₹1,180", "GST", AppRoutes.GST_CALCULATOR)

        var reusedEntry: HistoryEntry? = null
        composeTestRule.setContent {
            OmniCalcTheme {
                HistoryScreen(
                    viewModel = viewModel,
                    onReuseEntry = { reusedEntry = it },
                )
            }
        }
        composeTestRule.waitForIdle()

        // Filter by GST tool chip
        composeTestRule.onNodeWithTag("history_filter_GST").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("₹1,000 + 18% GST").assertIsDisplayed()
        composeTestRule.onNodeWithText("2+3").assertDoesNotExist()

        // Switch back to All and search
        composeTestRule.onNodeWithTag("history_filter_All").performClick()
        composeTestRule.onNodeWithTag("history_search_input").performTextInput("2+3")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("= 5").assertIsDisplayed()
        composeTestRule.onNodeWithText("₹1,000 + 18% GST").assertDoesNotExist()

        // Clear search and test Reuse + Delete + Clear All confirmation
        composeTestRule.onNodeWithTag("history_search_input").performClick()
        val stdId = viewModel.history.value.first { it.expression == "2+3" }.id
        composeTestRule.onNodeWithTag("history_reuse_$stdId").performClick()
        assertEquals("2+3", reusedEntry?.expression)

        composeTestRule.onNodeWithTag("history_delete_$stdId").performClick()
        composeTestRule.waitForIdle()
        assertEquals(1, viewModel.history.value.size)

        // Clear All via confirmation dialog
        composeTestRule.onNodeWithTag("history_clear_all_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Clear All").performClick()
        composeTestRule.waitForIdle()
        assertEquals(0, viewModel.history.value.size)
        composeTestRule.onNodeWithText("No calculations yet").assertIsDisplayed()
    }

    @Test
    fun `button sound haptic feedback angle mode and decimal precision settings update calculator live`() {
        val viewModel = OmniCalcViewModel()
        var soundCount = 0
        var hapticCount = 0
        val fakeHaptic = object : HapticFeedback {
            override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) {
                hapticCount++
            }
        }

        composeTestRule.setContent {
            OmniCalcTheme {
                CompositionLocalProvider(
                    LocalHapticFeedback provides fakeHaptic,
                    LocalButtonSoundPlayer provides { soundCount++ },
                ) {
                    CalculatorScreen(
                        viewModel = viewModel,
                        initialScientific = true,
                        onBack = null,
                        onOpenHistory = {},
                    )
                }
            }
        }

        // Default: hapticFeedback = true, buttonSound = false
        composeTestRule.onNodeWithTag("sci_key_sin").performClick()
        composeTestRule.waitForIdle()
        assertEquals(1, hapticCount)
        assertEquals(0, soundCount)

        // Enable buttonSound, disable hapticFeedback -> next taps play sound & skip haptic
        viewModel.setButtonSound(true)
        viewModel.setHapticFeedback(false)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("key_9").performClick()
        composeTestRule.onNodeWithTag("key_0").performClick()
        composeTestRule.onNodeWithTag("key_( )").performClick()
        composeTestRule.waitForIdle()

        assertEquals(1, hapticCount)
        assertEquals(3, soundCount)
        // DEG: sin(90) = 1
        composeTestRule.onNodeWithTag("calculator_result_display").assertTextEquals("1")

        // Change AngleMode to RAD and DecimalPrecision to 3 from Settings -> live preview updates!
        viewModel.setDecimalPrecision(3)
        viewModel.setAngleMode(AngleMode.RAD)
        composeTestRule.waitForIdle()
        // sin(90 rad) = 0.89399666... -> 0.894 at precision 3
        composeTestRule.onNodeWithTag("calculator_result_display").assertTextEquals("0.894")
    }

    @Test
    fun `settings screen reset confirmation restores defaults and privacy policy discloses local and network behavior`() {
        val viewModel = OmniCalcViewModel()
        viewModel.setThemeMode(ThemeMode.LIGHT)
        viewModel.setCurrencyCode("USD")
        viewModel.setDecimalPrecision(2)
        viewModel.setAngleMode(AngleMode.RAD)
        viewModel.setButtonSound(true)
        viewModel.setHapticFeedback(false)

        composeTestRule.setContent {
            OmniCalcTheme {
                SettingsScreen(
                    viewModel = viewModel,
                    onNavigate = {},
                    onReplayOnboarding = {},
                )
            }
        }

        // Reset settings via confirmation dialog
        composeTestRule.onNodeWithTag("setting_reset_settings").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Reset").performClick()
        composeTestRule.waitForIdle()

        assertEquals(AppSettings(), viewModel.settings.value)

        // Wait for the "Settings reset to defaults" snackbar at the bottom to clear so it does not cover setting_privacy
        composeTestRule.onNodeWithText("Settings reset to defaults").assertIsDisplayed()
        composeTestRule.mainClock.advanceTimeBy(4_500L)
        composeTestRule.waitForIdle()

        // Open Privacy Policy and verify accurate local Room/DataStore & ExchangeRate-API disclosure
        composeTestRule.onNodeWithTag("setting_privacy").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Room SQLite", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("DataStore Preferences", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("open.er-api.com", substring = true).assertIsDisplayed()
    }

    @Test
    fun `about screen accurately describes Phase 6 persistent history and live currency features`() {
        composeTestRule.setContent {
            OmniCalcTheme {
                AboutScreen(onBack = {})
            }
        }

        composeTestRule.onNodeWithText("Persistent History (Room) & Preferences (DataStore)", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Live Currency Rates", substring = true).assertIsDisplayed()
    }
}
