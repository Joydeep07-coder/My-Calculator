package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import com.example.data.ExchangeRateProvider
import com.example.data.LiveExchangeRatesResult
import com.example.model.UnitCategory
import com.example.state.OmniCalcViewModel
import com.example.ui.screens.CurrencyConverterScreen
import com.example.ui.screens.EmiCalculatorScreen
import com.example.ui.screens.GstCalculatorScreen
import com.example.ui.screens.UnitConverterScreen
import com.example.ui.theme.OmniCalcTheme
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h915dp")
class Phase5UiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `gst screen supports 0 percent uses AppSettings currency and validates inputs`() {
        val viewModel = OmniCalcViewModel()
        viewModel.setCurrencyCode("USD")

        composeTestRule.setContent {
            OmniCalcTheme {
                GstCalculatorScreen(viewModel = viewModel, onBack = {})
            }
        }

        // Blank input disables Save to History
        composeTestRule.onNodeWithTag("gst_save_history_button").performScrollTo().assertIsNotEnabled()

        // Enter negative amount -> shows error banner
        composeTestRule.onNodeWithTag("gst_amount_input").performTextInput("-100")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("gst_error_banner").assertIsDisplayed()

        // Replace with 1000 @ 0% -> $1,000 result using AppSettings USD currency
        composeTestRule.onNodeWithTag("gst_amount_input").performClick()
        composeTestRule.onNodeWithTag("gst_amount_input").performTextReplacement("1000")
        composeTestRule.onNodeWithTag("gst_preset_0%").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("gst_result_card").performScrollTo().assertIsDisplayed()
        composeTestRule.onAllNodesWithText("$1,000").onFirst().assertIsDisplayed()
    }

    @Test
    fun `emi screen handles blank tenure zero percent interest and accurate breakdown percentages`() {
        val viewModel = OmniCalcViewModel()

        composeTestRule.setContent {
            OmniCalcTheme {
                EmiCalculatorScreen(viewModel = viewModel, onBack = {})
            }
        }

        // Fill loan and rate, leave tenure blank -> must NOT coerce tenure to 1 year / 12 months
        composeTestRule.onNodeWithTag("emi_loan_input").performTextInput("120000")
        composeTestRule.onNodeWithTag("emi_rate_input").performTextInput("0")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("emi_save_button").performScrollTo().assertIsNotEnabled()

        // Enter 12 months @ 0% -> 100% Principal, 0% Interest (not 95%/5%)
        composeTestRule.onNodeWithTag("emi_tenure_months").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("emi_tenure_input").performScrollTo().performTextInput("12")
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("emi_principal_pct").performScrollTo().assertTextEquals("● Principal (100%)")
        composeTestRule.onNodeWithTag("emi_interest_pct").performScrollTo().assertTextEquals("● Interest (0%)")
        composeTestRule.onNodeWithTag("emi_result_card").performScrollTo().assertIsDisplayed()
    }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h640dp")
    fun `unit converter scrolls categories on narrow 320dp screen and avoids rounding tiny values to zero`() {
        val viewModel = OmniCalcViewModel()
        viewModel.setDecimalPrecision(2)

        composeTestRule.setContent {
            OmniCalcTheme {
                UnitConverterScreen(viewModel = viewModel, onBack = {})
            }
        }

        // Empty state shows placeholder instead of "0"
        composeTestRule.onNodeWithTag("unit_converted_result").assertTextEquals("—")
        composeTestRule.onNodeWithTag("unit_bookmark_button").assertIsNotEnabled()

        // Scroll LazyRow horizontally to FUEL category on narrow 320dp screen and select it
        composeTestRule.onNodeWithTag("unit_category_row").performScrollToIndex(UnitCategory.FUEL.ordinal)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("unit_cat_FUEL").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("unit_input_field").performTextInput("20")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("unit_converted_result").assertTextEquals("47.04 mpg")

        // Verify small non-zero value is NOT rounded to 0 despite decimalPrecision=2
        composeTestRule.onNodeWithTag("unit_input_field").performTextReplacement("0.0001")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("unit_converted_result").assertTextEquals("0.0002352 mpg")
    }

    @Test
    fun `currency converter shows loading state error retry state and live source with timestamp`() {
        val firstResponse = CompletableDeferred<Result<LiveExchangeRatesResult>>()
        var attempts = 0
        val provider = object : ExchangeRateProvider {
            override suspend fun fetchUsdRates(): Result<LiveExchangeRatesResult> {
                attempts++
                return if (attempts == 1) {
                    firstResponse.await()
                } else {
                    Result.success(
                        LiveExchangeRatesResult(
                            ratesFromUsd = mapOf("USD" to 1.0, "INR" to 85.0, "EUR" to 0.92),
                            sourceName = "ExchangeRate-API (open.er-api.com)",
                            lastUpdatedTimestamp = "Thu, 19 Nov 2026 00:00:00 +0000",
                        )
                    )
                }
            }
        }

        composeTestRule.setContent {
            OmniCalcTheme {
                CurrencyConverterScreen(
                    onBack = {},
                    viewModel = OmniCalcViewModel(),
                    rateProvider = provider,
                )
            }
        }

        // 1. Loading indicator shown while network call in flight
        composeTestRule.onNodeWithTag("currency_loading_indicator").assertIsDisplayed()
        composeTestRule.onNodeWithTag("currency_converted_result").assertTextEquals("—")

        // 2. Network failure shows Error banner + Retry button (no fake live rates!)
        firstResponse.complete(Result.failure(IllegalStateException("Network offline")))
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("currency_error_banner").assertIsDisplayed()
        composeTestRule.onNodeWithTag("currency_retry_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("currency_unit_rate_text").assertTextContains("unavailable", substring = true)

        // 3. Retry succeeds -> displays source, timestamp, and converts entered amount
        composeTestRule.onNodeWithTag("currency_retry_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("currency_source_banner").assertIsDisplayed()
        composeTestRule.onNodeWithText("Source: ExchangeRate-API (open.er-api.com)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Last updated: Thu, 19 Nov 2026 00:00:00 +0000").assertIsDisplayed()

        composeTestRule.onNodeWithTag("currency_amount_input").performScrollTo().performTextInput("10")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("currency_converted_result").performScrollTo().assertTextEquals("₹ 850.00")
        assertEquals(2, attempts)
    }
}
