package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.SampleCatalogs
import com.example.navigation.AppRoutes
import com.example.state.OmniCalcViewModel
import com.example.ui.components.ErrorStateBanner
import com.example.ui.components.OmniCard
import com.example.ui.components.OmniInputField
import com.example.ui.components.OmniResultCard
import com.example.ui.components.OmniTopBar
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.VioletPrimary
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.round

sealed class EmiCalculationResult {
    data object Empty : EmiCalculationResult()
    data class Error(val message: String) : EmiCalculationResult()
    data class Valid(
        val principal: Double,
        val annualRate: Double,
        val months: Int,
        val monthlyEmi: Double,
        val avgMonthlyPrincipal: Double,
        val avgMonthlyInterest: Double,
        val month1Principal: Double,
        val month1Interest: Double,
        val totalInterest: Double,
        val totalPayment: Double,
        val principalShare: Double,
        val interestShare: Double,
    ) : EmiCalculationResult()
}

fun calculateEmi(
    loanAmountInput: String,
    rateInput: String,
    tenureInput: String,
    isYears: Boolean,
): EmiCalculationResult {
    if (loanAmountInput.isNotBlank()) {
        val p = loanAmountInput.trim().toDoubleOrNull()
            ?: return EmiCalculationResult.Error("Enter a valid numeric loan amount.")
        if (!p.isFinite() || p <= 0.0) {
            return EmiCalculationResult.Error("Loan amount must be greater than 0.")
        }
    }
    if (rateInput.isNotBlank()) {
        val r = rateInput.trim().toDoubleOrNull()
            ?: return EmiCalculationResult.Error("Enter a valid annual interest rate.")
        if (!r.isFinite() || r < 0.0 || r > 100.0) {
            return EmiCalculationResult.Error("Interest rate must be between 0% and 100%.")
        }
    }
    if (tenureInput.isNotBlank()) {
        val t = tenureInput.trim().toDoubleOrNull()
            ?: return EmiCalculationResult.Error("Enter a valid loan tenure.")
        if (!t.isFinite() || t <= 0.0) {
            return EmiCalculationResult.Error("Loan tenure must be greater than 0.")
        }
        if (!isYears && abs(t - round(t)) > 1e-6) {
            return EmiCalculationResult.Error("Tenure in months must be a whole number.")
        }
        val m = round(if (isYears) t * 12.0 else t)
        if (m < 1.0 || m > 1200.0) {
            return EmiCalculationResult.Error("Tenure must be between 1 and 1200 months.")
        }
    }

    if (loanAmountInput.isBlank() || rateInput.isBlank() || tenureInput.isBlank()) {
        return EmiCalculationResult.Empty
    }

    val principal = loanAmountInput.trim().toDouble()
    val annualRate = rateInput.trim().toDouble()
    val rawTenure = tenureInput.trim().toDouble()
    val months = round(if (isYears) rawTenure * 12.0 else rawTenure).toInt().coerceAtLeast(1)

    val monthlyRate = annualRate / 12.0 / 100.0
    val monthlyEmi: Double
    val totalPayment: Double
    val totalInterest: Double

    if (monthlyRate == 0.0) {
        monthlyEmi = principal / months
        totalPayment = principal
        totalInterest = 0.0
    } else {
        val factor = (1.0 + monthlyRate).pow(months)
        if (!factor.isFinite() || factor <= 1.0) {
            return EmiCalculationResult.Error("Loan tenure or rate exceeds supported calculation range.")
        }
        monthlyEmi = principal * monthlyRate * factor / (factor - 1.0)
        if (!monthlyEmi.isFinite()) {
            return EmiCalculationResult.Error("Loan calculation overflow.")
        }
        totalPayment = monthlyEmi * months
        totalInterest = (totalPayment - principal).coerceAtLeast(0.0)
    }

    val month1Interest = principal * monthlyRate
    val month1Principal = (monthlyEmi - month1Interest).coerceAtLeast(0.0)
    val principalShare = if (totalPayment > 0.0) (principal / totalPayment).coerceIn(0.0, 1.0) else 1.0
    val interestShare = (1.0 - principalShare).coerceIn(0.0, 1.0)

    return EmiCalculationResult.Valid(
        principal = principal,
        annualRate = annualRate,
        months = months,
        monthlyEmi = monthlyEmi,
        avgMonthlyPrincipal = principal / months,
        avgMonthlyInterest = totalInterest / months,
        month1Principal = month1Principal,
        month1Interest = month1Interest,
        totalInterest = totalInterest,
        totalPayment = totalPayment,
        principalShare = principalShare,
        interestShare = interestShare,
    )
}

