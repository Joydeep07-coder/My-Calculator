package com.example

import com.example.data.OpenExchangeRateProvider
import com.example.data.SampleCatalogs
import com.example.model.AppSettings
import com.example.model.UnitCategory
import com.example.ui.screens.CurrencyConversionResult
import com.example.ui.screens.UnitConversionResult
import com.example.ui.screens.evaluateCurrencyConversion
import com.example.ui.screens.evaluateUnitConversion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class Phase5ConvertersUnitTest {

    // -------------------------------------------------------------------------
    // Unit Converter Tests
    // -------------------------------------------------------------------------

    @Test
    fun `unit conversions are accurate across length weight temperature area volume speed and fuel`() {
        val lenUnits = SampleCatalogs.unitDefinitions.getValue(UnitCategory.LENGTH).associateBy { it.id }
        val kmToM = evaluateUnitConversion(UnitCategory.LENGTH, lenUnits.getValue("km"), lenUnits.getValue("m"), "1.5") as UnitConversionResult.Valid
        assertEquals(1500.0, kmToM.value, 1e-9)

        val weightUnits = SampleCatalogs.unitDefinitions.getValue(UnitCategory.WEIGHT).associateBy { it.id }
        val lbToKg = evaluateUnitConversion(UnitCategory.WEIGHT, weightUnits.getValue("lb"), weightUnits.getValue("kg"), "10") as UnitConversionResult.Valid
        assertEquals(4.5359237, lbToKg.value, 1e-6)

        val tempUnits = SampleCatalogs.unitDefinitions.getValue(UnitCategory.TEMPERATURE).associateBy { it.id }
        val minus40 = evaluateUnitConversion(UnitCategory.TEMPERATURE, tempUnits.getValue("c"), tempUnits.getValue("f"), "-40") as UnitConversionResult.Valid
        assertEquals(-40.0, minus40.value, 1e-6)
        val cToK = evaluateUnitConversion(UnitCategory.TEMPERATURE, tempUnits.getValue("c"), tempUnits.getValue("k"), "25") as UnitConversionResult.Valid
        assertEquals(298.15, cToK.value, 1e-6)

        val areaUnits = SampleCatalogs.unitDefinitions.getValue(UnitCategory.AREA).associateBy { it.id }
        val haToSqm = evaluateUnitConversion(UnitCategory.AREA, areaUnits.getValue("hectare"), areaUnits.getValue("sqm"), "2") as UnitConversionResult.Valid
        assertEquals(20000.0, haToSqm.value, 1e-6)

        val volUnits = SampleCatalogs.unitDefinitions.getValue(UnitCategory.VOLUME).associateBy { it.id }
        val galToL = evaluateUnitConversion(UnitCategory.VOLUME, volUnits.getValue("gal"), volUnits.getValue("l"), "1") as UnitConversionResult.Valid
        assertEquals(3.785411784, galToL.value, 1e-6)

        val speedUnits = SampleCatalogs.unitDefinitions.getValue(UnitCategory.SPEED).associateBy { it.id }
        val kmhToMps = evaluateUnitConversion(UnitCategory.SPEED, speedUnits.getValue("kmh"), speedUnits.getValue("mps"), "36") as UnitConversionResult.Valid
        assertEquals(10.0, kmhToMps.value, 1e-9)

        val fuelUnits = SampleCatalogs.unitDefinitions.getValue(UnitCategory.FUEL).associateBy { it.id }
        val kmlToL100 = evaluateUnitConversion(UnitCategory.FUEL, fuelUnits.getValue("kml"), fuelUnits.getValue("l100"), "20") as UnitConversionResult.Valid
        assertEquals(5.0, kmlToL100.value, 1e-6)
    }

    @Test
    fun `small unit values preserve precision even when AppSettings decimalPrecision is 2`() {
        val lowPrecisionSettings = AppSettings(decimalPrecision = 2, thousandsSeparator = true)

        val weightUnits = SampleCatalogs.unitDefinitions.getValue(UnitCategory.WEIGHT).associateBy { it.id }
        val mgToKg = evaluateUnitConversion(
            UnitCategory.WEIGHT,
            weightUnits.getValue("mg"),
            weightUnits.getValue("kg"),
            "1",
            lowPrecisionSettings,
        ) as UnitConversionResult.Valid
        assertNotEquals("0", mgToKg.formatted)
        assertEquals("0.000001", mgToKg.formatted)

        val volUnits = SampleCatalogs.unitDefinitions.getValue(UnitCategory.VOLUME).associateBy { it.id }
        val mlToCum = evaluateUnitConversion(
            UnitCategory.VOLUME,
            volUnits.getValue("ml"),
            volUnits.getValue("cum"),
            "1",
            lowPrecisionSettings,
        ) as UnitConversionResult.Valid
        assertEquals("0.000001", mlToCum.formatted)

        val lenUnits = SampleCatalogs.unitDefinitions.getValue(UnitCategory.LENGTH).associateBy { it.id }
        val mmToMi = evaluateUnitConversion(
            UnitCategory.LENGTH,
            lenUnits.getValue("mm"),
            lenUnits.getValue("mi"),
            "1",
            lowPrecisionSettings,
        ) as UnitConversionResult.Valid
        assertNotEquals("0", mmToMi.formatted)
        assertTrue(mmToMi.formatted.contains("e-7"))
    }

    @Test
    fun `unit converter handles empty invalid below absolute zero and fuel zero inputs safely`() {
        val lenUnits = SampleCatalogs.unitDefinitions.getValue(UnitCategory.LENGTH).associateBy { it.id }
        assertEquals(
            UnitConversionResult.Empty,
            evaluateUnitConversion(UnitCategory.LENGTH, lenUnits.getValue("m"), lenUnits.getValue("km"), ""),
        )
        assertTrue(
            evaluateUnitConversion(UnitCategory.LENGTH, lenUnits.getValue("m"), lenUnits.getValue("km"), "xyz") is UnitConversionResult.Error,
        )

        val tempUnits = SampleCatalogs.unitDefinitions.getValue(UnitCategory.TEMPERATURE).associateBy { it.id }
        assertTrue(
            evaluateUnitConversion(UnitCategory.TEMPERATURE, tempUnits.getValue("k"), tempUnits.getValue("c"), "-1") is UnitConversionResult.Error,
        )

        val fuelUnits = SampleCatalogs.unitDefinitions.getValue(UnitCategory.FUEL).associateBy { it.id }
        assertTrue(
            evaluateUnitConversion(UnitCategory.FUEL, fuelUnits.getValue("l100"), fuelUnits.getValue("kml"), "0") is UnitConversionResult.Error,
        )
    }

    // -------------------------------------------------------------------------
    // Currency Converter Tests
    // -------------------------------------------------------------------------

    @Test
    fun `currency converter preserves small value precision and validates empty or negative inputs`() {
        val usd = SampleCatalogs.currencies.first { it.code == "USD" }.copy(rateFromUsd = 1.0)
        val inr = SampleCatalogs.currencies.first { it.code == "INR" }.copy(rateFromUsd = 84.0)
        val settings = AppSettings(decimalPrecision = 2, thousandsSeparator = true)

        val standard = evaluateCurrencyConversion("100", usd, inr, settings) as CurrencyConversionResult.Valid
        assertEquals("8,400.00", standard.formattedAmount)

        // Small value: 0.01 INR -> USD = 0.0001190476... (must NOT round to "0"!)
        val tiny = evaluateCurrencyConversion("0.01", inr, usd, settings) as CurrencyConversionResult.Valid
        assertNotEquals("0", tiny.formattedAmount)
        assertNotEquals("0.00", tiny.formattedAmount)
        assertTrue(tiny.formattedAmount.startsWith("0.000119"))

        assertEquals(CurrencyConversionResult.Empty, evaluateCurrencyConversion("", usd, inr, settings))
        assertTrue(evaluateCurrencyConversion("-5", usd, inr, settings) is CurrencyConversionResult.Error)
        assertTrue(evaluateCurrencyConversion("bad", usd, inr, settings) is CurrencyConversionResult.Error)
    }

    @Test
    fun `open exchange rate json parser extracts live rates source and timestamp`() {
        val sampleJson = """
            {
              "result": "success",
              "time_last_update_utc": "Thu, 19 Nov 2026 00:02:31 +0000",
              "base_code": "USD",
              "rates": {
                "USD": 1,
                "INR": 84.37,
                "EUR": 0.918
              }
            }
        """.trimIndent()

        val parsed = OpenExchangeRateProvider.parseOpenExchangeRateJson(sampleJson)
        assertEquals("ExchangeRate-API (open.er-api.com)", parsed.sourceName)
        assertEquals("Thu, 19 Nov 2026 00:02:31 +0000", parsed.lastUpdatedTimestamp)
        assertEquals(84.37, parsed.ratesFromUsd.getValue("INR"), 1e-6)
        assertEquals(0.918, parsed.ratesFromUsd.getValue("EUR"), 1e-6)

        val errorJson = """{"result": "error", "error-type": "unsupported-code"}"""
        val failure = runCatching { OpenExchangeRateProvider.parseOpenExchangeRateJson(errorJson) }
        assertTrue(failure.isFailure)
    }
}
