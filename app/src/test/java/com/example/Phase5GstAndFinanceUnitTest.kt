package com.example

import com.example.model.AppSettings
import com.example.ui.screens.DiscountCalculationResult
import com.example.ui.screens.EmiCalculationResult
import com.example.ui.screens.GstCalculationResult
import com.example.ui.screens.SimpleOrCompoundResult
import com.example.ui.screens.calculateCompoundInterest
import com.example.ui.screens.calculateDiscount
import com.example.ui.screens.calculateEmi
import com.example.ui.screens.calculateGst
import com.example.ui.screens.calculateSimpleInterest
import com.example.ui.screens.formatSettingsCurrency
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Phase5GstAndFinanceUnitTest {

    // -------------------------------------------------------------------------
    // GST Calculator Tests
    // -------------------------------------------------------------------------

    @Test
    fun `gst exclusive calculation across 0 5 12 18 28 percent and intra vs inter state`() {
        val rates = listOf(0, 5, 12, 18, 28)
        for (rate in rates) {
            val intra = calculateGst(
                amountInput = "1000",
                isAddGst = true,
                selectedPreset = rate,
                customRateInput = "",
                isInterState = false,
            ) as GstCalculationResult.Valid

            val expectedGst = 1000.0 * rate / 100.0
            assertEquals(1000.0, intra.taxableAmount, 1e-6)
            assertEquals(expectedGst, intra.gstAmount, 1e-6)
            assertEquals(expectedGst / 2.0, intra.cgstAmount, 1e-6)
            assertEquals(expectedGst / 2.0, intra.sgstAmount, 1e-6)
            assertEquals(0.0, intra.igstAmount, 1e-6)
            assertEquals(1000.0 + expectedGst, intra.totalAmount, 1e-6)

            val inter = calculateGst(
                amountInput = "1000",
                isAddGst = true,
                selectedPreset = rate,
                customRateInput = "",
                isInterState = true,
            ) as GstCalculationResult.Valid

            assertEquals(0.0, inter.cgstAmount, 1e-6)
            assertEquals(0.0, inter.sgstAmount, 1e-6)
            assertEquals(expectedGst, inter.igstAmount, 1e-6)
        }
    }

    @Test
    fun `gst inclusive removal and custom decimal rate work accurately`() {
        val inclusive = calculateGst(
            amountInput = "1180",
            isAddGst = false,
            selectedPreset = 18,
            customRateInput = "",
            isInterState = false,
        ) as GstCalculationResult.Valid

        assertEquals(1000.0, inclusive.taxableAmount, 1e-6)
        assertEquals(180.0, inclusive.gstAmount, 1e-6)
        assertEquals(90.0, inclusive.cgstAmount, 1e-6)
        assertEquals(90.0, inclusive.sgstAmount, 1e-6)
        assertEquals(1180.0, inclusive.totalAmount, 1e-6)

        val custom = calculateGst(
            amountInput = "2000",
            isAddGst = true,
            selectedPreset = -1,
            customRateInput = "7.5",
            isInterState = true,
        ) as GstCalculationResult.Valid

        assertEquals(7.5, custom.ratePercent, 1e-6)
        assertEquals(150.0, custom.gstAmount, 1e-6)
        assertEquals(150.0, custom.igstAmount, 1e-6)
        assertEquals(2150.0, custom.totalAmount, 1e-6)
    }

    @Test
    fun `gst validates empty negative non-numeric and invalid custom rates`() {
        assertEquals(
            GstCalculationResult.Empty,
            calculateGst("", true, 18, "", false),
        )
        assertTrue(
            calculateGst("-500", true, 18, "", false) is GstCalculationResult.Error,
        )
        assertTrue(
            calculateGst("abc", true, 18, "", false) is GstCalculationResult.Error,
        )
        assertTrue(
            calculateGst("1000", true, -1, "", false) is GstCalculationResult.Error,
        )
        assertTrue(
            calculateGst("1000", true, -1, "120", false) is GstCalculationResult.Error,
        )
        assertTrue(
            calculateGst("1000", true, -1, "-5", false) is GstCalculationResult.Error,
        )
    }

    @Test
    fun `app settings currency symbol precision and grouping format GST and finance outputs`() {
        val usdSettings = AppSettings(
            currencyCode = "USD",
            decimalPrecision = 2,
            thousandsSeparator = true,
        )
        assertEquals("$1,234.57", formatSettingsCurrency(1234.5678, usdSettings))

        val eurNoGrouping = AppSettings(
            currencyCode = "EUR",
            decimalPrecision = 4,
            thousandsSeparator = false,
        )
        assertEquals("€1234.5678", formatSettingsCurrency(1234.5678, eurNoGrouping))
    }

    // -------------------------------------------------------------------------
    // EMI & Finance Calculator Tests
    // -------------------------------------------------------------------------

    @Test
    fun `emi calculates accurate monthly installment interest and month 1 breakdown`() {
        // 100,000 @ 12% p.a. for 1 year (12 months)
        val result = calculateEmi(
            loanAmountInput = "100000",
            rateInput = "12",
            tenureInput = "1",
            isYears = true,
        ) as EmiCalculationResult.Valid

        assertEquals(12, result.months)
        assertEquals(8884.8788678, result.monthlyEmi, 1e-4)
        assertEquals(106618.5464, result.totalPayment, 1e-3)
        assertEquals(6618.5464, result.totalInterest, 1e-3)
        assertEquals(1000.0, result.month1Interest, 1e-4)
        assertEquals(7884.8788678, result.month1Principal, 1e-4)
        assertEquals(100000.0 / 12.0, result.avgMonthlyPrincipal, 1e-4)
        assertEquals(result.totalInterest / 12.0, result.avgMonthlyInterest, 1e-4)
        assertEquals(100000.0 / result.totalPayment, result.principalShare, 1e-6)
        assertEquals(result.totalInterest / result.totalPayment, result.interestShare, 1e-6)
    }

    @Test
    fun `emi zero interest loan produces exact 100 percent principal and 0 percent interest`() {
        val result = calculateEmi(
            loanAmountInput = "120000",
            rateInput = "0",
            tenureInput = "12",
            isYears = false,
        ) as EmiCalculationResult.Valid

        assertEquals(10000.0, result.monthlyEmi, 1e-6)
        assertEquals(0.0, result.totalInterest, 1e-6)
        assertEquals(120000.0, result.totalPayment, 1e-6)
        assertEquals(1.0, result.principalShare, 1e-6)
        assertEquals(0.0, result.interestShare, 1e-6)
        assertEquals(0.0, result.month1Interest, 1e-6)
        assertEquals(10000.0, result.month1Principal, 1e-6)
    }

    @Test
    fun `emi fractional years convert accurately and invalid or blank inputs do not coerce tenure`() {
        val halfYear = calculateEmi("60000", "0", "0.5", isYears = true) as EmiCalculationResult.Valid
        assertEquals(6, halfYear.months)
        assertEquals(10000.0, halfYear.monthlyEmi, 1e-6)

        // Blank tenure must return Empty, NOT coerce to 1 year / 12 months
        assertEquals(EmiCalculationResult.Empty, calculateEmi("100000", "10", "", true))
        assertTrue(calculateEmi("100000", "10", "0", true) is EmiCalculationResult.Error)
        assertTrue(calculateEmi("100000", "10", "-2", true) is EmiCalculationResult.Error)
        assertTrue(calculateEmi("0", "10", "5", true) is EmiCalculationResult.Error)
        assertTrue(calculateEmi("-1000", "10", "5", true) is EmiCalculationResult.Error)
        assertTrue(calculateEmi("100000", "-1", "5", true) is EmiCalculationResult.Error)
        assertTrue(calculateEmi("100000", "10", "12.5", isYears = false) is EmiCalculationResult.Error)
    }

    @Test
    fun `simple interest compound interest and discount calculations and validations`() {
        val si = calculateSimpleInterest("10000", "8", "3") as SimpleOrCompoundResult.Valid
        assertEquals(2400.0, si.interest, 1e-6)
        assertEquals(12400.0, si.totalAmount, 1e-6)
        assertTrue(calculateSimpleInterest("-100", "8", "3") is SimpleOrCompoundResult.Error)
        assertEquals(SimpleOrCompoundResult.Empty, calculateSimpleInterest("10000", "", "3"))

        // 10,000 @ 10% compounded quarterly for 2 years = 10000 * (1.025)^8 = 12184.028975...
        val ci = calculateCompoundInterest("10000", "10", "2", 4) as SimpleOrCompoundResult.Valid
        assertEquals(12184.028975, ci.totalAmount, 1e-4)
        assertEquals(2184.028975, ci.interest, 1e-4)
        assertTrue(calculateCompoundInterest("10000", "abc", "2", 4) is SimpleOrCompoundResult.Error)

        val disc = calculateDiscount("2500", "20") as DiscountCalculationResult.Valid
        assertEquals(500.0, disc.discountAmount, 1e-6)
        assertEquals(2000.0, disc.finalPrice, 1e-6)
        assertTrue(calculateDiscount("2500", "150") is DiscountCalculationResult.Error)
        assertTrue(calculateDiscount("-10", "20") is DiscountCalculationResult.Error)
    }
}
