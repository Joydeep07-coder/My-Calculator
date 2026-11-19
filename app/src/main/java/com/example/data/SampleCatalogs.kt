package com.example.data

import com.example.model.CurrencyInfo
import com.example.model.HistoryEntry
import com.example.model.HistoryGroup
import com.example.model.RecentConversion
import com.example.model.ToolCategory
import com.example.model.ToolItem
import com.example.model.UnitCategory
import com.example.model.UnitDef
import com.example.navigation.AppRoutes
import java.text.DecimalFormat
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

object SampleCatalogs {

    val allTools: List<ToolItem> = listOf(
        ToolItem(
            id = "standard_calc",
            title = "Standard Calculator",
            subtitle = "Everyday arithmetic with clean keypad",
            category = ToolCategory.CORE,
            route = AppRoutes.STANDARD_CALCULATOR,
        ),
        ToolItem(
            id = "scientific_calc",
            title = "Scientific Calculator",
            subtitle = "Trig, logs, powers, constants & DEG/RAD",
            category = ToolCategory.CORE,
            route = AppRoutes.SCIENTIFIC_CALCULATOR,
            badgeText = "PRO",
        ),
        ToolItem(
            id = "gst_calc",
            title = "GST Calculator",
            subtitle = "Indian CGST, SGST & IGST presets",
            category = ToolCategory.FINANCE,
            route = AppRoutes.GST_CALCULATOR,
            badgeText = "5-28%",
        ),
        ToolItem(
            id = "finance_hub",
            title = "Finance Suite",
            subtitle = "EMI, Simple, Compound & Discounts",
            category = ToolCategory.FINANCE,
            route = AppRoutes.FINANCE_HUB,
        ),
        ToolItem(
            id = "emi_calc",
            title = "EMI Calculator",
            subtitle = "Monthly loan installments & interest split",
            category = ToolCategory.FINANCE,
            route = AppRoutes.FINANCE_EMI,
        ),
        ToolItem(
            id = "simple_interest",
            title = "Simple Interest",
            subtitle = "Principal, rate & tenure returns",
            category = ToolCategory.FINANCE,
            route = AppRoutes.FINANCE_SIMPLE_INTEREST,
        ),
        ToolItem(
            id = "compound_interest",
            title = "Compound Interest",
            subtitle = "Quarterly, monthly & annual compounding",
            category = ToolCategory.FINANCE,
            route = AppRoutes.FINANCE_COMPOUND_INTEREST,
        ),
        ToolItem(
            id = "discount_calc",
            title = "Discount Calculator",
            subtitle = "Sale savings & final price breakdown",
            category = ToolCategory.FINANCE,
            route = AppRoutes.FINANCE_DISCOUNT,
        ),
        ToolItem(
            id = "unit_converter",
            title = "Unit Converter",
            subtitle = "12 categories from Length to Fuel",
            category = ToolCategory.CONVERTERS,
            route = AppRoutes.UNIT_CONVERTER,
            badgeText = "12",
        ),
        ToolItem(
            id = "currency_converter",
            title = "Currency Converter",
            subtitle = "INR, USD, EUR, GBP, JPY & more",
            category = ToolCategory.CONVERTERS,
            route = AppRoutes.CURRENCY_CONVERTER,
            badgeText = "Sample",
        ),
        ToolItem(
            id = "age_calc",
            title = "Age Calculator",
            subtitle = "Exact years, days & next birthday",
            category = ToolCategory.HEALTH_DATE,
            route = AppRoutes.AGE_CALCULATOR,
        ),
        ToolItem(
            id = "bmi_calc",
            title = "BMI Calculator",
            subtitle = "Body mass index & healthy scale",
            category = ToolCategory.HEALTH_DATE,
            route = AppRoutes.BMI_CALCULATOR,
        ),
        ToolItem(
            id = "programmer_calc",
            title = "Programmer",
            subtitle = "DEC, BIN, OCT, HEX & bitwise ops",
            category = ToolCategory.TECH,
            route = AppRoutes.PROGRAMMER_CALCULATOR,
            badgeText = "BIT",
        ),
        ToolItem(
            id = "graph_plotter",
            title = "Graph Plotter",
            subtitle = "Multi-function coordinate visualizer",
            category = ToolCategory.TECH,
            route = AppRoutes.GRAPH_PLOTTER,
            badgeText = "f(x)",
        ),
    )

