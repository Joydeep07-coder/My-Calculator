package com.example

import com.example.ui.screens.BmiCategory
import com.example.ui.screens.BmiValidationResult
import com.example.ui.screens.CalendarDate
import com.example.ui.screens.calculateBmi
import com.example.ui.screens.classifyBmi
import com.example.ui.screens.computeAgeDetails
import com.example.ui.screens.computeAgeDetailsFromUtcMillis
import com.example.ui.screens.daysInMonth
import com.example.ui.screens.isLeapYear
import com.example.ui.screens.isValidCalendarDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.TimeZone

class AgeAndBmiCalculatorUnitTest {

    // ==================== AGE CALCULATOR TESTS ====================

    @Test
    fun `age calculation computes exact years months days and totals`() {
        val dob = CalendarDate(1995, 6, 15)
        val today = CalendarDate(2025, 11, 20)
        val details = computeAgeDetails(dob = dob, today = today)

        assertNotNull(details)
        details!!
        assertEquals(30, details.years)
        assertEquals(5, details.months)
        assertEquals(5, details.days)
        assertEquals(365L, details.totalMonths)
        assertEquals(11116L, details.totalDays)
        assertEquals(11116L / 7L, details.totalWeeks)
        assertEquals(CalendarDate(2026, 6, 15).toUtcMillis(), details.nextBirthdayUtcMillis)
        assertEquals(207L, details.daysUntilNextBirthday)
    }

    @Test
    fun `age calculation handles same-day birth and one-day-old boundaries`() {
        val today = CalendarDate(2025, 3, 10)
        val newborn = computeAgeDetails(dob = today, today = today)!!
        assertEquals(0, newborn.years)
        assertEquals(0, newborn.months)
        assertEquals(0, newborn.days)
        assertEquals(0L, newborn.totalMonths)
        assertEquals(0L, newborn.totalWeeks)
        assertEquals(0L, newborn.totalDays)
        assertEquals(0L, newborn.daysUntilNextBirthday)

        val oneDayOld = computeAgeDetails(dob = CalendarDate(2025, 3, 9), today = today)!!
        assertEquals(0, oneDayOld.years)
        assertEquals(0, oneDayOld.months)
        assertEquals(1, oneDayOld.days)
        assertEquals(1L, oneDayOld.totalDays)
        assertEquals(364L, oneDayOld.daysUntilNextBirthday)
    }

    @Test
    fun `age calculation handles month-end boundaries without negative days`() {
        // Jan 31 -> Feb 28 (non-leap year): 0y 0m 28d
        val jan31ToFeb28 = computeAgeDetails(
            dob = CalendarDate(2021, 1, 31),
            today = CalendarDate(2021, 2, 28),
        )!!
        assertEquals(0, jan31ToFeb28.years)
        assertEquals(0, jan31ToFeb28.months)
        assertEquals(28, jan31ToFeb28.days)

        // Jan 31 -> Mar 1 (non-leap year): 0y 1m 1d (anchored at Feb 28)
        val jan31ToMar1NonLeap = computeAgeDetails(
            dob = CalendarDate(2021, 1, 31),
            today = CalendarDate(2021, 3, 1),
        )!!
        assertEquals(0, jan31ToMar1NonLeap.years)
        assertEquals(1, jan31ToMar1NonLeap.months)
        assertEquals(1, jan31ToMar1NonLeap.days)
        assertEquals(29L, jan31ToMar1NonLeap.totalDays)

        // Jan 31 -> Mar 1 (leap year 2020): 0y 1m 1d (anchored at Feb 29), 30 total days
        val jan31ToMar1Leap = computeAgeDetails(
            dob = CalendarDate(2020, 1, 31),
            today = CalendarDate(2020, 3, 1),
        )!!
        assertEquals(0, jan31ToMar1Leap.years)
        assertEquals(1, jan31ToMar1Leap.months)
        assertEquals(1, jan31ToMar1Leap.days)
        assertEquals(30L, jan31ToMar1Leap.totalDays)
    }

