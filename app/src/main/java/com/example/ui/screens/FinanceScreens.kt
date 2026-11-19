package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.SampleCatalogs
import com.example.navigation.AppRoutes
import com.example.state.OmniCalcViewModel
import com.example.ui.components.OmniCard
import com.example.ui.components.OmniInputField
import com.example.ui.components.OmniResultCard
import com.example.ui.components.OmniTopBar
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.VioletPrimary
import kotlinx.coroutines.launch
import kotlin.math.pow

@Composable
fun FinanceHubScreen(
    onNavigate: (String) -> Unit,
    onBack: () -> Unit,
) {
    val items = remember {
        listOf(
            FinanceMenuEntry(
                title = "EMI Calculator",
                subtitle = "Monthly loan installment, total interest & payment split",
                route = AppRoutes.FINANCE_EMI,
                icon = Icons.Rounded.AccountBalance,
                accent = VioletPrimary,
            ),
            FinanceMenuEntry(
                title = "Simple Interest",
                subtitle = "Calculate straightforward principal returns over tenure",
                route = AppRoutes.FINANCE_SIMPLE_INTEREST,
                icon = Icons.Rounded.Savings,
                accent = AccentEmerald,
            ),
            FinanceMenuEntry(
                title = "Compound Interest",
                subtitle = "Annual, half-yearly, quarterly & monthly compounding",
                route = AppRoutes.FINANCE_COMPOUND_INTEREST,
                icon = Icons.Rounded.TrendingUp,
                accent = AccentCyan,
            ),
            FinanceMenuEntry(
                title = "Discount Calculator",
                subtitle = "Original price, discount percentage & net savings",
                route = AppRoutes.FINANCE_DISCOUNT,
                icon = Icons.Rounded.LocalOffer,
                accent = AccentAmber,
            ),
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            OmniTopBar(
                title = "Finance Calculators",
                subtitle = "Loans, investments & retail pricing",
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
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items.forEach { entry ->
                    OmniCard(
                        onClick = { onNavigate(entry.route) },
                        contentPadding = PaddingValues(16.dp),
                        modifier = Modifier.testTag("finance_hub_${entry.route}"),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(entry.accent.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = entry.icon,
                                    contentDescription = entry.title,
                                    tint = entry.accent,
                                    modifier = Modifier.size(24.dp),
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = entry.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = entry.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = "Open ${entry.title}",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class FinanceMenuEntry(
    val title: String,
    val subtitle: String,
    val route: String,
    val icon: ImageVector,
    val accent: Color,
)

@Composable
fun EmiCalculatorScreen(
    viewModel: OmniCalcViewModel,
    onBack: () -> Unit,
) {
    var loanAmountInput by remember { mutableStateOf("") }
    var rateInput by remember { mutableStateOf("") }
    var tenureInput by remember { mutableStateOf("") }
    var isYears by remember { mutableStateOf(true) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val principal = (loanAmountInput.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
    val annualRate = (rateInput.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
    val rawTenure = (tenureInput.toDoubleOrNull() ?: 0.0).coerceAtLeast(1.0)
    val months = if (isYears) (rawTenure * 12).toInt().coerceAtLeast(1) else rawTenure.toInt().coerceAtLeast(1)

    val monthlyRate = annualRate / 12.0 / 100.0
    val emi = if (monthlyRate == 0.0) {
        principal / months
    } else {
        val factor = (1.0 + monthlyRate).pow(months)
        principal * monthlyRate * factor / (factor - 1.0)
    }
    val totalPayment = emi * months
    val totalInterest = (totalPayment - principal).coerceAtLeast(0.0)

    fun fmt(v: Double) = "₹" + SampleCatalogs.formatNumber(v, 0, true)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            OmniTopBar(
                title = "EMI Calculator",
                subtitle = "Equated monthly installment & interest split",
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
                    OmniInputField(
                        value = loanAmountInput,
                        onValueChange = { loanAmountInput = it },
                        label = "Loan Amount",
                        prefixText = "₹",
                        testTag = "emi_loan_input",
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OmniInputField(
                        value = rateInput,
                        onValueChange = { rateInput = it },
                        label = "Annual Interest Rate",
                        suffixText = "% p.a.",
                        testTag = "emi_rate_input",
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OmniInputField(
                        value = tenureInput,
                        onValueChange = { tenureInput = it },
                        label = "Loan Tenure",
                        suffixText = if (isYears) "Years" else "Months",
                        testTag = "emi_tenure_input",
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = isYears,
                            onClick = { isYears = true },
                            label = { Text("Years") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                        )
                        FilterChip(
                            selected = !isYears,
                            onClick = { isYears = false },
                            label = { Text("Months") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                        )
                    }
                    Slider(
                        value = annualRate.toFloat().coerceIn(1f, 25f),
                        onValueChange = { rateInput = SampleCatalogs.formatNumber(it.toDouble(), 1, false) },
                        valueRange = 1f..25f,
                    )
                }

                // Visual Breakdown Bar
                if (totalPayment > 0) {
                    val principalRatio = (principal / totalPayment).toFloat().coerceIn(0.05f, 0.95f)
                    OmniCard {
                        Text(
                            text = "Payment Breakdown",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(14.dp)
                                .clip(RoundedCornerShape(50)),
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(principalRatio)
                                    .fillMaxSize()
                                    .background(VioletPrimary)
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f - principalRatio)
                                    .fillMaxSize()
                                    .background(AccentEmerald)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = "● Principal (${(principalRatio * 100).toInt()}%)",
                                style = MaterialTheme.typography.labelMedium,
                                color = VioletPrimary,
                            )
                            Text(
                                text = "● Interest (${100 - (principalRatio * 100).toInt()}%)",
                                style = MaterialTheme.typography.labelMedium,
                                color = AccentEmerald,
                            )
                        }
                    }
                }

                OmniResultCard(
                    label = "Monthly EMI",
                    primaryValue = "${fmt(emi)} / mo",
                    badgeText = "$months Months",
                    onCopy = { scope.launch { snackbarHostState.showSnackbar("Copied EMI summary") } },
                    breakdown = listOf(
                        "Principal Amount" to fmt(principal),
                        "Total Interest Payable" to fmt(totalInterest),
                        "Total Payment" to fmt(totalPayment),
                    ),
                )

                Button(
                    onClick = {
                        viewModel.addHistoryEntry(
                            expression = "EMI ${fmt(principal)} @ $rateInput% · ${months}m",
                            result = "${fmt(emi)}/mo",
                            toolName = "EMI",
                            reusableRoute = AppRoutes.FINANCE_EMI,
                        )
                        scope.launch { snackbarHostState.showSnackbar("Saved to calculation history") }
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("emi_save_button"),
                ) {
                    Text("Save EMI to History")
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun SimpleInterestScreen(
    viewModel: OmniCalcViewModel,
    onBack: () -> Unit,
) {
    var principalInput by remember { mutableStateOf("") }
    var rateInput by remember { mutableStateOf("") }
    var timeInput by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val principal = (principalInput.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
    val rate = (rateInput.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
    val years = (timeInput.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)

    val interest = principal * rate * years / 100.0
    val finalAmount = principal + interest
    fun fmt(v: Double) = "₹" + SampleCatalogs.formatNumber(v, 2, true)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            OmniTopBar(
                title = "Simple Interest",
                subtitle = "Linear interest returns calculator",
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
                    OmniInputField(
                        value = principalInput,
                        onValueChange = { principalInput = it },
                        label = "Principal Amount",
                        prefixText = "₹",
                        testTag = "si_principal_input",
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OmniInputField(
                        value = rateInput,
                        onValueChange = { rateInput = it },
                        label = "Annual Interest Rate",
                        suffixText = "%",
                        testTag = "si_rate_input",
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OmniInputField(
                        value = timeInput,
                        onValueChange = { timeInput = it },
                        label = "Time Period",
                        suffixText = "Years",
                        testTag = "si_time_input",
                    )
                }

                OmniResultCard(
                    label = "Final Maturity Amount",
                    primaryValue = fmt(finalAmount),
                    badgeText = "${SampleCatalogs.formatNumber(years, 1, false)} Yrs",
                    onCopy = { scope.launch { snackbarHostState.showSnackbar("Copied amount") } },
                    breakdown = listOf(
                        "Principal Invested" to fmt(principal),
                        "Total Interest Earned" to fmt(interest),
                        "Final Amount" to fmt(finalAmount),
                    ),
                )

                Button(
                    onClick = {
                        viewModel.addHistoryEntry(
                            expression = "SI ${fmt(principal)} @ $rateInput% × ${timeInput}y",
                            result = fmt(finalAmount),
                            toolName = "Simple Interest",
                            reusableRoute = AppRoutes.FINANCE_SIMPLE_INTEREST,
                        )
                        scope.launch { snackbarHostState.showSnackbar("Saved to calculation history") }
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                ) {
                    Text("Save to History")
                }
            }
        }
    }
}

@Composable
fun CompoundInterestScreen(
    viewModel: OmniCalcViewModel,
    onBack: () -> Unit,
) {
    var principalInput by remember { mutableStateOf("") }
    var rateInput by remember { mutableStateOf("") }
    var timeInput by remember { mutableStateOf("") }
    var frequencyPerYear by remember { mutableStateOf(4) } // Quarterly default

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val principal = (principalInput.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
    val rate = (rateInput.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0) / 100.0
    val years = (timeInput.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)

    val finalAmount = principal * (1.0 + rate / frequencyPerYear).pow(frequencyPerYear * years)
    val interestEarned = (finalAmount - principal).coerceAtLeast(0.0)
    fun fmt(v: Double) = "₹" + SampleCatalogs.formatNumber(v, 2, true)

    val frequencies = listOf(
        1 to "Yearly",
        2 to "Half-Yearly",
        4 to "Quarterly",
        12 to "Monthly",
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            OmniTopBar(
                title = "Compound Interest",
                subtitle = "Flexible compounding frequency",
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
                    OmniInputField(
                        value = principalInput,
                        onValueChange = { principalInput = it },
                        label = "Principal Amount",
                        prefixText = "₹",
                        testTag = "ci_principal_input",
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OmniInputField(
                        value = rateInput,
                        onValueChange = { rateInput = it },
                        label = "Annual Interest Rate",
                        suffixText = "%",
                        testTag = "ci_rate_input",
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OmniInputField(
                        value = timeInput,
                        onValueChange = { timeInput = it },
                        label = "Time Period",
                        suffixText = "Years",
                        testTag = "ci_time_input",
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Compounding Frequency",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        frequencies.forEach { (freq, label) ->
                            FilterChip(
                                selected = frequencyPerYear == freq,
                                onClick = { frequencyPerYear = freq },
                                label = { Text(label, maxLines = 1) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                ),
                            )
                        }
                    }
                }

                OmniResultCard(
                    label = "Final Compound Amount",
                    primaryValue = fmt(finalAmount),
                    badgeText = frequencies.first { it.first == frequencyPerYear }.second,
                    onCopy = { scope.launch { snackbarHostState.showSnackbar("Copied amount") } },
                    breakdown = listOf(
                        "Principal" to fmt(principal),
                        "Interest Earned" to fmt(interestEarned),
                        "Final Amount" to fmt(finalAmount),
                    ),
                )

                Button(
                    onClick = {
                        viewModel.addHistoryEntry(
                            expression = "CI ${fmt(principal)} @ $rateInput% (${timeInput}y)",
                            result = fmt(finalAmount),
                            toolName = "Compound Interest",
                            reusableRoute = AppRoutes.FINANCE_COMPOUND_INTEREST,
                        )
                        scope.launch { snackbarHostState.showSnackbar("Saved to calculation history") }
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                ) {
                    Text("Save to History")
                }
            }
        }
    }
}

@Composable
fun DiscountCalculatorScreen(
    viewModel: OmniCalcViewModel,
    onBack: () -> Unit,
) {
    var priceInput by remember { mutableStateOf("") }
    var discountInput by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val originalPrice = (priceInput.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
    val discountPct = (discountInput.toDoubleOrNull() ?: 0.0).coerceIn(0.0, 100.0)
    val discountAmount = originalPrice * discountPct / 100.0
    val finalPrice = (originalPrice - discountAmount).coerceAtLeast(0.0)
    fun fmt(v: Double) = "₹" + SampleCatalogs.formatNumber(v, 2, true)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            OmniTopBar(
                title = "Discount Calculator",
                subtitle = "Instant sale price & savings",
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
                    OmniInputField(
                        value = priceInput,
                        onValueChange = { priceInput = it },
                        label = "Original Price",
                        prefixText = "₹",
                        testTag = "discount_price_input",
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OmniInputField(
                        value = discountInput,
                        onValueChange = { discountInput = it },
                        label = "Discount Percentage",
                        suffixText = "%",
                        testTag = "discount_pct_input",
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("10", "20", "25", "30", "50").forEach { pct ->
                            FilterChip(
                                selected = discountInput == pct,
                                onClick = { discountInput = pct },
                                label = { Text("$pct%") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                ),
                            )
                        }
                    }
                }

                OmniResultCard(
                    label = "Final Price",
                    primaryValue = fmt(finalPrice),
                    badgeText = "Save ${SampleCatalogs.formatNumber(discountPct, 1, false)}%",
                    onCopy = { scope.launch { snackbarHostState.showSnackbar("Copied final price") } },
                    breakdown = listOf(
                        "Original Price" to fmt(originalPrice),
                        "Discount Amount" to "− ${fmt(discountAmount)}",
                        "You Pay" to fmt(finalPrice),
                    ),
                )

                Button(
                    onClick = {
                        viewModel.addHistoryEntry(
                            expression = "${fmt(originalPrice)} − $discountInput% off",
                            result = fmt(finalPrice),
                            toolName = "Discount",
                            reusableRoute = AppRoutes.FINANCE_DISCOUNT,
                        )
                        scope.launch { snackbarHostState.showSnackbar("Saved to calculation history") }
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                ) {
                    Text("Save to History")
                }
            }
        }
    }
}
