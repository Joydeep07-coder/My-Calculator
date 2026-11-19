package com.example.model

import androidx.compose.ui.graphics.Color

enum class ThemeMode(val label: String) {
    DARK("Dark Mode"),
    LIGHT("Light Mode"),
    SYSTEM("System Default"),
}

enum class AngleMode(val label: String) {
    DEG("DEG"),
    RAD("RAD"),
}

enum class NumberBase(val label: String, val radix: Int) {
    DEC("DEC", 10),
    BIN("BIN", 2),
    OCT("OCT", 8),
    HEX("HEX", 16),
}

enum class ToolCategory(val title: String) {
    CORE("Core Calculators"),
    FINANCE("Finance & Tax"),
    CONVERTERS("Smart Converters"),
    HEALTH_DATE("Date & Health"),
    TECH("Tech & Plotting"),
}

data class ToolItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: ToolCategory,
    val route: String,
    val badgeText: String? = null,
)

enum class HistoryGroup(val label: String) {
    TODAY("Today"),
    YESTERDAY("Yesterday"),
    EARLIER("Earlier"),
}

data class HistoryEntry(
    val id: String,
    val expression: String,
    val result: String,
    val toolName: String,
    val timeLabel: String,
    val group: HistoryGroup,
    val reusableRoute: String = "calculator",
)

data class CurrencyInfo(
    val code: String,
    val name: String,
    val symbol: String,
    val flag: String,
    val rateFromUsd: Double,
    val isPopular: Boolean = false,
)

enum class UnitCategory(val title: String) {
    LENGTH("Length"),
    WEIGHT("Weight"),
    TEMPERATURE("Temperature"),
    AREA("Area"),
    VOLUME("Volume"),
    SPEED("Speed"),
    TIME("Time"),
    DATA("Data Storage"),
    PRESSURE("Pressure"),
    ENERGY("Energy"),
    POWER("Power"),
    FUEL("Fuel Consumption"),
}

data class UnitDef(
    val id: String,
    val name: String,
    val symbol: String,
    val toBaseFactor: Double,
)

data class RecentConversion(
    val id: String,
    val category: UnitCategory,
    val summary: String,
)

data class PlotFunction(
    val id: String,
    val expression: String,
    val color: Color,
    val isVisible: Boolean = true,
)

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.DARK,
    val hapticFeedback: Boolean = true,
    val buttonSound: Boolean = false,
    val decimalPrecision: Int = 6,
    val thousandsSeparator: Boolean = true,
    val angleMode: AngleMode = AngleMode.DEG,
    val language: String = "English",
)
