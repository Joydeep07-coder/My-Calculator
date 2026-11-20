package com.example

import com.example.data.SampleCatalogs
import com.example.data.SimpleExprHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Locale
import kotlin.math.E
import kotlin.math.PI

class CoreExpressionEngineTest {

    private fun eval(expr: String, isDegrees: Boolean = true, xValue: Double? = null): Double {
        val result = SimpleExprHelper.evaluate(expr, isDegrees = isDegrees, xValue = xValue)
        assertNotNull("Expected non-null result for '$expr'", result)
        return result!!
    }

    @Test
    fun `positive arithmetic and precedence`() {
        assertEquals(14.0, eval("2 + 3 × 4"), 1e-9)
        assertEquals(20.0, eval("(2 + 3) × 4"), 1e-9)
        assertEquals(2.5, eval("5 ÷ 2"), 1e-9)
        assertEquals(512.0, eval("2^3^2"), 1e-9)
        assertEquals(19.0, eval("1 + 2 × 3^2"), 1e-9)
        assertEquals(4.0, eval("((1 + 1) × (1 + 1))"), 1e-9)
    }

    @Test
    fun `negative numbers and unary precedence with exponents`() {
        assertEquals(-9.0, eval("-3^2"), 1e-9)
        assertEquals(-9.0, eval("−3^2"), 1e-9)
        assertEquals(9.0, eval("(-3)^2"), 1e-9)
        assertEquals(-9.0, eval("-x^2", xValue = 3.0), 1e-9)
        assertEquals(-9.0, eval("-x^2", xValue = -3.0), 1e-9)
        assertEquals(-18.0, eval("-2x^2", xValue = 3.0), 1e-9)
        assertEquals(0.125, eval("2^-3"), 1e-9)
        assertEquals(-6.0, eval("2 × -3"), 1e-9)
        assertEquals(-8.0, eval("-5 + -3"), 1e-9)
        assertEquals(0.0, eval("-0^2"), 1e-9)
    }

    @Test
    fun `decimal expressions and locale-independent formatting`() {
        assertEquals(0.75, eval("0.5 + 0.25"), 1e-9)
        assertEquals(5.0, eval("1.25 × 4"), 1e-9)
        assertEquals(1.0, eval(".5 × 2"), 1e-9)
        assertEquals(0.125, eval("0.5^3"), 1e-9)

        val previousLocale = Locale.getDefault()
        try {
            Locale.setDefault(Locale.GERMANY)
            assertEquals("1,234.56", SampleCatalogs.formatNumber(1234.56, precision = 2, useGrouping = true))
            assertEquals("1.5", SampleCatalogs.formatNumber(1.5, precision = 6, useGrouping = false))
            assertEquals("0", SampleCatalogs.formatNumber(-1e-15, precision = 6, useGrouping = true))

            Locale.setDefault(Locale.FRANCE)
            assertEquals("1000.25", SampleCatalogs.formatNumber(1000.25, precision = 2, useGrouping = false))
            assertEquals("1,000.25", SampleCatalogs.formatNumber(1000.25, precision = 2, useGrouping = true))
        } finally {
            Locale.setDefault(previousLocale)
        }
    }

    @Test
    fun `implicit multiplication expressions`() {
        assertEquals(14.0, eval("2(3+4)"), 1e-9)
        assertEquals(18.0, eval("2×(4+5)"), 1e-9)
        assertEquals(3.0 * PI, eval("3π"), 1e-9)
        assertEquals(2.0 * E, eval("2e"), 1e-9)
        assertEquals(45.0, eval("(2+3)(4+5)"), 1e-9)
        assertEquals(20.0, eval("(2+3)4"), 1e-9)
        assertEquals(1.0, eval("2sin(30)", isDegrees = true), 1e-9)
        assertEquals(6.0, eval("2√(9)"), 1e-9)
        assertEquals(6.0, eval("2√9"), 1e-9)
        assertEquals(18.0, eval("2x^2", xValue = 3.0), 1e-9)
        assertEquals(2.0, eval("50%(4)"), 1e-9)
    }

