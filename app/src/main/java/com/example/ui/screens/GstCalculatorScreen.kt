package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.SampleCatalogs
import com.example.model.AppSettings
import com.example.navigation.AppRoutes
import com.example.state.OmniCalcViewModel
import com.example.ui.components.ErrorStateBanner
import com.example.ui.components.OmniCard
import com.example.ui.components.OmniInputField
import com.example.ui.components.OmniResultCard
import com.example.ui.components.OmniTopBar
import kotlinx.coroutines.launch

sealed class GstCalculationResult {
    data object Empty : GstCalculationResult()
    data class Error(val message: String) : GstCalculationResult()
    data class Valid(
        val taxableAmount: Double,
        val gstAmount: Double,
        val cgstAmount: Double,
        val sgstAmount: Double,
        val igstAmount: Double,
        val totalAmount: Double,
        val ratePercent: Double,
        val isAddGst: Boolean,
        val isInterState: Boolean,
    ) : GstCalculationResult()
}

fun calculateGst(
    amountInput: String,
    isAddGst: Boolean,
    selectedPreset: Int,
    customRateInput: String,
    isInterState: Boolean,
): GstCalculationResult {
    // Validate custom rate first if selected and typed
    val gstRate = if (selectedPreset == -1) {
        if (customRateInput.isBlank()) {
            if (amountInput.isBlank()) return GstCalculationResult.Empty
            return GstCalculationResult.Error("Enter a custom GST rate between 0% and 100%.")
        }
        val parsedRate = customRateInput.trim().toDoubleOrNull()
        if (parsedRate == null || !parsedRate.isFinite()) {
            return GstCalculationResult.Error("Enter a valid numeric GST percentage.")
        }
        if (parsedRate < 0.0 || parsedRate > 100.0) {
            return GstCalculationResult.Error("GST rate must be between 0% and 100%.")
        }
        parsedRate
    } else {
        selectedPreset.toDouble().coerceIn(0.0, 100.0)
    }

    if (amountInput.isBlank()) {
        return GstCalculationResult.Empty
    }

    val amount = amountInput.trim().toDoubleOrNull()
    if (amount == null || !amount.isFinite()) {
        return GstCalculationResult.Error("Enter a valid numeric amount.")
    }
    if (amount < 0.0) {
        return GstCalculationResult.Error("Amount cannot be negative.")
    }

    val taxableAmount = if (isAddGst) {
        amount
    } else {
        if (gstRate == 0.0) amount else amount * 100.0 / (100.0 + gstRate)
    }
    val gstAmount = if (isAddGst) {
        amount * (gstRate / 100.0)
    } else {
        amount - taxableAmount
    }
    val totalAmount = if (isAddGst) taxableAmount + gstAmount else amount
    val halfGst = gstAmount / 2.0

    return GstCalculationResult.Valid(
        taxableAmount = taxableAmount,
        gstAmount = gstAmount,
        cgstAmount = if (isInterState) 0.0 else halfGst,
        sgstAmount = if (isInterState) 0.0 else halfGst,
        igstAmount = if (isInterState) gstAmount else 0.0,
        totalAmount = totalAmount,
        ratePercent = gstRate,
        isAddGst = isAddGst,
        isInterState = isInterState,
    )
}