sealed class SimpleOrCompoundResult {
    data object Empty : SimpleOrCompoundResult()
    data class Error(val message: String) : SimpleOrCompoundResult()
    data class Valid(
        val principal: Double,
        val annualRate: Double,
        val years: Double,
        val interest: Double,
        val totalAmount: Double,
    ) : SimpleOrCompoundResult()
}

fun calculateSimpleInterest(
    principalInput: String,
    rateInput: String,
    timeInput: String,
): SimpleOrCompoundResult {
    if (principalInput.isNotBlank()) {
        val p = principalInput.trim().toDoubleOrNull()
            ?: return SimpleOrCompoundResult.Error("Enter a valid numeric principal.")
        if (!p.isFinite() || p < 0.0) return SimpleOrCompoundResult.Error("Principal cannot be negative.")
    }
    if (rateInput.isNotBlank()) {
        val r = rateInput.trim().toDoubleOrNull()
            ?: return SimpleOrCompoundResult.Error("Enter a valid annual interest rate.")
        if (!r.isFinite() || r < 0.0) return SimpleOrCompoundResult.Error("Interest rate cannot be negative.")
    }
    if (timeInput.isNotBlank()) {
        val t = timeInput.trim().toDoubleOrNull()
            ?: return SimpleOrCompoundResult.Error("Enter a valid time period.")
        if (!t.isFinite() || t < 0.0) return SimpleOrCompoundResult.Error("Time period cannot be negative.")
    }
    if (principalInput.isBlank() || rateInput.isBlank() || timeInput.isBlank()) {
        return SimpleOrCompoundResult.Empty
    }
    val principal = principalInput.trim().toDouble()
    val rate = rateInput.trim().toDouble()
    val years = timeInput.trim().toDouble()
    val interest = principal * rate * years / 100.0
    val total = principal + interest
    if (!total.isFinite()) return SimpleOrCompoundResult.Error("Values exceed calculation range.")
    return SimpleOrCompoundResult.Valid(principal, rate, years, interest, total)
}

fun calculateCompoundInterest(
    principalInput: String,
    rateInput: String,
    timeInput: String,
    frequencyPerYear: Int,
): SimpleOrCompoundResult {
    if (principalInput.isNotBlank()) {
        val p = principalInput.trim().toDoubleOrNull()
            ?: return SimpleOrCompoundResult.Error("Enter a valid numeric principal.")
        if (!p.isFinite() || p < 0.0) return SimpleOrCompoundResult.Error("Principal cannot be negative.")
    }
    if (rateInput.isNotBlank()) {
        val r = rateInput.trim().toDoubleOrNull()
            ?: return SimpleOrCompoundResult.Error("Enter a valid annual interest rate.")
        if (!r.isFinite() || r < 0.0) return SimpleOrCompoundResult.Error("Interest rate cannot be negative.")
    }
    if (timeInput.isNotBlank()) {
        val t = timeInput.trim().toDoubleOrNull()
            ?: return SimpleOrCompoundResult.Error("Enter a valid time period.")
        if (!t.isFinite() || t < 0.0) return SimpleOrCompoundResult.Error("Time period cannot be negative.")
    }
    if (principalInput.isBlank() || rateInput.isBlank() || timeInput.isBlank()) {
        return SimpleOrCompoundResult.Empty
    }
    val principal = principalInput.trim().toDouble()
    val rate = rateInput.trim().toDouble()
    val years = timeInput.trim().toDouble()
    val freq = frequencyPerYear.coerceAtLeast(1)
    val total = principal * (1.0 + (rate / 100.0) / freq).pow(freq * years)
    if (!total.isFinite()) return SimpleOrCompoundResult.Error("Values exceed calculation range.")
    val interest = (total - principal).coerceAtLeast(0.0)
    return SimpleOrCompoundResult.Valid(principal, rate, years, interest, total)
}

