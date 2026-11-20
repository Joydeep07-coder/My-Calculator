package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.example.ui.screens.AgeCalculatorScreen
import com.example.ui.screens.BmiCalculatorScreen
import com.example.ui.theme.OmniCalcTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h915dp")
class AgeAndBmiUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `age calculator shows validation error when calculating without selecting date`() {
        composeTestRule.setContent {
            OmniCalcTheme {
                AgeCalculatorScreen(onBack = {})
            }
        }

        composeTestRule.onNodeWithTag("age_calculate_button").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("age_error_banner").assertIsDisplayed()
        composeTestRule.onNodeWithText("Please select your date of birth first.").assertIsDisplayed()
    }

    @Test
    fun `bmi calculator hides unused gender chips validates bad input and computes imperial lbs range`() {
        composeTestRule.setContent {
            OmniCalcTheme {
                BmiCalculatorScreen(onBack = {})
            }
        }

        // Unused Gender chips must not be present
        composeTestRule.onNodeWithText("Gender (Optional)").assertDoesNotExist()
        composeTestRule.onNodeWithText("Male").assertDoesNotExist()
        composeTestRule.onNodeWithText("Female").assertDoesNotExist()
        composeTestRule.onNodeWithText("Unspecified").assertDoesNotExist()

        // Empty or zero inputs show validation banner and no result
        composeTestRule.onNodeWithTag("bmi_calculate_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("bmi_error_banner").assertIsDisplayed()

        composeTestRule.onNodeWithTag("bmi_height_input").performTextInput("0")
        composeTestRule.onNodeWithTag("bmi_weight_input").performTextInput("70")
        composeTestRule.onNodeWithTag("bmi_calculate_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("bmi_error_banner").assertIsDisplayed()
        composeTestRule.onNodeWithText("Height and weight must be greater than zero.").assertIsDisplayed()

        // Switch to Imperial and compute 69 in, 154 lbs -> BMI 22.7, Normal weight, healthy range in lbs
        composeTestRule.onNodeWithTag("bmi_unit_imperial").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("bmi_error_banner").assertDoesNotExist()

        composeTestRule.onNodeWithTag("bmi_height_input").performTextInput("69")
        composeTestRule.onNodeWithTag("bmi_weight_input").performTextInput("154")
        composeTestRule.onNodeWithTag("bmi_calculate_button").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("bmi_error_banner").assertDoesNotExist()
        composeTestRule.onNodeWithTag("bmi_result_value").performScrollTo().assertTextEquals("22.7")
        composeTestRule.onNodeWithTag("bmi_category_badge").performScrollTo().assertTextEquals("Normal weight")
        composeTestRule.onNodeWithTag("bmi_healthy_range").performScrollTo().assertTextContains("lbs", substring = true)
    }
}