    val initialHistory: List<HistoryEntry> = emptyList()

    val currencies: List<CurrencyInfo> = listOf(
        CurrencyInfo("INR", "Indian Rupee", "₹", "🇮🇳", 84.25, isPopular = true),
        CurrencyInfo("USD", "US Dollar", "$", "🇺🇸", 1.0, isPopular = true),
        CurrencyInfo("EUR", "Euro", "€", "🇪🇺", 0.92, isPopular = true),
        CurrencyInfo("GBP", "British Pound", "£", "🇬🇧", 0.79, isPopular = true),
        CurrencyInfo("JPY", "Japanese Yen", "¥", "🇯🇵", 154.20, isPopular = true),
        CurrencyInfo("AUD", "Australian Dollar", "A$", "🇦🇺", 1.53, isPopular = true),
        CurrencyInfo("CAD", "Canadian Dollar", "C$", "🇨🇦", 1.40, isPopular = true),
        CurrencyInfo("AED", "UAE Dirham", "د.إ", "🇦🇪", 3.67, isPopular = true),
        CurrencyInfo("CHF", "Swiss Franc", "CHF", "🇨🇭", 0.88),
        CurrencyInfo("SGD", "Singapore Dollar", "S$", "🇸🇬", 1.34),
        CurrencyInfo("CNY", "Chinese Yuan", "¥", "🇨🇳", 7.24),
        CurrencyInfo("KRW", "South Korean Won", "₩", "🇰🇷", 1395.0),
        CurrencyInfo("SAR", "Saudi Riyal", "﷼", "🇸🇦", 3.75),
        CurrencyInfo("NZD", "New Zealand Dollar", "NZ$", "🇳🇿", 1.69),
    )

    val unitDefinitions: Map<UnitCategory, List<UnitDef>> = mapOf(
        UnitCategory.LENGTH to listOf(
            UnitDef("m", "Meter", "m", 1.0),
            UnitDef("km", "Kilometer", "km", 1000.0),
            UnitDef("cm", "Centimeter", "cm", 0.01),
            UnitDef("mm", "Millimeter", "mm", 0.001),
            UnitDef("mi", "Mile", "mi", 1609.344),
            UnitDef("yd", "Yard", "yd", 0.9144),
            UnitDef("ft", "Foot", "ft", 0.3048),
            UnitDef("in", "Inch", "in", 0.0254),
        ),
        UnitCategory.WEIGHT to listOf(
            UnitDef("kg", "Kilogram", "kg", 1.0),
            UnitDef("g", "Gram", "g", 0.001),
            UnitDef("mg", "Milligram", "mg", 0.000001),
            UnitDef("lb", "Pound", "lb", 0.45359237),
            UnitDef("oz", "Ounce", "oz", 0.0283495231),
            UnitDef("ton", "Metric Ton", "t", 1000.0),
        ),
        UnitCategory.TEMPERATURE to listOf(
            UnitDef("c", "Celsius", "°C", 1.0),
            UnitDef("f", "Fahrenheit", "°F", 1.0),
            UnitDef("k", "Kelvin", "K", 1.0),
        ),
        UnitCategory.AREA to listOf(
            UnitDef("sqm", "Square Meter", "m²", 1.0),
            UnitDef("sqkm", "Square Kilometer", "km²", 1_000_000.0),
            UnitDef("sqft", "Square Foot", "ft²", 0.09290304),
            UnitDef("acre", "Acre", "ac", 4046.8564224),
            UnitDef("hectare", "Hectare", "ha", 10000.0),
        ),
        UnitCategory.VOLUME to listOf(
            UnitDef("l", "Liter", "L", 1.0),
            UnitDef("ml", "Milliliter", "mL", 0.001),
            UnitDef("cum", "Cubic Meter", "m³", 1000.0),
            UnitDef("gal", "US Gallon", "gal", 3.785411784),
            UnitDef("cup", "Cup", "cup", 0.2365882365),
        ),
        UnitCategory.SPEED to listOf(
            UnitDef("mps", "Meter / sec", "m/s", 1.0),
            UnitDef("kmh", "Kilometer / hr", "km/h", 0.277777778),
            UnitDef("mph", "Mile / hr", "mph", 0.44704),
            UnitDef("knot", "Knot", "kn", 0.514444444),
        ),
        UnitCategory.TIME to listOf(
            UnitDef("s", "Second", "s", 1.0),
            UnitDef("min", "Minute", "min", 60.0),
            UnitDef("hr", "Hour", "hr", 3600.0),
            UnitDef("day", "Day", "d", 86400.0),
            UnitDef("wk", "Week", "wk", 604800.0),
        ),
        UnitCategory.DATA to listOf(
            UnitDef("b", "Byte", "B", 1.0),
            UnitDef("kb", "Kilobyte", "KB", 1024.0),
            UnitDef("mb", "Megabyte", "MB", 1024.0 * 1024.0),
            UnitDef("gb", "Gigabyte", "GB", 1024.0 * 1024.0 * 1024.0),
            UnitDef("tb", "Terabyte", "TB", 1024.0 * 1024.0 * 1024.0 * 1024.0),
        ),
        UnitCategory.PRESSURE to listOf(
            UnitDef("pa", "Pascal", "Pa", 1.0),
            UnitDef("kpa", "Kilopascal", "kPa", 1000.0),
            UnitDef("bar", "Bar", "bar", 100000.0),
            UnitDef("psi", "PSI", "psi", 6894.75729),
            UnitDef("atm", "Atmosphere", "atm", 101325.0),
        ),
        UnitCategory.ENERGY to listOf(
            UnitDef("j", "Joule", "J", 1.0),
            UnitDef("kj", "Kilojoule", "kJ", 1000.0),
            UnitDef("cal", "Calorie", "cal", 4.184),
            UnitDef("kcal", "Kilocalorie", "kcal", 4184.0),
            UnitDef("kwh", "Kilowatt-hour", "kWh", 3_600_000.0),
        ),
        UnitCategory.POWER to listOf(
            UnitDef("w", "Watt", "W", 1.0),
            UnitDef("kw", "Kilowatt", "kW", 1000.0),
            UnitDef("hp", "Horsepower", "hp", 745.699872),
        ),
        UnitCategory.FUEL to listOf(
            UnitDef("kml", "Kilometer / Liter", "km/L", 1.0),
            UnitDef("mpg", "Miles / Gallon (US)", "mpg", 0.425143707),
            UnitDef("l100", "Liters / 100 km", "L/100km", 1.0),
        ),
    )