sealed class DiscountCalculationResult {
    data object Empty : DiscountCalculationResult()
    data class Error(val message: String) : DiscountCalculationResult()
    data class Valid(
        val originalPrice: Double,
        val discountPercent: Double,
        val discountAmount: Double,
        val finalPrice: Double,
    ) : DiscountCalculationResult()
}

fun calculateDiscount(
    priceInput: String,
    discountInput: String,
): DiscountCalculationResult {
    if (priceInput.isNotBlank()) {
        val p = priceInput.trim().toDoubleOrNull()
            ?: return DiscountCalculationResult.Error("Enter a valid price.")
        if (!p.isFinite() || p < 0.0) return DiscountCalculationResult.Error("Price cannot be negative.")
    }
    if (discountInput.isNotBlank()) {
        val d = discountInput.trim().toDoubleOrNull()
            ?: return DiscountCalculationResult.Error("Enter a valid discount percentage.")
        if (!d.isFinite() || d < 0.0 || d > 100.0) {
            return DiscountCalculationResult.Error("Discount percentage must be between 0% and 100%.")
        }
    }
    if (priceInput.isBlank() || discountInput.isBlank()) {
        return DiscountCalculationResult.Empty
    }
    val originalPrice = priceInput.trim().toDouble()
    val discountPct = discountInput.trim().toDouble()
    val discountAmount = originalPrice * discountPct / 100.0
    val finalPrice = (originalPrice - discountAmount).coerceAtLeast(0.0)
    return DiscountCalculationResult.Valid(originalPrice, discountPct, discountAmount, finalPrice)
}

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

    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val emiResult = remember(loanAmountInput, rateInput, tenureInput, isYears) {
        calculateEmi(
            loanAmountInput = loanAmountInput,
            rateInput = rateInput,
            tenureInput = tenureInput,
            isYears = isYears,
        )
    }

    fun fmt(v: Double): String = formatSettingsCurrency(v, settings)
    val sliderRate = (rateInput.trim().toDoubleOrNull() ?: 0.0).toFloat().coerceIn(0f, 25f)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            OmniTopBar(
                title = "EMI Calculator",
                subtitle = "Monthly loan installment & interest split (${settings.currencyCode})",
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
                        prefixText = settings.currencySymbol(),
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
                            modifier = Modifier.testTag("emi_tenure_years"),
                        )
                        FilterChip(
                            selected = !isYears,
                            onClick = { isYears = false },
                            label = { Text("Months") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                            modifier = Modifier.testTag("emi_tenure_months"),
                        )
                    }
                    Slider(
                        value = sliderRate,
                        onValueChange = { rateInput = SampleCatalogs.formatNumber(it.toDouble(), 1, false) },
                        valueRange = 0f..25f,
                    )
                }

                if (emiResult is EmiCalculationResult.Error) {
                    ErrorStateBanner(
                        message = emiResult.message,
                        modifier = Modifier.testTag("emi_error_banner"),
                    )
                }

                // Visual Breakdown Bar (reflects exact principal vs interest percentages)
                if (emiResult is EmiCalculationResult.Valid && emiResult.totalPayment > 0.0) {
                    val principalRatio = emiResult.principalShare.toFloat().coerceIn(0f, 1f)
                    val interestRatio = emiResult.interestShare.toFloat().coerceIn(0f, 1f)
                    val principalPctStr = SampleCatalogs.formatNumber(emiResult.principalShare * 100.0, 1, false)
                    val interestPctStr = SampleCatalogs.formatNumber(emiResult.interestShare * 100.0, 1, false)

                    OmniCard(modifier = Modifier.testTag("emi_breakdown_card")) {
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
                            if (principalRatio > 0.0001f) {
                                Box(
                                    modifier = Modifier
                                        .weight(principalRatio)
                                        .fillMaxSize()
                                        .background(VioletPrimary)
                                )
                            }
                            if (interestRatio > 0.0001f) {
                                Box(
                                    modifier = Modifier
                                        .weight(interestRatio)
                                        .fillMaxSize()
                                        .background(AccentEmerald)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = "● Principal ($principalPctStr%)",
                                style = MaterialTheme.typography.labelMedium,
                                color = VioletPrimary,
                                modifier = Modifier.testTag("emi_principal_pct"),
                            )
                            Text(
                                text = "● Interest ($interestPctStr%)",
                                style = MaterialTheme.typography.labelMedium,
                                color = AccentEmerald,
                                modifier = Modifier.testTag("emi_interest_pct"),
                            )
                        }
                    }
                }

                when (emiResult) {
                    is EmiCalculationResult.Valid -> {
                        OmniResultCard(
                            label = "Monthly EMI",
                            primaryValue = "${fmt(emiResult.monthlyEmi)} / mo",
                            badgeText = "${emiResult.months} Months",
                            onCopy = { scope.launch { snackbarHostState.showSnackbar("Copied EMI summary") } },
                            breakdown = listOf(
                                "Avg. Monthly Principal" to fmt(emiResult.avgMonthlyPrincipal),
                                "Avg. Monthly Interest" to fmt(emiResult.avgMonthlyInterest),
                                "Month 1 Principal" to fmt(emiResult.month1Principal),
                                "Month 1 Interest" to fmt(emiResult.month1Interest),
                                "Principal Amount" to fmt(emiResult.principal),
                                "Total Interest Payable" to fmt(emiResult.totalInterest),
                                "Total Payment" to fmt(emiResult.totalPayment),
                            ),
                            modifier = Modifier.testTag("emi_result_card"),
                        )
                    }
                    else -> {
                        OmniResultCard(
                            label = "Monthly EMI",
                            primaryValue = "—",
                            badgeText = null,
                            breakdown = listOf(
                                "Avg. Monthly Principal" to "—",
                                "Avg. Monthly Interest" to "—",
                                "Principal Amount" to "—",
                                "Total Interest Payable" to "—",
                                "Total Payment" to "—",
                            ),
                            modifier = Modifier.testTag("emi_result_card"),
                        )
                    }
                }

                Button(
                    enabled = emiResult is EmiCalculationResult.Valid,
                    onClick = {
                        val valid = emiResult as? EmiCalculationResult.Valid ?: return@Button
                        viewModel.addHistoryEntry(
                            expression = "EMI ${fmt(valid.principal)} @ ${SampleCatalogs.formatNumber(valid.annualRate, 2, false)}% · ${valid.months}m",
                            result = "${fmt(valid.monthlyEmi)}/mo",
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
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val siResult = remember(principalInput, rateInput, timeInput) {
        calculateSimpleInterest(principalInput, rateInput, timeInput)
    }
    fun fmt(v: Double): String = formatSettingsCurrency(v, settings)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            OmniTopBar(
                title = "Simple Interest",
                subtitle = "Linear interest returns (${settings.currencyCode})",
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
                        prefixText = settings.currencySymbol(),
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

                if (siResult is SimpleOrCompoundResult.Error) {
                    ErrorStateBanner(message = siResult.message)
                }

                when (siResult) {
                    is SimpleOrCompoundResult.Valid -> {
                        OmniResultCard(
                            label = "Final Maturity Amount",
                            primaryValue = fmt(siResult.totalAmount),
                            badgeText = "${SampleCatalogs.formatNumber(siResult.years, 1, false)} Yrs",
                            onCopy = { scope.launch { snackbarHostState.showSnackbar("Copied amount") } },
                            breakdown = listOf(
                                "Principal Invested" to fmt(siResult.principal),
                                "Total Interest Earned" to fmt(siResult.interest),
                                "Final Amount" to fmt(siResult.totalAmount),
                            ),
                        )
                    }
                    else -> {
                        OmniResultCard(
                            label = "Final Maturity Amount",
                            primaryValue = "—",
                            breakdown = listOf(
                                "Principal Invested" to "—",
                                "Total Interest Earned" to "—",
                                "Final Amount" to "—",
                            ),
                        )
                    }
                }

                Button(
                    enabled = siResult is SimpleOrCompoundResult.Valid,
                    onClick = {
                        val valid = siResult as? SimpleOrCompoundResult.Valid ?: return@Button
                        viewModel.addHistoryEntry(
                            expression = "SI ${fmt(valid.principal)} @ ${SampleCatalogs.formatNumber(valid.annualRate, 2, false)}% × ${SampleCatalogs.formatNumber(valid.years, 2, false)}y",
                            result = fmt(valid.totalAmount),
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

    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val ciResult = remember(principalInput, rateInput, timeInput, frequencyPerYear) {
        calculateCompoundInterest(principalInput, rateInput, timeInput, frequencyPerYear)
    }
    fun fmt(v: Double): String = formatSettingsCurrency(v, settings)

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
                subtitle = "Flexible compounding frequency (${settings.currencyCode})",
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
                        prefixText = settings.currencySymbol(),
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
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

                if (ciResult is SimpleOrCompoundResult.Error) {
                    ErrorStateBanner(message = ciResult.message)
                }

                val freqLabel = frequencies.first { it.first == frequencyPerYear }.second
                when (ciResult) {
                    is SimpleOrCompoundResult.Valid -> {
                        OmniResultCard(
                            label = "Final Compound Amount",
                            primaryValue = fmt(ciResult.totalAmount),
                            badgeText = freqLabel,
                            onCopy = { scope.launch { snackbarHostState.showSnackbar("Copied amount") } },
                            breakdown = listOf(
                                "Principal" to fmt(ciResult.principal),
                                "Interest Earned" to fmt(ciResult.interest),
                                "Final Amount" to fmt(ciResult.totalAmount),
                            ),
                        )
                    }
                    else -> {
                        OmniResultCard(
                            label = "Final Compound Amount",
                            primaryValue = "—",
                            badgeText = freqLabel,
                            breakdown = listOf(
                                "Principal" to "—",
                                "Interest Earned" to "—",
                                "Final Amount" to "—",
                            ),
                        )
                    }
                }

                Button(
                    enabled = ciResult is SimpleOrCompoundResult.Valid,
                    onClick = {
                        val valid = ciResult as? SimpleOrCompoundResult.Valid ?: return@Button
                        viewModel.addHistoryEntry(
                            expression = "CI ${fmt(valid.principal)} @ ${SampleCatalogs.formatNumber(valid.annualRate, 2, false)}% (${SampleCatalogs.formatNumber(valid.years, 2, false)}y)",
                            result = fmt(valid.totalAmount),
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
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val discountResult = remember(priceInput, discountInput) {
        calculateDiscount(priceInput, discountInput)
    }
    fun fmt(v: Double): String = formatSettingsCurrency(v, settings)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            OmniTopBar(
                title = "Discount Calculator",
                subtitle = "Instant sale price & savings (${settings.currencyCode})",
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
                        prefixText = settings.currencySymbol(),
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        listOf("10", "20", "25", "30", "50").forEach { pct ->
                            FilterChip(
                                selected = discountInput == pct,
                                onClick = { discountInput = pct },
                                label = { Text("$pct%", maxLines = 1) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                ),
                            )
                        }
                    }
                }

                if (discountResult is DiscountCalculationResult.Error) {
                    ErrorStateBanner(message = discountResult.message)
                }

                when (discountResult) {
                    is DiscountCalculationResult.Valid -> {
                        OmniResultCard(
                            label = "Final Price",
                            primaryValue = fmt(discountResult.finalPrice),
                            badgeText = "Save ${SampleCatalogs.formatNumber(discountResult.discountPercent, 1, false)}%",
                            onCopy = { scope.launch { snackbarHostState.showSnackbar("Copied final price") } },
                            breakdown = listOf(
                                "Original Price" to fmt(discountResult.originalPrice),
                                "Discount Amount" to "− ${fmt(discountResult.discountAmount)}",
                                "You Pay" to fmt(discountResult.finalPrice),
                            ),
                        )
                    }
                    else -> {
                        OmniResultCard(
                            label = "Final Price",
                            primaryValue = "—",
                            breakdown = listOf(
                                "Original Price" to "—",
                                "Discount Amount" to "—",
                                "You Pay" to "—",
                            ),
                        )
                    }
                }

                Button(
                    enabled = discountResult is DiscountCalculationResult.Valid,
                    onClick = {
                        val valid = discountResult as? DiscountCalculationResult.Valid ?: return@Button
                        viewModel.addHistoryEntry(
                            expression = "${fmt(valid.originalPrice)} − ${SampleCatalogs.formatNumber(valid.discountPercent, 1, false)}% off",
                            result = fmt(valid.finalPrice),
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
