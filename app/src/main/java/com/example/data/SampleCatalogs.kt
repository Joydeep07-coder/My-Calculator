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
            subtitle = "CGST, SGST & IGST presets (0–28%)",
            category = ToolCategory.FINANCE,
            route = AppRoutes.GST_CALCULATOR,
            badgeText = "0-28%",
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
            badgeText = "LIVE",
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
            UnitDef("kmh", "Kilometer / hr", "km/h", 1.0 / 3.6),
            UnitDef("mph", "Mile / hr", "mph", 0.44704),
            UnitDef("knot", "Knot", "kn", 1852.0 / 3600.0),
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
        val symbols = java.text.DecimalFormatSymbols(java.util.Locale.US)
        return DecimalFormat(pattern, symbols).format(clean)
    }

    /**
     * Formats numbers while preserving significant digits for small non-zero values
     * (e.g. unit conversions like mg -> kg, mL -> m³, or tiny currency amounts) instead
     * of rounding them down to "0".
     */
    fun formatFlexibleNumber(
        value: Double,
        preferredPrecision: Int = 6,
        useGrouping: Boolean = true,
        minFixedDecimals: Int = 0,
    ): String {
        if (value.isNaN() || value.isInfinite()) return "Error"
        if (value == 0.0) {
            return if (minFixedDecimals > 0) {
                val symbols = java.text.DecimalFormatSymbols(java.util.Locale.US)
                val pattern = "0." + "0".repeat(minFixedDecimals.coerceIn(1, 10))
                DecimalFormat(pattern, symbols).format(0.0)
            } else {
                "0"
            }
        }

        val absVal = abs(value)
        val symbols = java.text.DecimalFormatSymbols(java.util.Locale.US)
        if (absVal < 1e-6) {
            val sigDecimals = preferredPrecision.coerceIn(2, 6)
            val expPattern = "0." + "#".repeat(sigDecimals) + "E0"
            return DecimalFormat(expPattern, symbols).format(value).replace("E", "e")
        }

        val leadingZeros = if (absVal < 1.0) {
            kotlin.math.ceil(-log10(absVal)).toInt().coerceAtLeast(1) - 1
        } else {
            0
        }
        val effectivePrecision = maxOf(
            preferredPrecision.coerceIn(0, 10),
            minFixedDecimals.coerceIn(0, 10),
            if (absVal < 1.0) (leadingZeros + 4).coerceAtMost(10) else 0,
        )
        val pattern = buildString {
            append(if (useGrouping) "#,##0" else "###0")
            if (effectivePrecision > 0) {
                append(".")
                val requiredZeros = if (absVal >= 1.0 || absVal >= 10.0.pow(-minFixedDecimals)) {
                    minFixedDecimals.coerceAtMost(effectivePrecision)
                } else {
                    0
                }
                repeat(requiredZeros) { append("0") }
                repeat(effectivePrecision - requiredZeros) { append("#") }
            }
        }
        return DecimalFormat(pattern, symbols).format(value)
    }
}

/**
 * Mathematical expression evaluator for Standard, Scientific, and Graph Plotter.
 *
 * Precedence (lowest -> highest):
 * 1. Binary +, -
 * 2. Binary *, /, and implicit multiplication (e.g. 2(3+4), 3π, 2x, 2sin(30))
 * 3. Unary +, - (so -x^2 = -(x^2))
 * 4. Power ^ (right-associative; exponent allows unary +/- e.g. 2^-3)
 * 5. Postfix !, %
 * 6. Primary: numbers, constants (π, e), variable (x), functions, √, and (...)
 */
object SimpleExprHelper {

    sealed interface CompiledExpr {
        fun evaluate(isDegrees: Boolean = true, xValue: Double? = null): Double?
    }

    sealed class CompileResult {
        data class Success(val expr: CompiledExpr) : CompileResult()
        data class Error(val message: String) : CompileResult()
    }