    @Test
    fun `age calculation handles leap years and Feb 29 birthdays`() {
        assertTrue(isLeapYear(2000))
        assertTrue(isLeapYear(2024))
        assertFalse(isLeapYear(1900))
        assertFalse(isLeapYear(2025))
        assertEquals(29, daysInMonth(2024, 2))
        assertEquals(28, daysInMonth(2025, 2))

        val feb29Birth = CalendarDate(2000, 2, 29)

        // Exact leap-day birthday in 2024
        val onLeapBirthday = computeAgeDetails(dob = feb29Birth, today = CalendarDate(2024, 2, 29))!!
        assertEquals(24, onLeapBirthday.years)
        assertEquals(0, onLeapBirthday.months)
        assertEquals(0, onLeapBirthday.days)
        assertEquals(0L, onLeapBirthday.daysUntilNextBirthday)
        assertEquals(CalendarDate(2024, 2, 29).toUtcMillis(), onLeapBirthday.nextBirthdayUtcMillis)

        // Non-leap year Feb 28 (celebrated on Feb 28 -> 0 days left)
        val onNonLeapFeb28 = computeAgeDetails(dob = feb29Birth, today = CalendarDate(2025, 2, 28))!!
        assertEquals(24, onNonLeapFeb28.years)
        assertEquals(11, onNonLeapFeb28.months)
        assertEquals(30, onNonLeapFeb28.days)
        assertEquals(0L, onNonLeapFeb28.daysUntilNextBirthday)
        assertEquals(CalendarDate(2025, 2, 28).toUtcMillis(), onNonLeapFeb28.nextBirthdayUtcMillis)

        // Non-leap year Mar 1 -> 25y 0m 1d, next birthday Feb 28, 2026
        val onNonLeapMar1 = computeAgeDetails(dob = feb29Birth, today = CalendarDate(2025, 3, 1))!!
        assertEquals(25, onNonLeapMar1.years)
        assertEquals(0, onNonLeapMar1.months)
        assertEquals(1, onNonLeapMar1.days)
        assertEquals(364L, onNonLeapMar1.daysUntilNextBirthday)
        assertEquals(CalendarDate(2026, 2, 28).toUtcMillis(), onNonLeapMar1.nextBirthdayUtcMillis)

        // 2027-03-01 -> next birthday falls in leap year 2028 on Feb 29 (365 days later)
        val beforeLeapYear = computeAgeDetails(dob = feb29Birth, today = CalendarDate(2027, 3, 1))!!
        assertEquals(CalendarDate(2028, 2, 29).toUtcMillis(), beforeLeapYear.nextBirthdayUtcMillis)
        assertEquals(365L, beforeLeapYear.daysUntilNextBirthday)
    }

    @Test
    fun `utc millis conversion has no off-by-one-day bug across timezones`() {
        val previousTz = TimeZone.getDefault()
        try {
            // Negative UTC offset where UTC midnight would be previous evening in local time
            TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"))
            val dobUtcMillis = CalendarDate(2000, 1, 1).toUtcMillis()
            val roundTripped = CalendarDate.fromUtcMillis(dobUtcMillis)
            assertEquals(CalendarDate(2000, 1, 1), roundTripped)

            val details = computeAgeDetailsFromUtcMillis(
                dobUtcMillis = dobUtcMillis,
                today = CalendarDate(2025, 1, 1),
            )!!
            assertEquals(25, details.years)
            assertEquals(0, details.months)
            assertEquals(0, details.days)
            assertEquals(0L, details.daysUntilNextBirthday)

            // Positive UTC offset as well
            TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tokyo"))
            assertEquals(CalendarDate(2000, 1, 1), CalendarDate.fromUtcMillis(dobUtcMillis))
        } finally {
            TimeZone.setDefault(previousTz)
        }
    }

    @Test
    fun `future and invalid calendar dates are rejected`() {
        val today = CalendarDate(2025, 5, 10)
        assertNull(computeAgeDetails(dob = CalendarDate(2025, 5, 11), today = today))
        assertNull(computeAgeDetails(dob = CalendarDate(2026, 1, 1), today = today))

        assertFalse(isValidCalendarDate(CalendarDate(2023, 2, 29)))
        assertFalse(isValidCalendarDate(CalendarDate(2024, 4, 31)))
        assertFalse(isValidCalendarDate(CalendarDate(2024, 13, 1)))
        assertFalse(isValidCalendarDate(CalendarDate(2024, 0, 10)))
        assertNull(computeAgeDetails(dob = CalendarDate(2023, 2, 29), today = today))
    }

