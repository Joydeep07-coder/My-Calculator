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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.SampleCatalogs
import com.example.model.AppSettings
import com.example.model.RecentConversion
import com.example.model.UnitCategory
import com.example.model.UnitDef
import com.example.state.OmniCalcViewModel
import com.example.ui.components.ErrorStateBanner
import com.example.ui.components.OmniCard
import com.example.ui.components.OmniInputField
import com.example.ui.components.OmniTopBar
import com.example.ui.theme.JetBrainsMonoFontFamily
import kotlinx.coroutines.launch
import java.util.UUID

sealed class UnitConversionResult {
    data object Empty : UnitConversionResult()
    data class Error(val message: String) : UnitConversionResult()
    data class Valid(val value: Double, val formatted: String) : UnitConversionResult()
}

fun evaluateUnitConversion(
    category: UnitCategory,
    fromUnit: UnitDef,
    toUnit: UnitDef,
    inputText: String,
    settings: AppSettings = AppSettings(),
): UnitConversionResult {
    if (inputText.isBlank()) {
        return UnitConversionResult.Empty
    }
    val parsed = inputText.trim().toDoubleOrNull()
        ?: return UnitConversionResult.Error("Enter a valid numeric value.")
    if (!parsed.isFinite()) {
        return UnitConversionResult.Error("Enter a finite numeric value.")
    }

    if (category == UnitCategory.TEMPERATURE) {
        when (fromUnit.id) {
            "k" -> if (parsed < 0.0) return UnitConversionResult.Error("Temperature cannot be below 0 K.")
            "c" -> if (parsed < -273.15) return UnitConversionResult.Error("Temperature cannot be below -273.15 °C.")
            "f" -> if (parsed < -459.67) return UnitConversionResult.Error("Temperature cannot be below -459.67 °F.")
        }
    }

    if (category == UnitCategory.FUEL) {
        if (parsed < 0.0) {
            return UnitConversionResult.Error("Fuel consumption cannot be negative.")
        }
        if (parsed == 0.0 && (fromUnit.id == "l100" || toUnit.id == "l100") && fromUnit.id != toUnit.id) {
            return UnitConversionResult.Error("Value must be greater than 0 for L/100km conversion.")
        }
    }

    val converted = SampleCatalogs.convertUnit(category, fromUnit, toUnit, parsed)
    if (!converted.isFinite()) {
        return UnitConversionResult.Error("Converted result is out of range.")
    }
    val formatted = SampleCatalogs.formatFlexibleNumber(
        value = converted,
        preferredPrecision = settings.decimalPrecision,
        useGrouping = settings.thousandsSeparator,
    )
    return UnitConversionResult.Valid(value = converted, formatted = formatted)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitConverterScreen(
    initialCategoryName: String = "LENGTH",
    viewModel: OmniCalcViewModel? = null,
    onBack: () -> Unit,
) {
    val startCategory = remember(initialCategoryName) {
        UnitCategory.entries.firstOrNull { it.name.equals(initialCategoryName, ignoreCase = true) }
            ?: UnitCategory.LENGTH
    }

    val settings = if (viewModel != null) {
        viewModel.settings.collectAsStateWithLifecycle().value
    } else {
        AppSettings()
    }

    var selectedCategory by remember { mutableStateOf(startCategory) }
    val availableUnits = remember(selectedCategory) {
        SampleCatalogs.unitDefinitions[selectedCategory].orEmpty()
    }
    var fromUnit by remember(selectedCategory) { mutableStateOf(availableUnits.first()) }
    var toUnit by remember(selectedCategory) {
        mutableStateOf(availableUnits.getOrElse(1) { availableUnits.first() })
    }
    var inputValue by remember { mutableStateOf("") }
    var recentConversions by remember { mutableStateOf(SampleCatalogs.initialRecentConversions) }
    var pickingForFrom by remember { mutableStateOf<Boolean?>(null) }
    var swapAngle by remember { mutableFloatStateOf(0f) }
    val animatedRotation by animateFloatAsState(targetValue = swapAngle, label = "unit_swap")

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val conversionResult = remember(selectedCategory, fromUnit, toUnit, inputValue, settings) {
        evaluateUnitConversion(
            category = selectedCategory,
            fromUnit = fromUnit,
            toUnit = toUnit,
            inputText = inputValue,
            settings = settings,
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            OmniTopBar(
                title = "Unit Converter",
                subtitle = "${UnitCategory.entries.size} measurement categories",
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
                    .padding(bottom = 24.dp),
            ) {
                // 12 Categories horizontally scrollable selector
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.testTag("unit_category_row"),
                ) {
                    items(UnitCategory.entries) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category.title, maxLines = 1) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                            modifier = Modifier.testTag("unit_cat_${category.name}"),
                        )
                    }
                }

                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // From Unit Card
                    OmniCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "From",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            UnitSelectorPill(
                                unit = fromUnit,
                                onClick = { pickingForFrom = true },
                                testTag = "unit_from_selector",
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        OmniInputField(
                            value = inputValue,
                            onValueChange = { inputValue = it },
                            label = "${fromUnit.name} (${fromUnit.symbol})",
                            placeholder = "Enter value",
                            testTag = "unit_input_field",
                        )
                    }

                    // Swap Button centered
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
                                    val temp = fromUnit
                                    fromUnit = toUnit
                                    toUnit = temp
                                    swapAngle += 180f
                                }
                                .testTag("unit_swap_button"),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.SwapVert,
                                    contentDescription = "Swap units",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.rotate(animatedRotation),
                                )
                            }
                        }
                    }

                    if (conversionResult is UnitConversionResult.Error) {
                        ErrorStateBanner(
                            message = conversionResult.message,
                            modifier = Modifier.testTag("unit_error_banner"),
                        )
                    }

                    // To Unit Result Card
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
                                    text = "To (${toUnit.name})",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                UnitSelectorPill(
                                    unit = toUnit,
                                    onClick = { pickingForFrom = false },
                                    testTag = "unit_to_selector",
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                val displayText = when (conversionResult) {
                                    is UnitConversionResult.Valid -> "${conversionResult.formatted} ${toUnit.symbol}"
                                    else -> "—"
                                }
                                Text(
                                    text = displayText,
                                    style = MaterialTheme.typography.displayMedium.copy(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("unit_converted_result"),
                                )
                                IconButton(
                                    enabled = conversionResult is UnitConversionResult.Valid,
                                    onClick = {
                                        val valid = conversionResult as? UnitConversionResult.Valid ?: return@IconButton
                                        val entry = RecentConversion(
                                            id = UUID.randomUUID().toString(),
                                            category = selectedCategory,
                                            summary = "${inputValue.trim()} ${fromUnit.symbol} = ${valid.formatted} ${toUnit.symbol}",
                                        )
                                        recentConversions = (listOf(entry) + recentConversions).take(8)
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Added to recent conversions")
                                        }
                                    },
                                    modifier = Modifier.testTag("unit_bookmark_button"),
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.BookmarkAdd,
                                        contentDescription = "Save conversion",
                                        tint = if (conversionResult is UnitConversionResult.Valid) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                        },
                                    )
                                }
                            }
                        }
                    }

                    // Recent Conversions
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Recent Conversions",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    if (recentConversions.isEmpty()) {
                        OmniCard(contentPadding = PaddingValues(14.dp)) {
                            Text(
                                text = "No saved conversions yet. Tap the bookmark icon to save one.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        recentConversions.forEach { rec ->
                            OmniCard(contentPadding = PaddingValues(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = rec.summary,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontFamily = JetBrainsMonoFontFamily
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f),
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                    ) {
                                        Text(
                                            text = rec.category.title,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (pickingForFrom != null) {
            val isFrom = pickingForFrom == true
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            var unitQuery by remember { mutableStateOf("") }
            val filtered = availableUnits.filter {
                unitQuery.isBlank() ||
                    it.name.contains(unitQuery, ignoreCase = true) ||
                    it.symbol.contains(unitQuery, ignoreCase = true)
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
                        text = "Select ${if (isFrom) "Source" else "Target"} Unit (${selectedCategory.title})",
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = unitQuery,
                        onValueChange = { unitQuery = it },
                        placeholder = { Text("Search units...") },
                        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyColumn(
                        modifier = Modifier.height(320.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        items(filtered, key = { it.id }) { unit ->
                            val isSelected = if (isFrom) unit.id == fromUnit.id else unit.id == toUnit.id
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
                                        if (isFrom) fromUnit = unit else toUnit = unit
                                        pickingForFrom = null
                                    },
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column {
                                        Text(unit.name, style = MaterialTheme.typography.titleSmall)
                                        Text(
                                            unit.symbol,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
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
private fun UnitSelectorPill(
    unit: UnitDef,
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
            Text(
                text = "${unit.name} (${unit.symbol})",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowDown,
                contentDescription = "Change unit",
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