    val initialRecentConversions: List<RecentConversion> = emptyList()

    fun convertUnit(
        category: UnitCategory,
        from: UnitDef,
        to: UnitDef,
        input: Double,
    ): Double {
        if (from.id == to.id) return input
        if (category == UnitCategory.TEMPERATURE) {
            val celsius = when (from.id) {
                "c" -> input
                "f" -> (input - 32.0) * 5.0 / 9.0
                "k" -> input - 273.15
                else -> input
            }
            return when (to.id) {
                "c" -> celsius
                "f" -> celsius * 9.0 / 5.0 + 32.0
                "k" -> celsius + 273.15
                else -> celsius
            }
        }
        if (category == UnitCategory.FUEL) {
            val kml = when (from.id) {
                "kml" -> input
                "mpg" -> input * 0.425143707
                "l100" -> if (input == 0.0) 0.0 else 100.0 / input
                else -> input
            }
            return when (to.id) {
                "kml" -> kml
                "mpg" -> kml / 0.425143707
                "l100" -> if (kml == 0.0) 0.0 else 100.0 / kml
                else -> kml
            }
        }
        val baseValue = input * from.toBaseFactor
        return baseValue / to.toBaseFactor
    }

    fun formatNumber(value: Double, precision: Int = 6, useGrouping: Boolean = true): String {
        if (value.isNaN() || value.isInfinite()) return "Error"
        val clean = if (abs(value) < 1e-12) 0.0 else value
        val pattern = buildString {
            append(if (useGrouping) "#,##0" else "###0")
            if (precision > 0) {
                append(".")
                repeat(precision) { append("#") }
            }
        }
        return DecimalFormat(pattern).format(clean)
    }
}

/**
 * Clean lightweight UI-preview expression evaluator for Standard, Scientific, and Graph Plotter.
 * Handles +, -, ×, ÷, %, ^, parentheses, sin/cos/tan/asin/acos/atan/log/ln/sqrt/abs/fact, π, e, and variable x.
 */
