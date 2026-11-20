package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ExchangeRateProvider
import com.example.data.ExchangeRateUiState
import com.example.data.OpenExchangeRateProvider
import com.example.data.SampleCatalogs
import com.example.model.AppSettings
import com.example.model.CurrencyInfo
import com.example.state.OmniCalcViewModel
import com.example.ui.components.ErrorStateBanner
import com.example.ui.components.OmniCard
import com.example.ui.components.OmniInputField
import com.example.ui.components.OmniTopBar
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.JetBrainsMonoFontFamily

sealed class CurrencyConversionResult {
    data object Empty : CurrencyConversionResult()
    data class Error(val message: String) : CurrencyConversionResult()
    data class Valid(
        val convertedAmount: Double,
        val unitRate: Double,
        val formattedAmount: String,
        val formattedUnitRate: String,
    ) : CurrencyConversionResult()
}

fun formatCurrencyDisplayValue(
    value: Double,
    settings: AppSettings = AppSettings(),
): String = SampleCatalogs.formatFlexibleNumber(
    value = value,
    preferredPrecision = settings.decimalPrecision.coerceIn(2, 10),
    useGrouping = settings.thousandsSeparator,
    minFixedDecimals = 2,
)

fun evaluateCurrencyConversion(
    amountInput: String,
    fromCurrency: CurrencyInfo,
    toCurrency: CurrencyInfo,
    settings: AppSettings = AppSettings(),
): CurrencyConversionResult {
    if (amountInput.isBlank()) {
        return CurrencyConversionResult.Empty
    }
    val amount = amountInput.trim().toDoubleOrNull()
        ?: return CurrencyConversionResult.Error("Enter a valid numeric amount.")
    if (!amount.isFinite()) {
        return CurrencyConversionResult.Error("Enter a finite numeric amount.")
    }
    if (amount < 0.0) {
        return CurrencyConversionResult.Error("Amount cannot be negative.")
    }
    if (fromCurrency.rateFromUsd <= 0.0 || toCurrency.rateFromUsd <= 0.0) {
        return CurrencyConversionResult.Error("Exchange rate is unavailable for this pair.")
    }

    val unitRate = toCurrency.rateFromUsd / fromCurrency.rateFromUsd
    val convertedAmount = amount * unitRate
    if (!convertedAmount.isFinite() || !unitRate.isFinite()) {
        return CurrencyConversionResult.Error("Converted amount exceeds supported range.")
    }

    return CurrencyConversionResult.Valid(
        convertedAmount = convertedAmount,
        unitRate = unitRate,
        formattedAmount = formatCurrencyDisplayValue(convertedAmount, settings),
        formattedUnitRate = SampleCatalogs.formatFlexibleNumber(
            value = unitRate,
            preferredPrecision = settings.decimalPrecision.coerceAtLeast(4),
            useGrouping = false,
            minFixedDecimals = 2,
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyConverterScreen(
    onBack: () -> Unit,
    viewModel: OmniCalcViewModel? = null,
    rateProvider: ExchangeRateProvider = remember { OpenExchangeRateProvider() },
) {
    val settings = if (viewModel != null) {
        viewModel.settings.collectAsStateWithLifecycle().value
    } else {
        AppSettings()
    }

    val baseCatalog = remember { SampleCatalogs.currencies }
    var fromCode by remember { mutableStateOf("USD") }
    var toCode by remember { mutableStateOf(settings.currencyCode.takeIf { code -> baseCatalog.any { it.code == code } } ?: "INR") }
    var amountInput by remember { mutableStateOf("") }
    var pickingForFrom by remember { mutableStateOf<Boolean?>(null) }
    var swapAngle by remember { mutableFloatStateOf(0f) }
    val animatedRotation by animateFloatAsState(targetValue = swapAngle, label = "currency_swap")

    var refreshNonce by remember { mutableIntStateOf(0) }
    var uiState by remember { mutableStateOf<ExchangeRateUiState>(ExchangeRateUiState.Loading) }

    LaunchedEffect(rateProvider, refreshNonce) {
        uiState = ExchangeRateUiState.Loading
        val response = rateProvider.fetchUsdRates()
        uiState = response.fold(
            onSuccess = { live ->
                val liveCurrencies = baseCatalog.mapNotNull { info ->
                    val rate = live.ratesFromUsd[info.code] ?: return@mapNotNull null
                    info.copy(rateFromUsd = rate)
                }
                if (liveCurrencies.isEmpty()) {
                    ExchangeRateUiState.Error("No supported currency rates were returned by the provider.")
                } else {
                    ExchangeRateUiState.Success(
                        currencies = liveCurrencies,
                        sourceName = live.sourceName,
                        lastUpdated = live.lastUpdatedTimestamp,
                    )
                }
            },
            onFailure = { error ->
                ExchangeRateUiState.Error(
                    error.message?.takeIf { it.isNotBlank() }
                        ?: "Unable to load live exchange rates. Check your internet connection."
                )
            },
        )
    }

    val activeCurrencies = when (val state = uiState) {
        is ExchangeRateUiState.Success -> state.currencies
        else -> baseCatalog
    }
    val fromCurrency = activeCurrencies.firstOrNull { it.code == fromCode } ?: activeCurrencies.first()
    val toCurrency = activeCurrencies.firstOrNull { it.code == toCode }
        ?: activeCurrencies.getOrElse(1) { activeCurrencies.first() }
    val popularCurrencies = remember(activeCurrencies) {
        activeCurrencies.filter { it.isPopular }
    }

    val conversionResult = remember(uiState, amountInput, fromCurrency, toCurrency, settings) {
        if (uiState is ExchangeRateUiState.Success) {
            evaluateCurrencyConversion(
                amountInput = amountInput,
                fromCurrency = fromCurrency,
                toCurrency = toCurrency,
                settings = settings,
            )
        } else {
            // Still surface syntax/negative input errors immediately even while rates load/fail
            if (amountInput.isNotBlank()) {
                val parsed = amountInput.trim().toDoubleOrNull()
                if (parsed == null || !parsed.isFinite()) {
                    CurrencyConversionResult.Error("Enter a valid numeric amount.")
                } else if (parsed < 0.0) {
                    CurrencyConversionResult.Error("Amount cannot be negative.")
                } else {
                    CurrencyConversionResult.Empty
                }
            } else {
                CurrencyConversionResult.Empty
            }
        }
    }

    val topBarSubtitle = when (val state = uiState) {
        is ExchangeRateUiState.Loading -> "Fetching live exchange rates..."
        is ExchangeRateUiState.Success -> "${state.sourceName} · ${state.lastUpdated}"
        is ExchangeRateUiState.Error -> "Live exchange rates unavailable"
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            OmniTopBar(
                title = "Currency Converter",
                subtitle = topBarSubtitle,
                onBack = onBack,
                actions = {
                    IconButton(
                        onClick = { refreshNonce++ },
                        enabled = uiState !is ExchangeRateUiState.Loading,
                        modifier = Modifier.testTag("currency_refresh_button"),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Refresh exchange rates",
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                },
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
                // Live exchange rate status banner (Loading / Success / Error — never claims sample rates are live)
                when (val state = uiState) {
                    is ExchangeRateUiState.Loading -> {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.testTag("currency_loading_banner"),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .testTag("currency_loading_indicator"),
                                    strokeWidth = 2.dp,
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Fetching live exchange rates...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }

                    is ExchangeRateUiState.Success -> {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = AccentEmerald.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, AccentEmerald.copy(alpha = 0.4f)),
                            modifier = Modifier.testTag("currency_source_banner"),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CloudDone,
                                    contentDescription = "Live rates loaded",
                                    tint = AccentEmerald,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Source: ${state.sourceName}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        text = "Last updated: ${state.lastUpdated}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }

                    is ExchangeRateUiState.Error -> {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.testTag("currency_error_banner"),
                        ) {
                            ErrorStateBanner(message = state.message)
                            OutlinedButton(
                                onClick = { refreshNonce++ },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("currency_retry_button"),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Retry Live Rates")
                            }
                        }
                    }
                }

                // From Currency Card
                OmniCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "From Currency",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        CurrencySelectorPill(
                            currency = fromCurrency,
                            onClick = { pickingForFrom = true },
                            testTag = "currency_from_selector",
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OmniInputField(
                        value = amountInput,
                        onValueChange = { amountInput = it },
                        label = "Amount (${fromCurrency.code})",
                        placeholder = "Enter amount",
                        prefixText = fromCurrency.symbol,
                        testTag = "currency_amount_input",
                    )
                }

                // Swap Button
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable {
                                val temp = fromCode
                                fromCode = toCode
                                toCode = temp
                                swapAngle += 180f
                            }
                            .testTag("currency_swap_button"),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.SwapVert,
                                contentDescription = "Swap currencies",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.rotate(animatedRotation),
                            )
                        }
                    }
                }

                if (conversionResult is CurrencyConversionResult.Error) {
                    ErrorStateBanner(
                        message = conversionResult.message,
                        modifier = Modifier.testTag("currency_input_error_banner"),
                    )
                }

                // To Currency Result Card
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                    ),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Converted Amount",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            CurrencySelectorPill(
                                currency = toCurrency,
                                onClick = { pickingForFrom = false },
                                testTag = "currency_to_selector",
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        val primaryResultText = when {
                            uiState !is ExchangeRateUiState.Success -> "—"
                            conversionResult is CurrencyConversionResult.Valid ->
                                "${toCurrency.symbol} ${conversionResult.formattedAmount}"
                            else -> "—"
                        }
                        Text(
                            text = primaryResultText,
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.testTag("currency_converted_result"),
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val rateSubtitle = when (uiState) {
                            is ExchangeRateUiState.Success -> {
                                val unitRate = toCurrency.rateFromUsd / fromCurrency.rateFromUsd
                                val formattedRate = SampleCatalogs.formatFlexibleNumber(
                                    value = unitRate,
                                    preferredPrecision = settings.decimalPrecision.coerceAtLeast(4),
                                    useGrouping = false,
                                    minFixedDecimals = 2,
                                )
                                "1 ${fromCurrency.code} = $formattedRate ${toCurrency.code}"
                            }
                            is ExchangeRateUiState.Loading -> "Loading live rate..."
                            is ExchangeRateUiState.Error -> "Live exchange rate unavailable"
                        }
                        Text(
                            text = rateSubtitle,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = JetBrainsMonoFontFamily
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.testTag("currency_unit_rate_text"),
                        )
                    }
                }

                // Popular Currencies
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Popular Currencies (vs ${fromCurrency.code})",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                val validInputAmount = if (uiState is ExchangeRateUiState.Success && conversionResult is CurrencyConversionResult.Valid) {
                    amountInput.trim().toDoubleOrNull()
                } else {
                    null
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    popularCurrencies.chunked(2).forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            pair.forEach { curr ->
                                val displayVal = if (uiState is ExchangeRateUiState.Success && validInputAmount != null) {
                                    val rate = curr.rateFromUsd / fromCurrency.rateFromUsd
                                    formatCurrencyDisplayValue(validInputAmount * rate, settings)
                                } else {
                                    "—"
                                }
                                OmniCard(
                                    onClick = { toCode = curr.code },
                                    contentPadding = PaddingValues(12.dp),
                                    borderColor = if (curr.code == toCurrency.code) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.outlineVariant
                                    },
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text = "${curr.flag} ${curr.code}",
                                            style = MaterialTheme.typography.titleSmall,
                                        )
                                        Text(
                                            text = curr.symbol,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = displayVal,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = JetBrainsMonoFontFamily
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                            if (pair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (pickingForFrom != null) {
            val isFrom = pickingForFrom == true
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            var searchQuery by remember { mutableStateOf("") }
            val filtered = activeCurrencies.filter {
                searchQuery.isBlank() ||
                    it.code.contains(searchQuery, ignoreCase = true) ||
                    it.name.contains(searchQuery, ignoreCase = true)
            }

            ModalBottomSheet(
                onDismissRequest = { pickingForFrom = null },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = "Select ${if (isFrom) "From" else "To"} Currency",
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search INR, USD, EUR, Euro...") },
                        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("currency_search_input"),
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyColumn(
                        modifier = Modifier.height(340.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        items(filtered, key = { it.code }) { currency ->
                            val isSelected = if (isFrom) currency.code == fromCurrency.code else currency.code == toCurrency.code
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainer
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        if (isFrom) fromCode = currency.code else toCode = currency.code
                                        pickingForFrom = null
                                    },
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(currency.flag, fontSize = 22.sp)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "${currency.code} · ${currency.symbol}",
                                                style = MaterialTheme.typography.titleSmall,
                                            )
                                            Text(
                                                text = currency.name,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    }
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = "Selected",
                                            tint = MaterialTheme.colorScheme.primary,
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun CurrencySelectorPill(
    currency: CurrencyInfo,
    onClick: () -> Unit,
    testTag: String,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(currency.flag, fontSize = 18.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = currency.code,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowDown,
                contentDescription = "Select currency",
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
