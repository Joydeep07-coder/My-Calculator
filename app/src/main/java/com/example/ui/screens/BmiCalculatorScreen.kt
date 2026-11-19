package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.SampleCatalogs
import com.example.ui.components.OmniCard
import com.example.ui.components.OmniInputField
import com.example.ui.components.OmniTopBar
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCoral
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.JetBrainsMonoFontFamily

@Composable
fun BmiCalculatorScreen(
    onBack: () -> Unit,
) {
    var gender by remember { mutableStateOf("Unspecified") }
    var useMetric by remember { mutableStateOf(true) }
    var heightInput by remember { mutableStateOf("") }
    var weightInput by remember { mutableStateOf("") }
    var computedBmi by remember { mutableStateOf<Double?>(null) }
    var computedMinHealthyKg by remember { mutableStateOf(0.0) }
    var computedMaxHealthyKg by remember { mutableStateOf(0.0) }

    fun recalculate() {
        val rawH = heightInput.toDoubleOrNull() ?: 0.0
        val rawW = weightInput.toDoubleOrNull() ?: 0.0
        val heightMeters = if (useMetric) rawH / 100.0 else rawH * 0.0254
        val weightKg = if (useMetric) rawW else rawW * 0.45359237
        if (heightMeters > 0.3 && weightKg > 1.0) {
            computedBmi = weightKg / (heightMeters * heightMeters)
            computedMinHealthyKg = 18.5 * heightMeters * heightMeters
            computedMaxHealthyKg = 24.9 * heightMeters * heightMeters
        }
    }

    val bmiValue = computedBmi ?: 0.0
    val (categoryLabel, categoryColor) = when {
        bmiValue < 18.5 -> "Underweight" to AccentCyan
        bmiValue < 25.0 -> "Normal weight" to AccentEmerald
        bmiValue < 30.0 -> "Overweight" to AccentAmber
        else -> "Obesity" to AccentCoral
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            OmniTopBar(
                title = "BMI Calculator",
                subtitle = "Body mass index & healthy scale",
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
                        text = "Profile (Optional)",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Male", "Female", "Unspecified").forEach { option ->
                            FilterChip(
                                selected = gender == option,
                                onClick = { gender = option },
                                label = { Text(option) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                ),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Measurement Units",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = useMetric,
                            onClick = {
                                if (!useMetric) {
                                    useMetric = true
                                    heightInput = ""
                                    weightInput = ""
                                    computedBmi = null
                                }
                            },
                            label = { Text("Metric (cm / kg)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                        )
                        FilterChip(
                            selected = !useMetric,
                            onClick = {
                                if (useMetric) {
                                    useMetric = false
                                    heightInput = ""
                                    weightInput = ""
                                    computedBmi = null
                                }
                            },
                            label = { Text("Imperial (in / lbs)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    OmniInputField(
                        value = heightInput,
                        onValueChange = { heightInput = it },
                        label = if (useMetric) "Height (cm)" else "Height (inches)",
                        suffixText = if (useMetric) "cm" else "in",
                        testTag = "bmi_height_input",
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OmniInputField(
                        value = weightInput,
                        onValueChange = { weightInput = it },
                        label = if (useMetric) "Weight (kg)" else "Weight (lbs)",
                        suffixText = if (useMetric) "kg" else "lbs",
                        testTag = "bmi_weight_input",
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { recalculate() },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("bmi_calculate_button"),
                    ) {
                        Text("Calculate BMI")
                    }
                }

                if (computedBmi == null) {
                    OmniCard {
                        Text(
                            text = "No BMI result yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Enter your height and weight above and tap Calculate BMI.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    // Visual Result Card
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        border = BorderStroke(1.dp, categoryColor.copy(alpha = 0.55f)),
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column {
                                    Text(
                                        text = "Your BMI Score",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text = SampleCatalogs.formatNumber(bmiValue, 1, false),
                                        style = MaterialTheme.typography.displayLarge.copy(
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.testTag("bmi_result_value"),
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = categoryColor.copy(alpha = 0.18f),
                                    border = BorderStroke(1.dp, categoryColor.copy(alpha = 0.5f)),
                                ) {
                                    Text(
                                        text = categoryLabel,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = categoryColor,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Visual BMI Scale
                            BmiVisualScale(bmi = bmiValue)

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = "Healthy Weight Range",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = "${SampleCatalogs.formatNumber(computedMinHealthyKg, 1, false)} – ${SampleCatalogs.formatNumber(computedMaxHealthyKg, 1, false)} kg",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontFamily = JetBrainsMonoFontFamily
                                    ),
                                    color = AccentEmerald,
                                )
                            }
                        }
                    }
                }

                // Category Legend
                OmniCard {
                    Text(
                        text = "BMI Categories",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    val bands = listOf(
                        Triple("Underweight", "< 18.5", AccentCyan),
                        Triple("Normal weight", "18.5 – 24.9", AccentEmerald),
                        Triple("Overweight", "25.0 – 29.9", AccentAmber),
                        Triple("Obesity", "≥ 30.0", AccentCoral),
                    )
                    bands.forEach { (name, range, color) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(name, style = MaterialTheme.typography.bodyMedium, color = color)
                            Text(
                                text = range,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = JetBrainsMonoFontFamily
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun BmiVisualScale(bmi: Double) {
    val markerColor = MaterialTheme.colorScheme.onSurface
    Column {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp),
        ) {
            val barTop = 12.dp.toPx()
            val barH = 12.dp.toPx()
            val totalW = size.width
            val segments = listOf(
                0.22f to AccentCyan,
                0.28f to AccentEmerald,
                0.24f to AccentAmber,
                0.26f to AccentCoral,
            )
            var currentX = 0f
            val gap = 3.dp.toPx()
            segments.forEach { (frac, color) ->
                val segW = (totalW - gap * 3) * frac
                drawRoundRect(
                    color = color,
                    topLeft = Offset(currentX, barTop),
                    size = Size(segW, barH),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                )
                currentX += segW + gap
            }

            // Map BMI 12..40 onto 0..totalW
            val normalized = ((bmi - 12.0) / (40.0 - 12.0)).toFloat().coerceIn(0.02f, 0.98f)
            val markerX = normalized * totalW
            drawCircle(
                color = Color.White,
                radius = 7.dp.toPx(),
                center = Offset(markerX, barTop + barH / 2f),
            )
            drawCircle(
                color = markerColor,
                radius = 4.dp.toPx(),
                center = Offset(markerX, barTop + barH / 2f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            listOf("12", "18.5", "25", "30", "40").forEach { tick ->
                Text(
                    text = tick,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = JetBrainsMonoFontFamily
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
