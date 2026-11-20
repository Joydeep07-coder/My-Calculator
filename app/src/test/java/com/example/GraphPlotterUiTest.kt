package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import com.example.model.AngleMode
import com.example.state.OmniCalcViewModel
import com.example.ui.screens.GraphPlotterScreen
import com.example.ui.theme.OmniCalcTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h915dp")
class GraphPlotterUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `graph plotter validates invalid expressions plots valid functions and updates fractional zoom labels`() {
        val viewModel = OmniCalcViewModel()
        composeTestRule.setContent {
            OmniCalcTheme {
                GraphPlotterScreen(
                    onBack = {},
                    viewModel = viewModel,
                )
            }
        }

        // Empty expression rejected cleanly with error banner
        composeTestRule.onNodeWithTag("graph_add_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("graph_error_banner").assertIsDisplayed()
        composeTestRule.onNodeWithText("Plotted Functions (0)").assertIsDisplayed()

        // Syntactically invalid expression rejected cleanly
        composeTestRule.onNodeWithTag("graph_expr_input").performTextInput("sin(")
        composeTestRule.onNodeWithTag("graph_add_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("graph_error_banner").assertIsDisplayed()
        composeTestRule.onNodeWithText("Plotted Functions (0)").assertIsDisplayed()

        // Valid expression clears error and adds normalized function
        composeTestRule.onNodeWithTag("graph_expr_input").performTextReplacement("y = x^2")
        composeTestRule.onNodeWithTag("graph_add_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("graph_error_banner").assertDoesNotExist()
        composeTestRule.onNodeWithText("Plotted Functions (1)").assertIsDisplayed()
        composeTestRule.onNodeWithText("f(x) = x^2").assertIsDisplayed()

        // Zoom in from [-6, 6] -> [-4.5, 4.5] without integer truncation
        composeTestRule.onNodeWithTag("graph_viewport_label").assertTextContains("X [-6, 6]", substring = true)
        composeTestRule.onNodeWithTag("graph_zoom_in").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("graph_viewport_label").assertTextContains("X [-4.5, 4.5]", substring = true)

        // Reset restores [-6, 6]
        composeTestRule.onNodeWithTag("graph_reset").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("graph_viewport_label").assertTextContains("X [-6, 6]", substring = true)

        // Angle mode toggle syncs with AppSettings
        composeTestRule.onNodeWithTag("graph_angle_toggle").assertTextEquals("DEG")
        composeTestRule.onNodeWithTag("graph_angle_toggle").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("graph_angle_toggle").assertTextEquals("RAD")
        assertEquals(AngleMode.RAD, viewModel.settings.value.angleMode)
    }
}