    private sealed class AstNode : CompiledExpr {
        override fun evaluate(isDegrees: Boolean, xValue: Double?): Double? = try {
            val result = eval(isDegrees, xValue)
            if (!result.isFinite()) null else if (result == 0.0) 0.0 else result
        } catch (_: Exception) {
            null
        }

        abstract fun eval(isDegrees: Boolean, xValue: Double?): Double
    }

    private class ConstNode(private val value: Double) : AstNode() {
        override fun eval(isDegrees: Boolean, xValue: Double?): Double = value
    }

    private object VarXNode : AstNode() {
        override fun eval(isDegrees: Boolean, xValue: Double?): Double =
            xValue ?: throw ArithmeticException("Variable x has no value")
    }

    private class UnaryNegNode(private val operand: AstNode) : AstNode() {
        override fun eval(isDegrees: Boolean, xValue: Double?): Double = -operand.eval(isDegrees, xValue)
    }

    private class BinaryNode(
        private val op: Char,
        private val left: AstNode,
        private val right: AstNode,
    ) : AstNode() {
        override fun eval(isDegrees: Boolean, xValue: Double?): Double {
            val l = left.eval(isDegrees, xValue)
            val r = right.eval(isDegrees, xValue)
            return when (op) {
                '+' -> l + r
                '-' -> l - r
                '*' -> l * r
                '/' -> {
                    if (r == 0.0) throw ArithmeticException("Div by zero")
                    l / r
                }
                '^' -> {
                    val res = l.pow(r)
                    if (!res.isFinite()) throw ArithmeticException("Invalid power")
                    res
                }
                else -> throw IllegalStateException("Unknown op: $op")
            }
        }
    }

    private class FactorialNode(private val operand: AstNode) : AstNode() {
        override fun eval(isDegrees: Boolean, xValue: Double?): Double {
            val x = operand.eval(isDegrees, xValue)
            if (x < 0.0 || !x.isFinite() || abs(x - kotlin.math.round(x)) > 1e-9) {
                throw ArithmeticException("Factorial domain error")
            }
            val n = kotlin.math.round(x).toInt()
            if (n > 170) throw ArithmeticException("Factorial overflow")
            var fact = 1.0
            for (i in 2..n) fact *= i
            return fact
        }
    }

    private class PercentNode(private val operand: AstNode) : AstNode() {
        override fun eval(isDegrees: Boolean, xValue: Double?): Double = operand.eval(isDegrees, xValue) / 100.0
    }

    private class SqrtNode(private val operand: AstNode) : AstNode() {
        override fun eval(isDegrees: Boolean, xValue: Double?): Double {
            val arg = operand.eval(isDegrees, xValue)
            if (arg < 0.0) throw ArithmeticException("Negative sqrt")
            return sqrt(arg)
        }
    }