    // ==================== BMI CALCULATOR TESTS ====================

    @Test
    fun `metric bmi and healthy weight range calculation`() {
        val outcome = calculateBmi(heightInput = "175", weightInput = "70", useMetric = true)
        assertTrue(outcome is BmiValidationResult.Valid)
        val result = (outcome as BmiValidationResult.Valid).result

        assertEquals(22.857142857, result.bmi, 1e-6)
        assertEquals(BmiCategory.NORMAL, result.category)
        assertEquals(56.65625, result.minHealthyWeight, 1e-4)
        assertEquals(76.25625, result.maxHealthyWeight, 1e-4)
        assertEquals("kg", result.weightUnitLabel)
    }

    @Test
    fun `imperial bmi converts accurately and returns healthy weight in lbs`() {
        // 68.8976378 in == 175 cm; 154.3235835 lbs == 70 kg
        val outcome = calculateBmi(heightInput = "69", weightInput = "154", useMetric = false)
        assertTrue(outcome is BmiValidationResult.Valid)
        val result = (outcome as BmiValidationResult.Valid).result

        // Standard Imperial formula: weight_lb * 703.06957964 / height_in^2 (exact SI equivalents)
        val expectedBmi = (154.0 * 0.45359237) / ((69.0 * 0.0254) * (69.0 * 0.0254))
        assertEquals(22.741591, expectedBmi, 1e-6)
        assertEquals(expectedBmi, result.bmi, 1e-6)
        assertEquals(BmiCategory.NORMAL, result.category)
        assertEquals("lbs", result.weightUnitLabel)

        // Verify healthy range in lbs matches metric conversion of min/max kg
        val heightMeters = 69.0 * 0.0254
        val expectedMinLbs = (18.5 * heightMeters * heightMeters) / 0.45359237
        val expectedMaxLbs = (24.9 * heightMeters * heightMeters) / 0.45359237
        assertEquals(expectedMinLbs, result.minHealthyWeight, 1e-4)
        assertEquals(expectedMaxLbs, result.maxHealthyWeight, 1e-4)
        assertTrue(result.minHealthyWeight in 120.0..135.0)
        assertTrue(result.maxHealthyWeight in 160.0..175.0)
    }

    @Test
    fun `bmi category boundaries classify accurately`() {
        assertEquals(BmiCategory.UNDERWEIGHT, classifyBmi(18.49))
        assertEquals(BmiCategory.NORMAL, classifyBmi(18.5))
        assertEquals(BmiCategory.NORMAL, classifyBmi(24.99))
        assertEquals(BmiCategory.OVERWEIGHT, classifyBmi(25.0))
        assertEquals(BmiCategory.OVERWEIGHT, classifyBmi(29.99))
        assertEquals(BmiCategory.OBESITY, classifyBmi(30.0))
        assertEquals(BmiCategory.OBESITY, classifyBmi(42.0))
    }

    @Test
    fun `bmi validation rejects empty zero negative non-numeric and out-of-range inputs`() {
        assertTrue(calculateBmi("", "70", true) is BmiValidationResult.Invalid)
        assertTrue(calculateBmi("175", "", true) is BmiValidationResult.Invalid)
        assertTrue(calculateBmi("abc", "70", true) is BmiValidationResult.Invalid)
        assertTrue(calculateBmi("0", "70", true) is BmiValidationResult.Invalid)
        assertTrue(calculateBmi("175", "0", true) is BmiValidationResult.Invalid)
        assertTrue(calculateBmi("-175", "70", true) is BmiValidationResult.Invalid)
        assertTrue(calculateBmi("175", "-70", true) is BmiValidationResult.Invalid)
        assertTrue(calculateBmi("10", "70", true) is BmiValidationResult.Invalid)
        assertTrue(calculateBmi("175", "0.5", true) is BmiValidationResult.Invalid)
        assertTrue(calculateBmi("5", "150", false) is BmiValidationResult.Invalid)
        assertTrue(calculateBmi("69", "1", false) is BmiValidationResult.Invalid)
    }
}