fun formatSettingsCurrency(
    value: Double,
    settings: AppSettings,
): String {
    val symbol = settings.currencySymbol()
    return symbol + SampleCatalogs.formatNumber(
        value = value,
        precision = settings.decimalPrecision,
        useGrouping = settings.thousandsSeparator,
    )
}

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

    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val gstResult = remember(amountInput, isAddGst, selectedPreset, customRateInput, isInterState) {
        calculateGst(
            amountInput = amountInput,
            isAddGst = isAddGst,
            selectedPreset = selectedPreset,
            customRateInput = customRateInput,
            isInterState = isInterState,
        )
    }

    fun fmt(v: Double): String = formatSettingsCurrency(v, settings)
    val zeroMoney = fmt(0.0)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            OmniTopBar(
                title = "GST Calculator",
                subtitle = "CGST, SGST & IGST breakdown (${settings.currencyCode})",
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
                        label = if (isAddGst) "Taxable / Net Amount (Excl. GST)" else "Gross Amount (Incl. GST)",
                        prefixText = settings.currencySymbol(),
                        testTag = "gst_amount_input",
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "GST Slab Rate",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val presets = listOf(
                        0 to "0%",
                        5 to "5%",
                        12 to "12%",
                        18 to "18%",
                        28 to "28%",
                        -1 to "Custom",
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        presets.forEach { (rate, label) ->
                            FilterChip(
                                selected = selectedPreset == rate,
                                onClick = { selectedPreset = rate },
                                label = { Text(label, maxLines = 1) },
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FilterChip(
                            selected = !isInterState,
                            onClick = { isInterState = false },
                            label = { Text("Intra-State (CGST + SGST)", maxLines = 1) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                            modifier = Modifier.testTag("gst_supply_intra"),
                        )
                        FilterChip(
                            selected = isInterState,
                            onClick = { isInterState = true },
                            label = { Text("Inter-State (IGST)", maxLines = 1) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                            modifier = Modifier.testTag("gst_supply_inter"),
                        )
                    }
                }

                if (gstResult is GstCalculationResult.Error) {
                    ErrorStateBanner(
                        message = gstResult.message,
                        modifier = Modifier.testTag("gst_error_banner"),
                    )
                }

                val activeRate = when (gstResult) {
                    is GstCalculationResult.Valid -> gstResult.ratePercent
                    else -> if (selectedPreset == -1) {
                        customRateInput.toDoubleOrNull()?.coerceIn(0.0, 100.0) ?: 0.0
                    } else {
                        selectedPreset.toDouble()
                    }
                }
                val rateLabel = SampleCatalogs.formatNumber(activeRate, 2, false)
                val halfRateLabel = SampleCatalogs.formatNumber(activeRate / 2.0, 2, false)

                // Result Card
                when (gstResult) {
                    is GstCalculationResult.Valid -> {
                        val primaryResult = if (isAddGst) fmt(gstResult.totalAmount) else fmt(gstResult.taxableAmount)
                        OmniResultCard(
                            label = if (isAddGst) "Final Amount (Incl. GST)" else "Taxable Amount (Excl. GST)",
                            primaryValue = primaryResult,
                            badgeText = "$rateLabel% GST",
                            onCopy = {
                                scope.launch { snackbarHostState.showSnackbar("Copied GST summary") }
                            },
                            breakdown = listOf(
                                "Taxable Amount" to fmt(gstResult.taxableAmount),
                                "Total GST ($rateLabel%)" to fmt(gstResult.gstAmount),
                                "CGST ($halfRateLabel%)" to if (isInterState) zeroMoney else fmt(gstResult.cgstAmount),
                                "SGST ($halfRateLabel%)" to if (isInterState) zeroMoney else fmt(gstResult.sgstAmount),
                                "IGST ($rateLabel%)" to if (isInterState) fmt(gstResult.igstAmount) else zeroMoney,
                                "Final Payable Amount" to fmt(gstResult.totalAmount),
                            ),
                            modifier = Modifier.testTag("gst_result_card"),
                        )
                    }
                    else -> {
                        OmniResultCard(
                            label = if (isAddGst) "Final Amount (Incl. GST)" else "Taxable Amount (Excl. GST)",
                            primaryValue = "—",
                            badgeText = "$rateLabel% GST",
                            breakdown = listOf(
                                "Taxable Amount" to "—",
                                "Total GST ($rateLabel%)" to "—",
                                "CGST ($halfRateLabel%)" to "—",
                                "SGST ($halfRateLabel%)" to "—",
                                "IGST ($rateLabel%)" to "—",
                                "Final Payable Amount" to "—",
                            ),
                            modifier = Modifier.testTag("gst_result_card"),
                        )
                    }
                }

                Button(
                    enabled = gstResult is GstCalculationResult.Valid,
                    onClick = {
                        val valid = gstResult as? GstCalculationResult.Valid ?: return@Button
                        val sign = if (isAddGst) "+" else "−"
                        val inputAmount = amountInput.trim().toDoubleOrNull() ?: valid.taxableAmount
                        viewModel.addHistoryEntry(
                            expression = "${fmt(inputAmount)} $sign $rateLabel% GST",
                            result = if (isAddGst) fmt(valid.totalAmount) else fmt(valid.taxableAmount),
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
