package com.example

import com.example.data.SimpleExprHelper
import com.example.ui.screens.GraphViewport
import com.example.ui.screens.PlotValidationResult
import com.example.ui.screens.formatAxisTick
import com.example.ui.screens.niceTickStep
import com.example.ui.screens.samplePlotPolylines
import com.example.ui.screens.validatePlotExpression
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.E
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.sin
import kotlin.math.tan

class GraphPlotterUnitTest {

    private fun compileOk(expr: String): SimpleExprHelper.CompiledExpr {
        val validation = validatePlotExpression(expr)
        assertTrue("Expected valid expression for '$expr', got $validation", validation is PlotValidationResult.Valid)
        val normalized = (validation as PlotValidationResult.Valid).normalizedExpression
        val compiled = SimpleExprHelper.compile(normalized, allowVariableX = true)
        assertTrue(compiled is SimpleExprHelper.CompileResult.Success)
        return (compiled as SimpleExprHelper.CompileResult.Success).expr
    }

    // ==================== 1. MATHEMATICAL ACCURACY ====================

    @Test
    fun `linear and quadratic functions y=x and y=x^2 evaluate accurately`() {
        val linear = compileOk("y = x")
        assertEquals(-3.5, linear.evaluate(isDegrees = false, xValue = -3.5)!!, 1e-9)
        assertEquals(0.0, linear.evaluate(isDegrees = false, xValue = 0.0)!!, 1e-9)
        assertEquals(4.25, linear.evaluate(isDegrees = false, xValue = 4.25)!!, 1e-9)

        val quad = compileOk("y = x^2")
        assertEquals(9.0, quad.evaluate(isDegrees = false, xValue = 3.0)!!, 1e-9)
        assertEquals(9.0, quad.evaluate(isDegrees = false, xValue = -3.0)!!, 1e-9)

        // Unary precedence: -x^2 = -(x^2)
        val negQuad = compileOk("-x^2")
        assertEquals(-9.0, negQuad.evaluate(isDegrees = false, xValue = 3.0)!!, 1e-9)
        assertEquals(-9.0, negQuad.evaluate(isDegrees = false, xValue = -3.0)!!, 1e-9)
    }

    @Test
    fun `trig functions sin cos tan respect RAD and DEG modes`() {
        val sinFn = compileOk("y = sin(x)")
        val cosFn = compileOk("y = cos(x)")
        val tanFn = compileOk("y = tan(x)")

        // RAD mode
        assertEquals(sin(1.2), sinFn.evaluate(isDegrees = false, xValue = 1.2)!!, 1e-9)
        assertEquals(1.0, sinFn.evaluate(isDegrees = false, xValue = PI / 2.0)!!, 1e-9)
        assertEquals(cos(0.7), cosFn.evaluate(isDegrees = false, xValue = 0.7)!!, 1e-9)
        assertEquals(-1.0, cosFn.evaluate(isDegrees = false, xValue = PI)!!, 1e-9)
        assertEquals(tan(0.5), tanFn.evaluate(isDegrees = false, xValue = 0.5)!!, 1e-9)
        assertNull(tanFn.evaluate(isDegrees = false, xValue = PI / 2.0))

        // DEG mode
        assertEquals(0.5, sinFn.evaluate(isDegrees = true, xValue = 30.0)!!, 1e-9)
        assertEquals(0.0, cosFn.evaluate(isDegrees = true, xValue = 90.0)!!, 1e-9)
        assertEquals(1.0, tanFn.evaluate(isDegrees = true, xValue = 45.0)!!, 1e-9)
        assertNull(tanFn.evaluate(isDegrees = true, xValue = 90.0))
    }

    @Test
    fun `logarithmic exponential and rational functions evaluate accurately and handle undefined domains`() {
        val lnFn = compileOk("ln(x)")
        assertNull(lnFn.evaluate(isDegrees = false, xValue = -2.0))
        assertNull(lnFn.evaluate(isDegrees = false, xValue = 0.0))
        assertEquals(0.0, lnFn.evaluate(isDegrees = false, xValue = 1.0)!!, 1e-9)
        assertEquals(ln(5.0), lnFn.evaluate(isDegrees = false, xValue = 5.0)!!, 1e-9)

        val logFn = compileOk("log(x)")
        assertNull(logFn.evaluate(isDegrees = false, xValue = 0.0))
        assertEquals(2.0, logFn.evaluate(isDegrees = false, xValue = 100.0)!!, 1e-9)

        val expFn = compileOk("exp(x)")
        assertEquals(1.0, expFn.evaluate(isDegrees = false, xValue = 0.0)!!, 1e-9)
        assertEquals(exp(2.0), expFn.evaluate(isDegrees = false, xValue = 2.0)!!, 1e-9)

        val ePowFn = compileOk("e^x")
        assertEquals(E, ePowFn.evaluate(isDegrees = false, xValue = 1.0)!!, 1e-9)

        val rational = compileOk("1/x")
        assertNull(rational.evaluate(isDegrees = false, xValue = 0.0))
        assertEquals(0.5, rational.evaluate(isDegrees = false, xValue = 2.0)!!, 1e-9)
        assertEquals(-0.5, rational.evaluate(isDegrees = false, xValue = -2.0)!!, 1e-9)
    }

