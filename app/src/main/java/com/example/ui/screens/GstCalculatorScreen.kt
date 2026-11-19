package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.SampleCatalogs
import com.example.navigation.AppRoutes
import com.example.state.OmniCalcViewModel
import com.example.ui.components.OmniCard
import com.example.ui.components.OmniInputField
import com.example.ui.components.OmniResultCard
import com.example.ui.components.OmniTopBar
import kotlinx.coroutines.launch

@Composable
fun GstCalculatorScreen(
    viewModel: OmniCalcViewModel,
    onBack: () -> Unit,
) {
    var amountInput by remember { mutableStateOf("") }
    var isAddGst by remember { mutableStateOf(true) }
    var selectedPreset by remember { mutableStateOf(18) } // -1 means Custom
    var customRateInput by remember { mutableStateOf("") }
    var isInterState by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val amount = amountInput.toDoubleOrNull() ?: 0.0
    val gstRate = if (selectedPreset == -1) {
        (customRateInput.toDoubleOrNull() ?: 0.0).coerceIn(0.0, 100.0)
    } else {
        selectedPreset.toDouble()
    }

    val originalAmount = if (isAddGst) {
        amount
    } else {
        if (gstRate == 0.0) amount else amount * 100.0 / (100.0 + gstRate)
    }
    val gstAmount = if (isAddGst) {
        amount * (gstRate / 100.0)
    } else {
        amount - originalAmount
    }
    val finalAmount = if (isAddGst) originalAmount + gstAmount else amount
    val halfGst = gstAmount / 2.0

    fun fmt(v: Double) = "₹" + SampleCatalogs.formatNumber(v, precision = 2, useGrouping = true)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            OmniTopBar(
                title = "GST Calculator",
                subtitle = "Indian CGST, SGST & IGST breakdown",
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
                // Mode Toggle: Add GST vs Remove GST
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                    ) {
                        GstModeSegment(
                            label = "Add GST (+)",
                            selected = isAddGst,
                            onClick = { isAddGst = true },
                            modifier = Modifier.weight(1f),
                            testTag = "gst_mode_add",
                        )
                        GstModeSegment(
                            label = "Remove GST (−)",
                            selected = !isAddGst,
                            onClick = { isAddGst = false },
                            modifier = Modifier.weight(1f),
                            testTag = "gst_mode_remove",
                        )
                    }
                }

                // Amount Input
                OmniCard {
                    OmniInputField(
                        value = amountInput,
                        onValueChange = { amountInput = it },
                        label = if (isAddGst) "Net Amount (Excl. GST)" else "Gross Amount (Incl. GST)",
                        prefixText = "₹",
                        testTag = "gst_amount_input",
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "GST Slab Rate",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val presets = listOf(5 to "5%", 12 to "12%", 18 to "18%", 28 to "28%", -1 to "Custom")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        presets.forEach { (rate, label) ->
                            FilterChip(
                                selected = selectedPreset == rate,
                                onClick = { selectedPreset = rate },
                                label = { Text(label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                ),
                                modifier = Modifier.testTag("gst_preset_$label"),
                            )
                        }
                    }

                    AnimatedVisibility(visible = selectedPreset == -1) {
                        Column {
                            Spacer(modifier = Modifier.height(12.dp))
                            OmniInputField(
                                value = customRateInput,
                                onValueChange = { customRateInput = it },
                                label = "Custom GST Percentage",
                                suffixText = "%",
                                testTag = "gst_custom_rate_input",
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Tax Supply Type",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = !isInterState,
                            onClick = { isInterState = false },
                            label = { Text("Intra-State (CGST + SGST)") },
                        )
                        FilterChip(
                            selected = isInterState,
                            onClick = { isInterState = true },
                            label = { Text("Inter-State (IGST)") },
                        )
                    }
                }

                // Result Card
                OmniResultCard(
                    label = if (isAddGst) "Final Amount (Incl. GST)" else "Original Amount (Excl. GST)",
                    primaryValue = if (isAddGst) fmt(finalAmount) else fmt(originalAmount),
                    badgeText = "${SampleCatalogs.formatNumber(gstRate, 2, false)}% GST",
                    onCopy = {
                        scope.launch { snackbarHostState.showSnackbar("Copied GST summary") }
                    },
                    breakdown = listOf(
                        "Original Amount" to fmt(originalAmount),
                        "Total GST (${SampleCatalogs.formatNumber(gstRate, 2, false)}%)" to fmt(gstAmount),
                        "CGST (${SampleCatalogs.formatNumber(gstRate / 2.0, 2, false)}%)" to if (isInterState) "₹0" else fmt(halfGst),
                        "SGST (${SampleCatalogs.formatNumber(gstRate / 2.0, 2, false)}%)" to if (isInterState) "₹0" else fmt(halfGst),
                        "IGST (${SampleCatalogs.formatNumber(gstRate, 2, false)}%)" to if (isInterState) fmt(gstAmount) else "₹0",
                        "Final Payable Amount" to fmt(finalAmount),
                    ),
                )

                Button(
                    onClick = {
                        val sign = if (isAddGst) "+" else "−"
                        viewModel.addHistoryEntry(
                            expression = "${fmt(amount)} $sign ${SampleCatalogs.formatNumber(gstRate, 2, false)}% GST",
                            result = if (isAddGst) fmt(finalAmount) else fmt(originalAmount),
                            toolName = "GST",
                            reusableRoute = AppRoutes.GST_CALCULATOR,
                        )
                        scope.launch { snackbarHostState.showSnackbar("Saved to calculation history") }
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("gst_save_history_button"),
                ) {
                    Text("Save to History")
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun GstModeSegment(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String,
) {
    Surface(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