object SimpleExprHelper {

    fun evaluate(rawExpr: String, isDegrees: Boolean = true, xValue: Double? = null): Double? {
        val cleaned = rawExpr
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("π", PI.toString())
            .replace("√(", "sqrt(")
            .replace("√", "sqrt")
            .replace("sin⁻¹(", "asin(")
            .replace("cos⁻¹(", "acos(")
            .replace("tan⁻¹(", "atan(")
            .replace(" ", "")
        if (cleaned.isBlank()) return null

        return try {
            val parser = Parser(cleaned, isDegrees, xValue)
            val result = parser.parseExpression()
            if (parser.pos < cleaned.length) null else result
        } catch (_: Exception) {
            null
        }
    }

    private class Parser(
        private val str: String,
        private val isDegrees: Boolean,
        private val xValue: Double?,
    ) {
        var pos = 0

        fun parseExpression(): Double {
            var x = parseTerm()
            while (pos < str.length) {
                when (str[pos]) {
                    '+' -> {
                        pos++
                        x += parseTerm()
                    }
                    '-' -> {
                        pos++
                        x -= parseTerm()
                    }
                    else -> return x
                }
            }
            return x
        }

        private fun parseTerm(): Double {
            var x = parsePower()
            while (pos < str.length) {
                when (str[pos]) {
                    '*' -> {
                        pos++
                        x *= parsePower()
                    }
                    '/' -> {
                        pos++
                        val d = parsePower()
                        if (d == 0.0) throw ArithmeticException("Div by zero")
                        x /= d
                    }
                    '%' -> {
                        pos++
                        x /= 100.0
                    }
                    else -> return x
                }
            }
            return x
        }

        private fun parsePower(): Double {
            var x = parseUnary()
            if (pos < str.length && str[pos] == '^') {
                pos++
                x = x.pow(parsePower())
            }
            return x
        }

        private fun parseUnary(): Double {
            if (pos < str.length && str[pos] == '+') {
                pos++
                return parseUnary()
            }
            if (pos < str.length && str[pos] == '-') {
                pos++
                return -parseUnary()
            }
            return parsePostfix()
        }

        private fun parsePostfix(): Double {
            var x = parsePrimary()
            while (pos < str.length && str[pos] == '!') {
                pos++
                val n = x.toInt().coerceIn(0, 20)
                var fact = 1.0
                for (i in 2..n) fact *= i
                x = fact
            }
            return x
        }

        private fun parsePrimary(): Double {
            if (pos < str.length && str[pos] == '(') {
                pos++
                val x = parseExpression()
                if (pos < str.length && str[pos] == ')') pos++
                return x
            }
            if (pos < str.length && (str[pos].isDigit() || str[pos] == '.')) {
                val start = pos
                while (pos < str.length && (str[pos].isDigit() || str[pos] == '.')) pos++
                return str.substring(start, pos).toDouble()
            }
            if (pos < str.length && str[pos].isLetter()) {
                val start = pos
                while (pos < str.length && str[pos].isLetter()) pos++
                val name = str.substring(start, pos)
                if (name == "e") return kotlin.math.E
                if (name == "x" && xValue != null) return xValue
                val arg = if (pos < str.length && str[pos] == '(') {
                    pos++
                    val v = parseExpression()
                    if (pos < str.length && str[pos] == ')') pos++
                    v
                } else {
                    parsePrimary()
                }
                val rad = if (isDegrees) Math.toRadians(arg) else arg
                return when (name) {
                    "sin" -> sin(rad)
                    "cos" -> cos(rad)
                    "tan" -> tan(rad)
                    "asin" -> if (isDegrees) Math.toDegrees(asin(arg)) else asin(arg)
                    "acos" -> if (isDegrees) Math.toDegrees(acos(arg)) else acos(arg)
                    "atan" -> if (isDegrees) Math.toDegrees(atan(arg)) else atan(arg)
                    "sqrt" -> sqrt(arg)
                    "log" -> log10(arg)
                    "ln" -> ln(arg)
                    "abs" -> abs(arg)
                    else -> throw IllegalArgumentException("Unknown fn: $name")
                }
            }
            throw IllegalArgumentException("Unexpected char at $pos")
        }
    }
}
