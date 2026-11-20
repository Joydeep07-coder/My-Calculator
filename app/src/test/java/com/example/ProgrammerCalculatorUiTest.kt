package com.example

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.text.AnnotatedString
import com.example.state.OmniCalcViewModel
import com.example.ui.screens.ProgrammerCalculatorScreen
import com.example.ui.theme.OmniCalcTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h915dp")
class ProgrammerCalculatorUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `programmer calculator displays negative values switches word size evaluates arithmetic and handles AC reset`() {
        val viewModel = OmniCalcViewModel()
        composeTestRule.setContent {
            OmniCalcTheme {
                ProgrammerCalculatorScreen(
                    onBack = {},
                    viewModel = viewModel,
                )
            }
        }

        // Enter 5 and negate -> -5
        composeTestRule.onNodeWithTag("prog_key_5").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("prog_key_negate").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("prog_value_DEC", useUnmergedTree = true).assertTextEquals("-5")
        composeTestRule.onNodeWithTag("prog_value_HEX", useUnmergedTree = true).assertTextEquals("FFFFFFFFFFFFFFFB")

        // Switch to 8-bit word size -> -5 in 8-bit is FB (HEX), 373 (OCT), 1111 1011 (BIN)
        composeTestRule.onNodeWithTag("prog_word_8").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("prog_value_DEC", useUnmergedTree = true).assertTextEquals("-5")
        composeTestRule.onNodeWithTag("prog_value_HEX", useUnmergedTree = true).assertTextEquals("FB")
        composeTestRule.onNodeWithTag("prog_value_OCT", useUnmergedTree = true).assertTextEquals("373")
        composeTestRule.onNodeWithTag("prog_value_BIN", useUnmergedTree = true).assertTextEquals("1111 1011")

        // Evaluate -5 + 12 = 7
        composeTestRule.onNodeWithTag("prog_key_+").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("prog_key_1").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("prog_key_2").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("prog_key_equals").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("prog_value_DEC", useUnmergedTree = true).assertTextEquals("7")

        // AC resets resetOnNextDigit cleanly so next digit starts fresh
        composeTestRule.onNodeWithTag("prog_key_AC").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("prog_key_3").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("prog_value_DEC", useUnmergedTree = true).assertTextEquals("3")

        // Division by zero displays error banner safely
        composeTestRule.onNodeWithTag("prog_key_÷").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("prog_key_0").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("prog_key_equals").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("prog_error_banner").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cannot divide by zero").assertIsDisplayed()
    }

    @Test
    fun `programmer calculator copies numeric result on tap and wires hapticFeedback to AppSettings`() {
        val viewModel = OmniCalcViewModel()
        var copiedText: String? = null
        var hapticCount = 0

        val fakeClipboard = object : ClipboardManager {
            override fun setText(annotatedString: AnnotatedString) {
                copiedText = annotatedString.text
            }

            override fun getText(): AnnotatedString? = copiedText?.let { AnnotatedString(it) }
        }
        val fakeHaptic = object : HapticFeedback {
            override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) {
                hapticCount++
            }
        }

        composeTestRule.setContent {
            OmniCalcTheme {
                CompositionLocalProvider(
                    LocalClipboardManager provides fakeClipboard,
                    LocalHapticFeedback provides fakeHaptic,
                ) {
                    ProgrammerCalculatorScreen(
                        onBack = {},
                        viewModel = viewModel,
                    )
                }
            }
        }

        // Haptic enabled by default -> tapping '1' and '5' triggers haptic twice
        composeTestRule.onNodeWithTag("prog_key_1").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("prog_key_5").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assertEquals(2, hapticCount)

        // Disable haptic feedback in AppSettings -> tapping '×', '2', '=' triggers 0 more haptics
        viewModel.setHapticFeedback(false)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("prog_key_×").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("prog_key_2").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("prog_key_equals").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assertEquals(2, hapticCount)
        composeTestRule.onNodeWithTag("prog_value_DEC", useUnmergedTree = true).assertTextEquals("30")
        composeTestRule.onNodeWithTag("prog_value_HEX", useUnmergedTree = true).assertTextEquals("1E")

        // Copy-on-tap copies the HEX value "1E"
        composeTestRule.onNodeWithTag("prog_copy_HEX").performClick()
        composeTestRule.waitForIdle()
        assertEquals("1E", copiedText)

        // Tapping the already-selected DEC row also copies "30"
        composeTestRule.onNodeWithTag("prog_base_DEC").performClick()
        composeTestRule.waitForIdle()
        assertEquals("30", copiedText)
    }
}