    private class FnNode(private val name: String, private val argNode: AstNode) : AstNode() {
        override fun eval(isDegrees: Boolean, xValue: Double?): Double {
            val arg = argNode.eval(isDegrees, xValue)
            val rad = if (isDegrees) Math.toRadians(arg) else arg
            return when (name) {
                "sin" -> {
                    if (isDegrees) {
                        val mod = ((arg % 360.0) + 360.0) % 360.0
                        when {
                            abs(mod) < 1e-12 || abs(mod - 180.0) < 1e-12 || abs(mod - 360.0) < 1e-12 -> 0.0
                            abs(mod - 90.0) < 1e-12 -> 1.0
                            abs(mod - 270.0) < 1e-12 -> -1.0
                            else -> sin(rad)
                        }
                    } else {
                        sin(rad)
                    }
                }
                "cos" -> {
                    if (isDegrees) {
                        val mod = ((arg % 360.0) + 360.0) % 360.0
                        when {
                            abs(mod - 90.0) < 1e-12 || abs(mod - 270.0) < 1e-12 -> 0.0
                            abs(mod) < 1e-12 || abs(mod - 360.0) < 1e-12 -> 1.0
                            abs(mod - 180.0) < 1e-12 -> -1.0
                            else -> cos(rad)
                        }
                    } else {
                        cos(rad)
                    }
                }
                "tan" -> {
                    if (isDegrees) {
                        val mod = ((arg % 360.0) + 360.0) % 360.0
                        if (abs(mod - 90.0) < 1e-12 || abs(mod - 270.0) < 1e-12) {
                            throw ArithmeticException("tan undefined at 90/270 deg")
                        }
                        if (abs(mod) < 1e-12 || abs(mod - 180.0) < 1e-12 || abs(mod - 360.0) < 1e-12) {
                            0.0
                        } else {
                            tan(rad)
                        }
                    } else {
                        if (abs(cos(rad)) < 1e-12) {
                            throw ArithmeticException("tan undefined")
                        }
                        tan(rad)
                    }
                }
                "asin" -> {
                    if (arg < -1.0 || arg > 1.0) throw ArithmeticException("asin domain")
                    if (isDegrees) Math.toDegrees(asin(arg)) else asin(arg)
                }
                "acos" -> {
                    if (arg < -1.0 || arg > 1.0) throw ArithmeticException("acos domain")
                    if (isDegrees) Math.toDegrees(acos(arg)) else acos(arg)
                }
                "atan" -> if (isDegrees) Math.toDegrees(atan(arg)) else atan(arg)
                "sqrt" -> {
                    if (arg < 0.0) throw ArithmeticException("Negative sqrt")
                    sqrt(arg)
                }
                "log" -> {
                    if (arg <= 0.0) throw ArithmeticException("Non-positive log")
                    log10(arg)
                }
                "ln" -> {
                    if (arg <= 0.0) throw ArithmeticException("Non-positive ln")
                    ln(arg)
                }
                "exp" -> {
                    val res = kotlin.math.exp(arg)
                    if (!res.isFinite()) throw ArithmeticException("exp overflow")
                    res
                }
                "abs" -> abs(arg)
                else -> throw IllegalArgumentException("Unknown function: '$name'")
            }
        }
    }

    private fun cleanExpression(rawExpr: String): String = rawExpr
        .replace("×", "*")
        .replace("÷", "/")
        .replace("−", "-")
        .replace("sin⁻¹", "asin")
        .replace("cos⁻¹", "acos")
        .replace("tan⁻¹", "atan")
        .replace(" ", "")

    fun compile(rawExpr: String, allowVariableX: Boolean = true): CompileResult {
        val cleaned = cleanExpression(rawExpr)
        if (cleaned.isBlank()) return CompileResult.Error("Expression cannot be empty.")

        return try {
            val parser = Parser(cleaned, allowVariableX)
            val ast = parser.parseExpression()
            if (parser.pos < cleaned.length) {
                CompileResult.Error("Unexpected '${cleaned[parser.pos]}' in expression.")
            } else {
                CompileResult.Success(ast)
            }
        } catch (e: IllegalArgumentException) {
            CompileResult.Error(e.message ?: "Invalid expression syntax.")
        } catch (e: Exception) {
            CompileResult.Error(e.message ?: "Invalid expression.")
        }
    }

    fun validateExpression(rawExpr: String, allowVariableX: Boolean = true): String? =
        when (val res = compile(rawExpr, allowVariableX)) {
            is CompileResult.Success -> null
            is CompileResult.Error -> res.message
        }

    fun evaluate(rawExpr: String, isDegrees: Boolean = true, xValue: Double? = null): Double? {
        return when (val compiled = compile(rawExpr, allowVariableX = xValue != null)) {
            is CompileResult.Success -> compiled.expr.evaluate(isDegrees = isDegrees, xValue = xValue)
            is CompileResult.Error -> null
        }
    }

