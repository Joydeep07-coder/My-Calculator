package com.example

import com.example.model.NumberBase
import com.example.ui.screens.ProgrammerEvalResult
import com.example.ui.screens.WordSize
import com.example.ui.screens.appendProgrammerDigit
import com.example.ui.screens.backspaceProgrammerValue
import com.example.ui.screens.bitwiseNotProgrammerValue
import com.example.ui.screens.evaluateProgrammerOp
import com.example.ui.screens.formatBinaryNibbles
import com.example.ui.screens.formatProgrammerValue
import com.example.ui.screens.negateProgrammerValue
import com.example.ui.screens.normalizeProgrammerValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgrammerCalculatorUnitTest {

    // ==================== 1. FORMATTING: POSITIVE & NEGATIVE ACROSS ALL BASES ====================

    @Test
    fun `positive and zero values format accurately in HEX DEC OCT BIN`() {
        assertEquals("0", formatProgrammerValue(0L, NumberBase.HEX, WordSize.BIT_64, true))
        assertEquals("0", formatProgrammerValue(0L, NumberBase.DEC, WordSize.BIT_64, true))
        assertEquals("0", formatProgrammerValue(0L, NumberBase.OCT, WordSize.BIT_64, true))
        assertEquals("0000", formatProgrammerValue(0L, NumberBase.BIN, WordSize.BIT_64, true))

        assertEquals("2A", formatProgrammerValue(42L, NumberBase.HEX, WordSize.BIT_8, true))
        assertEquals("42", formatProgrammerValue(42L, NumberBase.DEC, WordSize.BIT_8, true))
        assertEquals("52", formatProgrammerValue(42L, NumberBase.OCT, WordSize.BIT_8, true))
        assertEquals("0010 1010", formatProgrammerValue(42L, NumberBase.BIN, WordSize.BIT_8, true))
    }

    @Test
    fun `negative numbers format as twos complement in BIN HEX OCT and signed in DEC`() {
        // -1 across 8-bit, 16-bit, 32-bit, 64-bit
        assertEquals("-1", formatProgrammerValue(-1L, NumberBase.DEC, WordSize.BIT_8, true))
        assertEquals("FF", formatProgrammerValue(-1L, NumberBase.HEX, WordSize.BIT_8, true))
        assertEquals("377", formatProgrammerValue(-1L, NumberBase.OCT, WordSize.BIT_8, true))
        assertEquals("1111 1111", formatBinaryNibbles(-1L, WordSize.BIT_8))

        assertEquals("FFFF", formatProgrammerValue(-1L, NumberBase.HEX, WordSize.BIT_16, true))
        assertEquals("177777", formatProgrammerValue(-1L, NumberBase.OCT, WordSize.BIT_16, true))
        assertEquals("1111 1111 1111 1111", formatBinaryNibbles(-1L, WordSize.BIT_16))

        assertEquals("FFFFFFFF", formatProgrammerValue(-1L, NumberBase.HEX, WordSize.BIT_32, true))
        assertEquals("37777777777", formatProgrammerValue(-1L, NumberBase.OCT, WordSize.BIT_32, true))
        assertEquals(
            List(8) { "1111" }.joinToString(" "),
            formatBinaryNibbles(-1L, WordSize.BIT_32),
        )

        assertEquals("-1", formatProgrammerValue(-1L, NumberBase.DEC, WordSize.BIT_64, true))
        assertEquals("FFFFFFFFFFFFFFFF", formatProgrammerValue(-1L, NumberBase.HEX, WordSize.BIT_64, true))
        assertEquals("1777777777777777777777", formatProgrammerValue(-1L, NumberBase.OCT, WordSize.BIT_64, true))
        assertEquals(
            List(16) { "1111" }.joinToString(" "),
            formatBinaryNibbles(-1L, WordSize.BIT_64),
        )

        // -5 in 8-bit and 64-bit
        assertEquals("FB", formatProgrammerValue(-5L, NumberBase.HEX, WordSize.BIT_8, true))
        assertEquals("373", formatProgrammerValue(-5L, NumberBase.OCT, WordSize.BIT_8, true))
        assertEquals("1111 1011", formatBinaryNibbles(-5L, WordSize.BIT_8))

        // Long.MIN_VALUE (64-bit signed minimum)
        assertEquals(
            "-9223372036854775808",
            formatProgrammerValue(Long.MIN_VALUE, NumberBase.DEC, WordSize.BIT_64, true),
        )
        assertEquals(
            "8000000000000000",
            formatProgrammerValue(Long.MIN_VALUE, NumberBase.HEX, WordSize.BIT_64, true),
        )
        assertEquals(
            "1000 0000 " + List(14) { "0000" }.joinToString(" "),
            formatBinaryNibbles(Long.MIN_VALUE, WordSize.BIT_64),
        )
    }

    // ==================== 2. WORD SIZE, SIGNED/UNSIGNED & OVERFLOW ====================

    @Test
    fun `word size normalization and signed unsigned representation`() {
        assertEquals(-1L, normalizeProgrammerValue(255L, WordSize.BIT_8, isSigned = true))
        assertEquals(255L, normalizeProgrammerValue(-1L, WordSize.BIT_8, isSigned = false))
        assertEquals("255", formatProgrammerValue(-1L, NumberBase.DEC, WordSize.BIT_8, isSigned = false))

        assertEquals(-32768L, normalizeProgrammerValue(32768L, WordSize.BIT_16, isSigned = true))
        assertEquals(65535L, normalizeProgrammerValue(-1L, WordSize.BIT_16, isSigned = false))
        assertEquals("65535", formatProgrammerValue(-1L, NumberBase.DEC, WordSize.BIT_16, isSigned = false))

        assertEquals(Int.MIN_VALUE.toLong(), normalizeProgrammerValue(0x80000000L, WordSize.BIT_32, isSigned = true))
        assertEquals(4294967295L, normalizeProgrammerValue(-1L, WordSize.BIT_32, isSigned = false))
        assertEquals("4294967295", formatProgrammerValue(-1L, NumberBase.DEC, WordSize.BIT_32, isSigned = false))

        assertEquals(
            "18446744073709551615",
            formatProgrammerValue(-1L, NumberBase.DEC, WordSize.BIT_64, isSigned = false),
        )
    }

    @Test
    fun `digit entry and backspace respect word size bounds and negative numbers`() {
        // 8-bit signed max is 127
        var v = 0L
        v = appendProgrammerDigit(v, '1', NumberBase.DEC, WordSize.BIT_8, true, false)!!
        v = appendProgrammerDigit(v, '2', NumberBase.DEC, WordSize.BIT_8, true, false)!!
        v = appendProgrammerDigit(v, '7', NumberBase.DEC, WordSize.BIT_8, true, false)!!
        assertEquals(127L, v)
        assertNull(appendProgrammerDigit(v, '0', NumberBase.DEC, WordSize.BIT_8, true, false))

        // 8-bit HEX entry FF becomes -1 in signed mode, rejects 3rd hex digit
        var h = 0L
        h = appendProgrammerDigit(h, 'F', NumberBase.HEX, WordSize.BIT_8, true, false)!!
        h = appendProgrammerDigit(h, 'F', NumberBase.HEX, WordSize.BIT_8, true, false)!!
        assertEquals(-1L, h)
        assertNull(appendProgrammerDigit(h, '0', NumberBase.HEX, WordSize.BIT_8, true, false))
        assertEquals(15L, backspaceProgrammerValue(h, NumberBase.HEX, WordSize.BIT_8, true))

        // Negative DEC backspace: -42 -> -4 -> 0
        val neg42 = -42L
        val neg4 = backspaceProgrammerValue(neg42, NumberBase.DEC, WordSize.BIT_64, true)
        assertEquals(-4L, neg4)
        assertEquals(0L, backspaceProgrammerValue(neg4, NumberBase.DEC, WordSize.BIT_64, true))

        // Negate 5 -> -5 -> 5
        assertEquals(-5L, negateProgrammerValue(5L, WordSize.BIT_32, true))
        assertEquals(5L, negateProgrammerValue(-5L, WordSize.BIT_32, true))
    }

    // ==================== 3. ARITHMETIC & OVERFLOW ====================

    private fun evalOk(
        a: Long,
        b: Long,
        op: String,
        wordSize: WordSize = WordSize.BIT_64,
        isSigned: Boolean = true,
    ): Long {
        val result = evaluateProgrammerOp(a, b, op, wordSize, isSigned)
        assertTrue("Expected Success for $a $op $b, got $result", result is ProgrammerEvalResult.Success)
        return (result as ProgrammerEvalResult.Success).value
    }

    @Test
    fun `arithmetic operations support positive negative overflow and safe division`() {
        assertEquals(15L, evalOk(10L, 5L, "+"))
        assertEquals(-5L, evalOk(10L, -15L, "+"))
        assertEquals(25L, evalOk(10L, -15L, "−"))
        assertEquals(-60L, evalOk(-12L, 5L, "×"))
        assertEquals(60L, evalOk(-12L, -5L, "×"))
        assertEquals(-4L, evalOk(-20L, 5L, "÷"))

        // 8-bit signed and unsigned overflow wrapping
        assertEquals(-128L, evalOk(127L, 1L, "+", WordSize.BIT_8, true))
        assertEquals(0L, evalOk(255L, 1L, "+", WordSize.BIT_8, false))
        assertEquals(255L, evalOk(0L, 1L, "−", WordSize.BIT_8, false))

        // 32-bit boundary overflow
        assertEquals(Int.MIN_VALUE.toLong(), evalOk(Int.MAX_VALUE.toLong(), 1L, "+", WordSize.BIT_32, true))
        assertEquals(Int.MAX_VALUE.toLong(), evalOk(Int.MIN_VALUE.toLong(), 1L, "−", WordSize.BIT_32, true))

        // 64-bit boundary overflow & unsigned division
        assertEquals(Long.MIN_VALUE, evalOk(Long.MAX_VALUE, 1L, "+", WordSize.BIT_64, true))
        assertEquals(Long.MAX_VALUE, evalOk(-1L, 2L, "÷", WordSize.BIT_64, isSigned = false))

        // Division by zero returns Error safely
        val divZero = evaluateProgrammerOp(42L, 0L, "÷", WordSize.BIT_64, true)
        assertTrue(divZero is ProgrammerEvalResult.Error)
    }

    // ==================== 4. BITWISE & SHIFT BOUNDARIES ====================

    @Test
    fun `bitwise NOT AND OR XOR honor word size and negative values`() {
        assertEquals(-1L, bitwiseNotProgrammerValue(0L, WordSize.BIT_8, true))
        assertEquals(255L, bitwiseNotProgrammerValue(0L, WordSize.BIT_8, false))
        assertEquals(-1L, bitwiseNotProgrammerValue(0L, WordSize.BIT_32, true))
        assertEquals(4294967295L, bitwiseNotProgrammerValue(0L, WordSize.BIT_32, false))
        assertEquals(-1L, bitwiseNotProgrammerValue(0L, WordSize.BIT_64, true))

        assertEquals(0x0AL, evalOk(0xFA, 0x0F, "AND", WordSize.BIT_8, false))
        assertEquals(0xFFL, evalOk(0xF0, 0x0F, "OR", WordSize.BIT_8, false))
        assertEquals(-1L, evalOk(0xF0, 0x0F, "XOR", WordSize.BIT_8, true))
    }

    @Test
    fun `shifts handle 64-bit counts negative operands and boundary saturation`() {
        // 64-bit shift >= 32 bits (previously clamped to 31)
        val shifted35 = evalOk(1L, 35L, "<<", WordSize.BIT_64, true)
        assertEquals(1L shl 35, shifted35)
        assertEquals(1L, evalOk(shifted35, 35L, ">>", WordSize.BIT_64, true))

        // Signed negative right shift preserves sign bit (arithmetic shift)
        assertEquals(-2L, evalOk(-4L, 1L, ">>", WordSize.BIT_64, true))
        assertEquals(-1L, evalOk(-1L, 5L, ">>", WordSize.BIT_8, true))
        assertEquals(-64L, evalOk(-128L, 1L, ">>", WordSize.BIT_8, true))

        // Unsigned right shift fills with 0 (logical shift)
        assertEquals(64L, evalOk(128L, 1L, ">>", WordSize.BIT_8, false))
        assertEquals(1L, evalOk(Long.MIN_VALUE, 63L, ">>", WordSize.BIT_64, false))

        // Shift >= wordSize.bits boundaries (no JVM mod-64 wraparound bug)
        assertEquals(0L, evalOk(1L, 8L, "<<", WordSize.BIT_8, false))
        assertEquals(0L, evalOk(1L, 32L, "<<", WordSize.BIT_32, true))
        assertEquals(0L, evalOk(1L, 64L, "<<", WordSize.BIT_64, true))
        assertEquals(0L, evalOk(255L, 8L, ">>", WordSize.BIT_8, false))
        assertEquals(-1L, evalOk(-5L, 8L, ">>", WordSize.BIT_8, true))
        assertEquals(-1L, evalOk(-5L, 64L, ">>", WordSize.BIT_64, true))

        // Negative shift amount is safely rejected
        assertTrue(evaluateProgrammerOp(8L, -1L, "<<", WordSize.BIT_64, true) is ProgrammerEvalResult.Error)
        assertTrue(evaluateProgrammerOp(8L, -2L, ">>", WordSize.BIT_64, true) is ProgrammerEvalResult.Error)
    }
}
