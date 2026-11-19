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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.OmniCard
import com.example.ui.components.OmniTopBar
import com.example.ui.theme.AccentPink
import com.example.ui.theme.JetBrainsMonoFontFamily
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgeCalculatorScreen(
    onBack: () -> Unit,
) {
    var selectedDobMillis by remember { mutableStateOf<Long?>(null) }
    var calculatedDobMillis by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val weekdayFormatter = remember { SimpleDateFormat("EEEE", Locale.getDefault()) }

    val ageBreakdown = remember(calculatedDobMillis) {
        calculatedDobMillis?.let { computeAgeDetails(it) }
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
                        onClick = { calculatedDobMillis = selectedDobMillis },
                        enabled = selectedDobMillis != null,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("age_calculate_button"),
                    ) {
                        Text("Calculate Age")
                    }
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
                                    modifier = Modifier.weight(1f),
                                )
                                AgeUnitBlock(
                                    value = ageBreakdown.months.toString(),
                                    label = "Months",
                                    modifier = Modifier.weight(1f),
                                )
                                AgeUnitBlock(
                                    value = ageBreakdown.days.toString(),
                                    label = "Days",
                                    modifier = Modifier.weight(1f),
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
                                    text = "${dateFormatter.format(Date(ageBreakdown.nextBirthdayMillis))} (${weekdayFormatter.format(Date(ageBreakdown.nextBirthdayMillis))})",
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
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDobMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            selectedDobMillis = it
                            calculatedDobMillis = it
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

private data class AgeDetails(
    val years: Int,
    val months: Int,
    val days: Int,
    val totalMonths: Long,
    val totalWeeks: Long,
    val totalDays: Long,
    val nextBirthdayMillis: Long,
    val daysUntilNextBirthday: Long,
)

private fun computeAgeDetails(dobMillis: Long): AgeDetails {
    val dob = Calendar.getInstance().apply { timeInMillis = dobMillis }
    val today = Calendar.getInstance()

    if (dob.after(today)) {
        return AgeDetails(0, 0, 0, 0, 0, 0, dobMillis, 0)
    }

    var years = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
    var months = today.get(Calendar.MONTH) - dob.get(Calendar.MONTH)
    var days = today.get(Calendar.DAY_OF_MONTH) - dob.get(Calendar.DAY_OF_MONTH)

    if (days < 0) {
        months -= 1
        val prevMonth = (today.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
        days += prevMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
    }
    if (months < 0) {
        years -= 1
        months += 12
    }

    val diffMillis = (today.timeInMillis - dob.timeInMillis).coerceAtLeast(0L)
    val totalDays = TimeUnit.MILLISECONDS.toDays(diffMillis)
    val totalWeeks = totalDays / 7L
    val totalMonths = years * 12L + months

    val nextBday = Calendar.getInstance().apply {
        set(Calendar.YEAR, today.get(Calendar.YEAR))
        set(Calendar.MONTH, dob.get(Calendar.MONTH))
        set(Calendar.DAY_OF_MONTH, dob.get(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val todayStart = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    if (nextBday.before(todayStart)) {
        nextBday.add(Calendar.YEAR, 1)
    }
    val daysUntil = TimeUnit.MILLISECONDS.toDays(
        (nextBday.timeInMillis - todayStart.timeInMillis).coerceAtLeast(0L)
    )

    return AgeDetails(
        years = years,
        months = months,
        days = days,
        totalMonths = totalMonths,
        totalWeeks = totalWeeks,
        totalDays = totalDays,
        nextBirthdayMillis = nextBday.timeInMillis,
        daysUntilNextBirthday = daysUntil,
    )
}
