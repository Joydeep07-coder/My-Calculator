package com.example

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.navigation.AppRoutes
import com.example.state.OmniCalcViewModel
import com.example.ui.screens.CalculatorScreen
import com.example.ui.theme.OmniCalcTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h915dp")
class CalculatorIntegrationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `calculator screen evaluates implicit multiplication and saves to history`() {
        val viewModel = OmniCalcViewModel()
        composeTestRule.setContent {
            OmniCalcTheme {
                CalculatorScreen(
                    viewModel = viewModel,
                    initialScientific = false,
                    onBack = null,
                    onOpenHistory = {},
                )
            }
        }

        // Compute 2(3+4) = 14
        composeTestRule.onNodeWithTag("key_2").performClick()
        composeTestRule.onNodeWithTag("key_( )").performClick()
        composeTestRule.onNodeWithTag("key_3").performClick()
        composeTestRule.onNodeWithTag("key_+").performClick()
        composeTestRule.onNodeWithTag("key_4").performClick()
        composeTestRule.onNodeWithTag("key_( )").performClick()
        composeTestRule.onNodeWithTag("key_=").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("calculator_result_display").assertTextEquals("14")
        assertEquals(1, viewModel.history.value.size)
        val entry = viewModel.history.value.first()
        assertEquals("2(3+4)", entry.expression)
        assertEquals("14", entry.result)
        assertEquals("Standard", entry.toolName)
        assertEquals(AppRoutes.STANDARD_CALCULATOR, entry.reusableRoute)
        assertEquals("14", viewModel.lastPreviewResult.value)
    }

    @Test
    fun `scientific mode evaluates sin in DEG and RAD and does not save division by zero to history`() {
        val viewModel = OmniCalcViewModel()
        composeTestRule.setContent {
            OmniCalcTheme {
                CalculatorScreen(
                    viewModel = viewModel,
                    initialScientific = true,
                    onBack = null,
                    onOpenHistory = {},
                )
            }
        }

        // Compute sin(30) in DEG -> 0.5
        composeTestRule.onNodeWithTag("sci_key_sin").performClick()
        composeTestRule.onNodeWithTag("key_3").performClick()
        composeTestRule.onNodeWithTag("key_0").performClick()
        composeTestRule.onNodeWithTag("key_( )").performClick()
        composeTestRule.onNodeWithTag("key_=").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("calculator_result_display").assertTextEquals("0.5")
        assertEquals(1, viewModel.history.value.size)
        assertEquals("Scientific", viewModel.history.value.first().toolName)

        // Toggle DEG -> RAD and compute sin(π÷2) -> 1
        composeTestRule.onNodeWithTag("key_AC").performClick()
        composeTestRule.onNodeWithTag("angle_mode_toggle").performClick()
        composeTestRule.onNodeWithTag("sci_key_sin").performClick()
        composeTestRule.onNodeWithTag("sci_key_π").performClick()
        composeTestRule.onNodeWithTag("key_÷").performClick()
        composeTestRule.onNodeWithTag("key_2").performClick()
        composeTestRule.onNodeWithTag("key_( )").performClick()
        composeTestRule.onNodeWithTag("key_=").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("calculator_result_display").assertTextEquals("1")
        assertEquals(2, viewModel.history.value.size)

        // Division by zero should display Error and NOT add to history
        composeTestRule.onNodeWithTag("key_AC").performClick()
        composeTestRule.onNodeWithTag("key_5").performClick()
        composeTestRule.onNodeWithTag("key_÷").performClick()
        composeTestRule.onNodeWithTag("key_0").performClick()
        composeTestRule.onNodeWithTag("key_=").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("calculator_result_display").assertTextEquals("Error")
        assertEquals(2, viewModel.history.value.size)
    }
}