    @Test
    fun `scientific calculations in DEG and RAD modes`() {
        // DEG mode
        assertEquals(0.5, eval("sin(30)", isDegrees = true), 1e-9)
        assertEquals(1.0, eval("sin(90)", isDegrees = true), 1e-12)
        assertEquals(0.0, eval("sin(180)", isDegrees = true), 1e-12)
        assertEquals(0.0, eval("cos(90)", isDegrees = true), 1e-12)
        assertEquals(-1.0, eval("cos(180)", isDegrees = true), 1e-12)
        assertEquals(1.0, eval("tan(45)", isDegrees = true), 1e-9)
        assertEquals(30.0, eval("sin⁻¹(0.5)", isDegrees = true), 1e-9)
        assertEquals(60.0, eval("cos⁻¹(0.5)", isDegrees = true), 1e-9)
        assertEquals(45.0, eval("tan⁻¹(1)", isDegrees = true), 1e-9)

        // RAD mode
        assertEquals(1.0, eval("sin(π÷2)", isDegrees = false), 1e-9)
        assertEquals(1.0, eval("cos(0)", isDegrees = false), 1e-9)
        assertEquals(PI / 2.0, eval("sin⁻¹(1)", isDegrees = false), 1e-9)
        assertEquals(PI / 4.0, eval("tan⁻¹(1)", isDegrees = false), 1e-9)

        // Logs, roots, abs, factorial, percent
        assertEquals(2.0, eval("log(100)"), 1e-9)
        assertEquals(1.0, eval("ln(e)"), 1e-9)
        assertEquals(5.0, eval("√(9+16)"), 1e-9)
        assertEquals(7.5, eval("abs(-7.5)"), 1e-9)
        assertEquals(120.0, eval("5!"), 1e-9)
        assertEquals(1.0, eval("0!"), 1e-9)
        assertEquals(-120.0, eval("-5!"), 1e-9)
        assertEquals(0.5, eval("50%"), 1e-9)
        assertEquals(30.0, eval("200×15%"), 1e-9)
        assertEquals(4.0, eval("2÷50%"), 1e-9)
        assertEquals(0.25, eval("50%^2"), 1e-9)
    }

    @Test
    fun `edge cases and invalid expressions return null safely`() {
        assertNull(SimpleExprHelper.evaluate(""))
        assertNull(SimpleExprHelper.evaluate("   "))
        assertNull(SimpleExprHelper.evaluate("1÷0"))
        assertNull(SimpleExprHelper.evaluate("5 ÷ (2 - 2)"))
        assertNull(SimpleExprHelper.evaluate("tan(90)", isDegrees = true))
        assertNull(SimpleExprHelper.evaluate("tan(270)", isDegrees = true))
        assertNull(SimpleExprHelper.evaluate("tan(π÷2)", isDegrees = false))
        assertNull(SimpleExprHelper.evaluate("√(-4)"))
        assertNull(SimpleExprHelper.evaluate("log(0)"))
        assertNull(SimpleExprHelper.evaluate("ln(-2)"))
        assertNull(SimpleExprHelper.evaluate("sin⁻¹(1.5)"))
        assertNull(SimpleExprHelper.evaluate("cos⁻¹(-1.5)"))
        assertNull(SimpleExprHelper.evaluate("3.5!"))
        assertNull(SimpleExprHelper.evaluate("(-3)!"))
        assertNull(SimpleExprHelper.evaluate("171!"))
        assertNull(SimpleExprHelper.evaluate("1..2"))
        assertNull(SimpleExprHelper.evaluate("."))
        assertNull(SimpleExprHelper.evaluate("(2+3"))
        assertNull(SimpleExprHelper.evaluate("2+"))
        assertNull(SimpleExprHelper.evaluate("unknown(5)"))
        assertNull(SimpleExprHelper.evaluate("10^400"))
        assertNull(SimpleExprHelper.evaluate("(-2)^0.5"))
    }
}
