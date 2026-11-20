package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.ErrorStateBanner
import com.example.ui.components.OmniCard
import com.example.ui.components.OmniTopBar
import com.example.ui.theme.AccentPink
import com.example.ui.theme.JetBrainsMonoFontFamily
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgeCalculatorScreen(
    onBack: () -> Unit,
) {
    var selectedDobMillis by remember { mutableStateOf<Long?>(null) }
    var calculatedDobMillis by remember { mutableStateOf<Long?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    val dateFormatter = remember {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }
    val weekdayFormatter = remember {
        SimpleDateFormat("EEEE", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }

    val ageBreakdown = remember(calculatedDobMillis) {
        calculatedDobMillis?.let { computeAgeDetailsFromUtcMillis(it) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            OmniTopBar(
                title = "Age Calculator",
                subtitle = "Exact age, milestones & birthday countdown",
                onBack = onBack,
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 640.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                OmniCard {
                    Text(
                        text = "Date of Birth",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { showDatePicker = true }
                            .testTag("age_dob_selector"),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                val dob = selectedDobMillis
                                Text(
                                    text = if (dob != null) dateFormatter.format(Date(dob)) else "Select Date of Birth",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontFamily = JetBrainsMonoFontFamily
                                    ),
                                    color = if (dob != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = if (dob != null) {
                                        "Born on ${weekdayFormatter.format(Date(dob))}"
                                    } else {
                                        "Tap to choose from calendar"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Icon(
                                imageVector = Icons.Rounded.CalendarMonth,
                                contentDescription = "Pick date of birth",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            val dob = selectedDobMillis
                            if (dob == null) {
                                errorMessage = "Please select your date of birth first."
                                calculatedDobMillis = null
                            } else {
                                val details = computeAgeDetailsFromUtcMillis(dob)
                                if (details == null) {
                                    errorMessage = "Birth date cannot be in the future."
                                    calculatedDobMillis = null
                                } else {
                                    errorMessage = null
                                    calculatedDobMillis = dob
                                }
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("age_calculate_button"),
                    ) {
                        Text("Calculate Age")
                    }
                }

                if (!errorMessage.isNullOrBlank()) {
                    ErrorStateBanner(
                        message = errorMessage!!,
                        modifier = Modifier.testTag("age_error_banner"),
                    )
                }

                if (ageBreakdown == null) {
                    OmniCard {
                        Text(
                            text = "No age calculated yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Pick your date of birth above to see your exact age and next birthday countdown.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    // Attractive Age Result Card
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.52f),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                        ),
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "Current Age",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                AgeUnitBlock(
                                    value = ageBreakdown.years.toString(),
                                    label = "Years",
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("age_years_value"),
                                )
                                AgeUnitBlock(
                                    value = ageBreakdown.months.toString(),
                                    label = "Months",
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("age_months_value"),
                                )
                                AgeUnitBlock(
                                    value = ageBreakdown.days.toString(),
                                    label = "Days",
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("age_days_value"),
                                )
                            }
                        }
                    }

                    // Summary Metrics Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        SummaryMetricCard(
                            label = "Total Months",
                            value = ageBreakdown.totalMonths.toString(),
                            modifier = Modifier.weight(1f),
                        )
                        SummaryMetricCard(
                            label = "Total Weeks",
                            value = ageBreakdown.totalWeeks.toString(),
                            modifier = Modifier.weight(1f),
                        )
                        SummaryMetricCard(
                            label = "Total Days",
                            value = ageBreakdown.totalDays.toString(),
                            modifier = Modifier.weight(1f),
                        )
                    }

                    // Next Birthday Card
                    OmniCard(
                        borderColor = AccentPink.copy(alpha = 0.45f),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(AccentPink.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Cake,
                                    contentDescription = "Next birthday",
                                    tint = AccentPink,
                                    modifier = Modifier.size(24.dp),
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Next Birthday",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = "${dateFormatter.format(Date(ageBreakdown.nextBirthdayUtcMillis))} (${weekdayFormatter.format(Date(ageBreakdown.nextBirthdayUtcMillis))})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = AccentPink.copy(alpha = 0.18f),
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Text(
                                        text = "${ageBreakdown.daysUntilNextBirthday}",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                        ),
                                        color = AccentPink,
                                        modifier = Modifier.testTag("age_next_birthday_days"),
                                    )
                                    Text(
                                        text = "days left",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AccentPink,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val todayLocal = currentLocalCalendarDate()
        val todayUtcMillis = todayLocal.toUtcMillis()
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDobMillis ?: todayUtcMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis <= todayUtcMillis
                }

                override fun isSelectableYear(year: Int): Boolean {
                    return year <= todayLocal.year
                }
            },
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { picked ->
                            selectedDobMillis = picked
                            val details = computeAgeDetailsFromUtcMillis(picked)
                            if (details == null) {
                                errorMessage = "Birth date cannot be in the future."
                                calculatedDobMillis = null
                            } else {
                                errorMessage = null
                                calculatedDobMillis = picked
                            }
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun AgeUnitBlock(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.displayMedium.copy(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                ),
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SummaryMetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    OmniCard(
        modifier = modifier,
        contentPadding = PaddingValues(14.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold,
            ),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

data class CalendarDate(
    val year: Int,
    val month: Int, // 1..12
    val day: Int,   // 1..31
) : Comparable<CalendarDate> {

    override fun compareTo(other: CalendarDate): Int {
        if (year != other.year) return year.compareTo(other.year)
        if (month != other.month) return month.compareTo(other.month)
        return day.compareTo(other.day)
    }

    fun toUtcMillis(): Long {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
        }
        return cal.timeInMillis
    }

    companion object {
        fun fromUtcMillis(utcMillis: Long): CalendarDate {
            val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                timeInMillis = utcMillis
            }
            return CalendarDate(
                year = cal.get(Calendar.YEAR),
                month = cal.get(Calendar.MONTH) + 1,
                day = cal.get(Calendar.DAY_OF_MONTH),
            )
        }
    }
}

data class AgeDetails(
    val years: Int,
    val months: Int,
    val days: Int,
    val totalMonths: Long,
    val totalWeeks: Long,
    val totalDays: Long,
    val nextBirthdayUtcMillis: Long,
    val daysUntilNextBirthday: Long,
)

fun isLeapYear(year: Int): Boolean =
    (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)

fun daysInMonth(year: Int, month: Int): Int = when (month) {
    1, 3, 5, 7, 8, 10, 12 -> 31
    4, 6, 9, 11 -> 30
    2 -> if (isLeapYear(year)) 29 else 28
    else -> 0
}

fun isValidCalendarDate(date: CalendarDate): Boolean {
    if (date.month !in 1..12) return false
    val maxDay = daysInMonth(date.year, date.month)
    return date.day in 1..maxDay
}

private fun currentLocalCalendarDate(): CalendarDate {
    val now = Calendar.getInstance()
    return CalendarDate(
        year = now.get(Calendar.YEAR),
        month = now.get(Calendar.MONTH) + 1,
        day = now.get(Calendar.DAY_OF_MONTH),
    )
}

/**
 * Pure Gregorian epoch day count (Howard Hinnant algorithm) — timezone/DST independent.
 */
private fun toEpochDay(date: CalendarDate): Long {
    val y = date.year.toLong() - if (date.month <= 2) 1L else 0L
    val era = (if (y >= 0) y else y - 399L) / 400L
    val yoe = y - era * 400L
    val m = date.month.toLong()
    val doy = (153L * (m + if (m > 2) -3L else 9L) + 2L) / 5L + date.day.toLong() - 1L
    val doe = yoe * 365L + yoe / 4L - yoe / 100L + doy
    return era * 146097L + doe - 719468L
}

private fun addMonthsClamped(date: CalendarDate, monthsToAdd: Int): CalendarDate {
    val zeroBasedMonth = (date.month - 1) + monthsToAdd
    val targetYear = date.year + Math.floorDiv(zeroBasedMonth, 12)
    val targetMonth = Math.floorMod(zeroBasedMonth, 12) + 1
    val targetDay = min(date.day, daysInMonth(targetYear, targetMonth))
    return CalendarDate(targetYear, targetMonth, targetDay)
}

fun computeAgeDetailsFromUtcMillis(
    dobUtcMillis: Long,
    today: CalendarDate = currentLocalCalendarDate(),
): AgeDetails? = computeAgeDetails(CalendarDate.fromUtcMillis(dobUtcMillis), today)

fun computeAgeDetails(
    dob: CalendarDate,
    today: CalendarDate = currentLocalCalendarDate(),
): AgeDetails? {
    if (!isValidCalendarDate(dob) || !isValidCalendarDate(today)) return null
    if (dob > today) return null

    var totalMonths = (today.year - dob.year) * 12 + (today.month - dob.month)
    var days = today.day - dob.day
    if (totalMonths > 0 && days < 0) {
        totalMonths -= 1
        val anchor = addMonthsClamped(dob, totalMonths)
        days = (toEpochDay(today) - toEpochDay(anchor)).toInt()
    }

    val years = totalMonths / 12
    val months = totalMonths % 12

    val todayEpoch = toEpochDay(today)
    val dobEpoch = toEpochDay(dob)
    val totalDays = (todayEpoch - dobEpoch).coerceAtLeast(0L)
    val totalWeeks = totalDays / 7L

    val candidateThisYear = CalendarDate(
        year = today.year,
        month = dob.month,
        day = min(dob.day, daysInMonth(today.year, dob.month)),
    )
    val nextBirthdayDate = if (candidateThisYear < today) {
        val nextYear = today.year + 1
        CalendarDate(
            year = nextYear,
            month = dob.month,
            day = min(dob.day, daysInMonth(nextYear, dob.month)),
        )
    } else {
        candidateThisYear
    }
    val daysUntil = (toEpochDay(nextBirthdayDate) - todayEpoch).coerceAtLeast(0L)

    return AgeDetails(
        years = years,
        months = months,
        days = days,
        totalMonths = totalMonths.toLong(),
        totalWeeks = totalWeeks,
        totalDays = totalDays,
        nextBirthdayUtcMillis = nextBirthdayDate.toUtcMillis(),
        daysUntilNextBirthday = daysUntil,
    )
}