    // ==================== 2. ASYMPTOTE & DISCONTINUITY SPLITTING ====================

    @Test
    fun `1 over x splits at x=0 and never connects negative branch to positive branch`() {
        val expr = compileOk("1/x")
        // Use even sampleCount=201 so samples step over x=0 cleanly without hitting 0.0 directly
        val polylines = samplePlotPolylines(
            compiledExpr = expr,
            xMin = -6.0,
            xMax = 6.0,
            yMin = -5.0,
            yMax = 5.0,
            isDegrees = false,
            sampleCount = 201,
        )

        assertTrue("Expected at least 2 disconnected branches for 1/x", polylines.size >= 2)
        polylines.forEach { segment ->
            val hasNegX = segment.any { it.x < 0.0 }
            val hasPosX = segment.any { it.x > 0.0 }
            assertTrue("Segment must not cross x=0 asymptote", !(hasNegX && hasPosX))
        }
    }

    @Test
    fun `tan(x) splits cleanly across pi over 2 asymptotes without connecting lines`() {
        val expr = compileOk("tan(x)")
        val polylines = samplePlotPolylines(
            compiledExpr = expr,
            xMin = -6.0,
            xMax = 6.0,
            yMin = -5.0,
            yMax = 5.0,
            isDegrees = false,
            sampleCount = 300,
        )

        val poles = listOf(-1.5 * PI, -0.5 * PI, 0.5 * PI, 1.5 * PI)
        assertTrue("Expected multiple disconnected tan(x) branches, got ${polylines.size}", polylines.size >= 4)
        polylines.forEach { segment ->
            val minX = segment.minOf { it.x }
            val maxX = segment.maxOf { it.x }
            poles.forEach { pole ->
                assertTrue(
                    "Segment [$minX, $maxX] crossed tan(x) asymptote at $pole",
                    !(minX < pole && maxX > pole),
                )
            }
        }
    }

    @Test
    fun `continuous curves x and x^2 remain single connected segments across visible range`() {
        val linear = compileOk("y = x")
        val linearSegs = samplePlotPolylines(linear, -4.0, 4.0, -5.0, 5.0, isDegrees = false, sampleCount = 200)
        assertEquals(1, linearSegs.size)

        val quad = compileOk("y = x^2")
        val quadSegs = samplePlotPolylines(quad, -2.0, 2.0, -1.0, 5.0, isDegrees = false, sampleCount = 200)
        assertEquals(1, quadSegs.size)

        // ln(x) only produces points for x > 0
        val lnExpr = compileOk("ln(x)")
        val lnSegs = samplePlotPolylines(lnExpr, -6.0, 6.0, -5.0, 5.0, isDegrees = false, sampleCount = 200)
        assertEquals(1, lnSegs.size)
        assertTrue(lnSegs.first().all { it.x > 0.0 })
    }

    // ==================== 3. EXPRESSION VALIDATION ====================

    @Test
    fun `invalid expressions are rejected with descriptive messages`() {
        val invalidCases = listOf(
            "",
            "   ",
            "y = ",
            "f(x) = ",
            "sin(",
            "(x + 1",
            "x + 1)",
            "unknown(x)",
            "y + 2",
            "x +",
            "1..2",
            "x = 2",
        )
        invalidCases.forEach { bad ->
            val res = validatePlotExpression(bad)
            assertTrue("Expected Invalid for '$bad', got $res", res is PlotValidationResult.Invalid)
            assertTrue((res as PlotValidationResult.Invalid).message.isNotBlank())
        }
    }

    // ==================== 4. ZOOM, PAN, LABELS & COORDINATE TRANSFORMS ====================

    @Test
    fun `zoom labels do not truncate fractional values and coordinate transforms round-trip`() {
        // Fractional zoom: 6 * 0.75 = 4.5 (previously truncated to 4 via toInt())
        val zoomed = GraphViewport().zoomedBy(0.75f)
        assertEquals(4.5f, zoomed.xHalfSpan, 1e-4f)
        assertEquals("4.5", formatAxisTick(zoomed.xMax()))
        assertEquals("-4.5", formatAxisTick(zoomed.xMin()))
        assertEquals("0", formatAxisTick(-0.00001f))

        // Nice tick step adapts to zoomed-in fractional spans
        assertTrue(niceTickStep(3.0f, targetTicks = 6) <= 0.5f)
        assertEquals(2f, niceTickStep(12f, targetTicks = 6), 1e-4f)

        // Coordinate round-trip with pan + zoom
        val panned = zoomed.pannedBy(dragDxPx = 100f, dragDyPx = -50f, widthPx = 900f)
        val w = 900f
        val h = 600f
        val screenPt = panned.worldToScreen(1.25, -0.75, w, h)
        val worldPt = panned.screenToWorld(screenPt.x, screenPt.y, w, h)
        assertEquals(1.25, worldPt.x, 1e-3)
        assertEquals(-0.75, worldPt.y, 1e-3)

        // Aspect ratio preserves 1:1 isotropic scale
        assertEquals(panned.xHalfSpan * (h / w), panned.yHalfSpan(w, h), 1e-4f)
    }
}