    private class Parser(
        private val str: String,
        private val allowVariableX: Boolean,
    ) {
        var pos = 0

        fun parseExpression(): AstNode {
            var x = parseTerm()
            while (pos < str.length) {
                when (str[pos]) {
                    '+' -> {
                        pos++
                        x = BinaryNode('+', x, parseTerm())
                    }
                    '-' -> {
                        pos++
                        x = BinaryNode('-', x, parseTerm())
                    }
                    else -> return x
                }
            }
            return x
        }

        private fun canStartImplicitFactor(): Boolean {
            if (pos >= str.length) return false
            val c = str[pos]
            return c == '(' || c == 'π' || c == '√' || c.isLetter() || c.isDigit() || c == '.'
        }

        private fun parseTerm(): AstNode {
            var x = parseUnary()
            while (pos < str.length) {
                when {
                    str[pos] == '*' -> {
                        pos++
                        x = BinaryNode('*', x, parseUnary())
                    }
                    str[pos] == '/' -> {
                        pos++
                        x = BinaryNode('/', x, parseUnary())
                    }
                    canStartImplicitFactor() -> {
                        x = BinaryNode('*', x, parseUnary())
                    }
                    else -> return x
                }
            }
            return x
        }

        private fun parseUnary(): AstNode {
            if (pos < str.length && str[pos] == '+') {
                pos++
                return parseUnary()
            }
            if (pos < str.length && str[pos] == '-') {
                pos++
                return UnaryNegNode(parseUnary())
            }
            return parsePower()
        }

        private fun parsePower(): AstNode {
            val base = parsePostfix()
            if (pos < str.length && str[pos] == '^') {
                pos++
                val exponent = parseUnary()
                return BinaryNode('^', base, exponent)
            }
            return base
        }

        private fun parsePostfix(): AstNode {
            var x = parsePrimary()
            while (pos < str.length) {
                when (str[pos]) {
                    '!' -> {
                        pos++
                        x = FactorialNode(x)
                    }
                    '%' -> {
                        pos++
                        x = PercentNode(x)
                    }
                    else -> return x
                }
            }
            return x
        }

        private fun parsePrimary(): AstNode {
            if (pos >= str.length) throw IllegalArgumentException("Incomplete expression.")

            val ch = str[pos]
            if (ch == '(') {
                pos++
                val x = parseExpression()
                if (pos >= str.length || str[pos] != ')') {
                    throw IllegalArgumentException("Missing closing parenthesis ')'.")
                }
                pos++
                return x
            }

            if (ch == 'π') {
                pos++
                return ConstNode(PI)
            }

            if (ch == '√') {
                pos++
                return SqrtNode(parsePrimary())
            }

            if (ch.isDigit() || ch == '.') {
                val start = pos
                var dotCount = 0
                var digitCount = 0
                while (pos < str.length && (str[pos].isDigit() || str[pos] == '.')) {
                    if (str[pos] == '.') dotCount++ else digitCount++
                    pos++
                }
                if (dotCount > 1 || digitCount == 0) {
                    throw IllegalArgumentException("Invalid number format.")
                }
                val num = str.substring(start, pos).toDoubleOrNull()
                    ?: throw IllegalArgumentException("Invalid number format.")
                return ConstNode(num)
            }

            if (ch.isLetter()) {
                val start = pos
                while (pos < str.length && str[pos].isLetter()) pos++
                val name = str.substring(start, pos)
                if (name == "e") return ConstNode(kotlin.math.E)
                if (name == "pi") return ConstNode(PI)
                if (name == "x") {
                    if (!allowVariableX) {
                        throw IllegalArgumentException("Variable 'x' is not allowed here.")
                    }
                    return VarXNode
                }

                if (name !in KNOWN_FUNCTIONS) {
                    throw IllegalArgumentException("Unknown function or symbol: '$name'.")
                }
                val arg = parseFunctionArg()
                return FnNode(name, arg)
            }

            throw IllegalArgumentException("Unexpected character '$ch'.")
        }

        private fun parseFunctionArg(): AstNode {
            return if (pos < str.length && str[pos] == '(') {
                pos++
                val v = parseExpression()
                if (pos >= str.length || str[pos] != ')') {
                    throw IllegalArgumentException("Missing closing parenthesis ')'.")
                }
                pos++
                v
            } else {
                parsePrimary()
            }
        }
    }

    private val KNOWN_FUNCTIONS = setOf(
        "sin", "cos", "tan",
        "asin", "acos", "atan",
        "sqrt", "log", "ln", "exp", "abs",
    )
}
