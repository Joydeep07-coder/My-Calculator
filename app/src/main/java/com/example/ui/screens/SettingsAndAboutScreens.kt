package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CurrencyExchange
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Policy
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Slideshow
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.AngleMode
import com.example.model.AppSettings
import com.example.model.ThemeMode
import com.example.navigation.AppRoutes
import com.example.state.OmniCalcViewModel
import com.example.ui.components.ConfirmActionDialog
import com.example.ui.components.OmniCard
import com.example.ui.components.OmniLogoMark
import com.example.ui.components.OmniTopBar
import com.example.ui.theme.JetBrainsMonoFontFamily
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: OmniCalcViewModel,
    onNavigate: (String) -> Unit,
    onReplayOnboarding: () -> Unit,
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    var showLanguageSheet by remember { mutableStateOf(false) }
    var showCurrencySheet by remember { mutableStateOf(false) }
    var showPrivacySheet by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showResetSettingsDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding()),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 720.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                OmniTopBar(
                    title = "Settings",
                    subtitle = "Appearance, precision & app preferences",
                )

                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // Theme Selector Section
                    Text(
                        text = "Appearance",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    OmniCard {
                        Text(
                            text = "App Theme",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            ThemeOptionPill(
                                label = "Dark",
                                icon = Icons.Rounded.DarkMode,
                                selected = settings.themeMode == ThemeMode.DARK,
                                onClick = { viewModel.setThemeMode(ThemeMode.DARK) },
                                modifier = Modifier.weight(1f),
                                testTag = "theme_dark",
                            )
                            ThemeOptionPill(
                                label = "Light",
                                icon = Icons.Rounded.LightMode,
                                selected = settings.themeMode == ThemeMode.LIGHT,
                                onClick = { viewModel.setThemeMode(ThemeMode.LIGHT) },
                                modifier = Modifier.weight(1f),
                                testTag = "theme_light",
                            )
                            ThemeOptionPill(
                                label = "System",
                                icon = Icons.Rounded.PhoneAndroid,
                                selected = settings.themeMode == ThemeMode.SYSTEM,
                                onClick = { viewModel.setThemeMode(ThemeMode.SYSTEM) },
                                modifier = Modifier.weight(1f),
                                testTag = "theme_system",
                            )
                        }
                    }

                    // Calculator Preferences
                    Text(
                        text = "Calculator & Formatting",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    OmniCard(contentPadding = PaddingValues(0.dp)) {
                        SettingSwitchRow(
                            icon = Icons.Rounded.Vibration,
                            title = "Haptic Feedback",
                            subtitle = "Subtle tactile pulse on keypad taps",
                            checked = settings.hapticFeedback,
                            onCheckedChange = { viewModel.setHapticFeedback(it) },
                            testTag = "setting_haptic",
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        SettingSwitchRow(
                            icon = Icons.Rounded.VolumeUp,
                            title = "Button Sound",
                            subtitle = "Soft acoustic click on key press",
                            checked = settings.buttonSound,
                            onCheckedChange = { viewModel.setButtonSound(it) },
                            testTag = "setting_sound",
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        // Decimal Precision Stepper
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Decimal Precision",
                                    style = MaterialTheme.typography.titleSmall,
                                )
                                Text(
                                    text = "Max fractional digits displayed",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        viewModel.setDecimalPrecision(settings.decimalPrecision - 1)
                                    },
                                    enabled = settings.decimalPrecision > 2,
                                ) {
                                    Icon(Icons.Rounded.Remove, contentDescription = "Decrease precision")
                                }
                                Text(
                                    text = "${settings.decimalPrecision}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = JetBrainsMonoFontFamily
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp),
                                )
                                IconButton(
                                    onClick = {
                                        viewModel.setDecimalPrecision(settings.decimalPrecision + 1)
                                    },
                                    enabled = settings.decimalPrecision < 10,
                                ) {
                                    Icon(Icons.Rounded.Add, contentDescription = "Increase precision")
                                }
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        SettingSwitchRow(
                            icon = Icons.Rounded.Info,
                            title = "Thousands Separator",
                            subtitle = "Format large numbers with commas (1,000)",
                            checked = settings.thousandsSeparator,
                            onCheckedChange = { viewModel.setThousandsSeparator(it) },
                            testTag = "setting_thousands",
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        // Default Angle Mode
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("Default Angle Mode", style = MaterialTheme.typography.titleSmall)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                AngleMode.entries.forEach { mode ->
                                    FilterChip(
                                        selected = settings.angleMode == mode,
                                        onClick = { viewModel.setAngleMode(mode) },
                                        label = { Text(mode.label) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                        ),
                                    )
                                }
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        SettingActionRow(
                            icon = Icons.Rounded.CurrencyExchange,
                            title = "Default Currency",
                            trailingText = "${settings.currencyCode} (${settings.currencySymbol()})",
                            onClick = { showCurrencySheet = true },
                            testTag = "setting_currency",
                        )
                    }

                    // General & Data
                    Text(
                        text = "General & Legal",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    OmniCard(contentPadding = PaddingValues(0.dp)) {
                        SettingActionRow(
                            icon = Icons.Rounded.Language,
                            title = "Language",
                            trailingText = settings.language,
                            onClick = { showLanguageSheet = true },
                            testTag = "setting_language",
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        SettingActionRow(
                            icon = Icons.Rounded.DeleteSweep,
                            title = "Clear Calculation History",
                            trailingText = null,
                            isDestructive = true,
                            onClick = { showClearHistoryDialog = true },
                            testTag = "setting_clear_history",
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        SettingActionRow(
                            icon = Icons.Rounded.RestartAlt,
                            title = "Reset Settings to Defaults",
                            trailingText = null,
                            isDestructive = true,
                            onClick = { showResetSettingsDialog = true },
                            testTag = "setting_reset_settings",
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        SettingActionRow(
                            icon = Icons.Rounded.Slideshow,
                            title = "View Welcome Tour",
                            trailingText = null,
                            onClick = onReplayOnboarding,
                            testTag = "setting_onboarding",
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        SettingActionRow(
                            icon = Icons.Rounded.Policy,
                            title = "Privacy Policy",
                            trailingText = null,
                            onClick = { showPrivacySheet = true },
                            testTag = "setting_privacy",
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        SettingActionRow(
                            icon = Icons.Rounded.Info,
                            title = "About My Calculator",
                            trailingText = "v1.0.0",
                            onClick = { onNavigate(AppRoutes.ABOUT) },
                            testTag = "setting_about",
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }

        if (showLanguageSheet) {
            val languages = listOf("English", "Hindi", "Spanish", "French", "German", "Japanese")
            val sheetState = rememberModalBottomSheetState()
            ModalBottomSheet(
                onDismissRequest = { showLanguageSheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Select Language", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    languages.forEach { lang ->
                        val selected = settings.language == lang
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceContainer
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.setLanguage(lang)
                                    showLanguageSheet = false
                                },
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(lang, style = MaterialTheme.typography.titleSmall)
                                if (selected) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        if (showCurrencySheet) {
            val sheetState = rememberModalBottomSheetState()
            ModalBottomSheet(
                onDismissRequest = { showCurrencySheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Select Default Currency", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    AppSettings.supportedCurrencyCodes.forEach { (code, symbol) ->
                        val selected = settings.currencyCode.equals(code, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceContainer
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.setCurrencyCode(code)
                                    showCurrencySheet = false
                                }
                                .testTag("currency_option_$code"),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("$code ($symbol)", style = MaterialTheme.typography.titleSmall)
                                if (selected) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        if (showPrivacySheet) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { showPrivacySheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("Privacy Policy", style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = "Local Data Storage: Your calculation history is saved strictly on your device using a local Room SQLite database, and your app preferences (theme, currency, precision, angle mode, haptics, sound, and language) are stored locally using Jetpack DataStore Preferences. You can delete individual entries, clear all history, or reset settings at any time.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "Network Access: The Currency Converter connects over HTTPS to the public ExchangeRate-API (open.er-api.com) solely to retrieve live currency exchange rates when you open or refresh that tool. Standard calculator, scientific, GST, finance, unit, age, BMI, programmer, and graphing tools run 100% offline.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "No Data Collection: My Calculator does not collect, sell, or share personal information, analytics, or calculation history with external servers. Only the INTERNET permission (for live exchange rates) is declared; no storage, camera, location, or contact permissions are requested.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        if (showClearHistoryDialog) {
            ConfirmActionDialog(
                title = "Clear Calculation History?",
                message = "Remove all entries permanently from local history storage?",
                confirmLabel = "Clear",
                onConfirm = {
                    viewModel.clearAllHistory()
                    showClearHistoryDialog = false
                    scope.launch { snackbarHostState.showSnackbar("History cleared") }
                },
                onDismiss = { showClearHistoryDialog = false },
            )
        }

        if (showResetSettingsDialog) {
            ConfirmActionDialog(
                title = "Reset Settings to Defaults?",
                message = "Restore theme, currency, precision, angle mode, haptics, sound, and language to their default values?",
                confirmLabel = "Reset",
                onConfirm = {
                    viewModel.resetSettings()
                    showResetSettingsDialog = false
                    scope.launch { snackbarHostState.showSnackbar("Settings reset to defaults") }
                },
                onDismiss = { showResetSettingsDialog = false },
            )
        }
    }
}

@Composable
private fun ThemeOptionPill(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String,
) {
    Surface(
        modifier = modifier
            .height(54.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun SettingSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp),
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag),
        )
    }
}

@Composable
private fun SettingActionRow(
    icon: ImageVector,
    title: String,
    trailingText: String?,
    isDestructive: Boolean = false,
    onClick: () -> Unit,
    testTag: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 15.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp),
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        if (!trailingText.isNullOrBlank()) {
            Text(
                text = trailingText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun AboutScreen(
    onBack: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            OmniTopBar(
                title = "About My Calculator",
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
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                OmniCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(24.dp),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        OmniLogoMark(size = 72.dp)
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "My Calculator",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primaryContainer,
                        ) {
                            Text(
                                text = "Version 1.0.0 · Material 3",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "An all-in-one precision suite uniting Standard & Scientific calculators, GST, Finance tools, 12 Unit Converters, Live Currency rates, Age & BMI health utilities, Programmer bitwise logic, and a multi-function Graph Plotter with persistent local history and customizable settings.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                OmniCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Included Modules",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    val features = listOf(
                        "Standard & Scientific Calculators (DEG/RAD)",
                        "GST Calculator (0–28% & Custom CGST/SGST/IGST)",
                        "Finance Suite (EMI, Simple, Compound, Discount)",
                        "12-Category Unit Converter & Live Currency Rates",
                        "Age, BMI Scale, Programmer & Graph Plotter",
                        "Persistent History (Room) & Preferences (DataStore)",
                    )
                    features.forEach { feat ->
                        Text(
                            text = "•  $feat",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 3.dp),
                        )
                    }
                }
            }
        }
    }
}
