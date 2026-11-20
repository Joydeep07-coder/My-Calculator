package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.SampleCatalogs
import com.example.data.SimpleExprHelper
import com.example.model.AngleMode
import com.example.navigation.AppRoutes
import com.example.state.OmniCalcViewModel
import com.example.ui.components.CalcKeypadButton
import com.example.ui.components.KeypadKeyVariant
import com.example.ui.theme.JetBrainsMonoFontFamily
import kotlinx.coroutines.launch

@Composable
fun CalculatorScreen(
    viewModel: OmniCalcViewModel,
    initialScientific: Boolean = false,
    onBack: (() -> Unit)? = null,
    onOpenHistory: () -> Unit,
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val reusedExpr by viewModel.reusedExpression.collectAsStateWithLifecycle()

    var isScientific by remember(initialScientific) { mutableStateOf(initialScientific) }
    var expression by remember { mutableStateOf("") }
    var previousExpression by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("0") }
    var justEvaluated by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current

    LaunchedEffect(reusedExpr) {
        if (!reusedExpr.isNullOrBlank()) {
            expression = reusedExpr!!
            justEvaluated = false
            val eval = SimpleExprHelper.evaluate(
                expression,
                isDegrees = settings.angleMode == AngleMode.DEG,
            )
            resultText = if (eval != null) {
                SampleCatalogs.formatNumber(eval, settings.decimalPrecision, settings.thousandsSeparator)
            } else {
                "0"
            }
            viewModel.setReusedExpression(null)
        }
    }

    fun evaluateWithPreviewFallback(expr: String, isDeg: Boolean): Double? {
        SimpleExprHelper.evaluate(expr, isDegrees = isDeg)?.let { return it }
        val openCount = expr.count { it == '(' }
        val closeCount = expr.count { it == ')' }
        if (openCount > closeCount) {
            return SimpleExprHelper.evaluate(expr + ")".repeat(openCount - closeCount), isDegrees = isDeg)
        }
        return null
    }

    fun updatePreview(expr: String) {
        if (expr.isBlank()) {
            resultText = "0"
            return
        }
        val isDeg = settings.angleMode == AngleMode.DEG
        val eval = evaluateWithPreviewFallback(expr, isDeg)
        if (eval != null) {
            resultText = SampleCatalogs.formatNumber(
                eval,
                settings.decimalPrecision,
                settings.thousandsSeparator,
            )
        } else {
            val last = expr.last()
            val isIncomplete = last in setOf('+', '−', '-', '×', '÷', '^', '(', '.')
            if (!isIncomplete) {
                resultText = "Error"
            }
        }
    }

    LaunchedEffect(settings.angleMode, settings.decimalPrecision, settings.thousandsSeparator) {
        if (expression.isNotBlank() && !justEvaluated) {
            updatePreview(expression)
        }
    }

    fun appendToken(token: String) {
        val binaryOps = setOf("+", "×", "÷", "^")
        val continuationTokens = setOf("+", "−", "×", "÷", "%", "^", "^2", "^3", "!")

        if (justEvaluated) {
            expression = if (token in continuationTokens) {
                expression + token
            } else if (token == ".") {
                "0."
            } else {
                token
            }
            justEvaluated = false
            updatePreview(expression)
            return
        }

        if (token == ".") {
            val currentNumberSegment = expression.takeLastWhile { it.isDigit() || it == '.' }
            if (currentNumberSegment.contains('.')) return
            expression += if (currentNumberSegment.isEmpty()) "0." else "."
            justEvaluated = false
            updatePreview(expression)
            return
        }

        if (expression.isEmpty() && (token in binaryOps || token in setOf("%", "^2", "^3", "!"))) {
            return
        }

        val lastChar = expression.lastOrNull()
        val prevChar = if (expression.length >= 2) expression[expression.length - 2] else null
        if (token in binaryOps && lastChar == '(') {
            return
        }
        if (token in binaryOps && lastChar != null && lastChar in setOf('+', '−', '×', '÷', '^')) {
            if (lastChar == '−' && (expression.length == 1 || prevChar in setOf('×', '÷', '^', '('))) return
            expression = expression.dropLast(1) + token
        } else if (token == "−" && lastChar != null && lastChar in setOf('+', '−')) {
            expression = expression.dropLast(1) + token
        } else {
            expression += token
        }

        justEvaluated = false
        updatePreview(expression)
    }

    fun toggleSmartParentheses() {
        val openCount = expression.count { it == '(' }
        val closeCount = expression.count { it == ')' }
        val lastChar = expression.lastOrNull()
        val token = if (
            openCount > closeCount &&
            lastChar != null &&
            (lastChar.isDigit() || lastChar == ')' || lastChar == 'π' || lastChar == 'e' || lastChar == '!' || lastChar == '%')
        ) {
            ")"
        } else {
            "("
        }
        appendToken(token)
    }

    fun onEquals() {
        if (expression.isBlank()) return
        val eval = SimpleExprHelper.evaluate(
            expression,
            isDegrees = settings.angleMode == AngleMode.DEG,
        )
        if (eval != null) {
            val formatted = SampleCatalogs.formatNumber(
                eval,
                settings.decimalPrecision,
                settings.thousandsSeparator,
            )
            previousExpression = "$expression ="
            resultText = formatted
            viewModel.addHistoryEntry(
                expression = expression,
                result = formatted,
                toolName = if (isScientific) "Scientific" else "Standard",
                reusableRoute = if (isScientific) AppRoutes.SCIENTIFIC_CALCULATOR else AppRoutes.STANDARD_CALCULATOR,
            )
            expression = formatted.replace(",", "")
            justEvaluated = true
        } else {
            resultText = "Error"
            justEvaluated = false
        }
    }

    fun onBackspace() {
        if (expression.isNotEmpty()) {
            val functionTokens = listOf(
                "sin⁻¹(", "cos⁻¹(", "tan⁻¹(",
                "sin(", "cos(", "tan(",
                "log(", "ln(", "abs(", "√("
            )
            val matchedToken = functionTokens.firstOrNull { expression.endsWith(it) }
            expression = if (matchedToken != null) {
                expression.dropLast(matchedToken.length)
            } else {
                expression.dropLast(1)
            }
            justEvaluated = false
            updatePreview(expression)
        }
    }

    fun onClear() {
        expression = ""
        previousExpression = ""
        resultText = "0"
        justEvaluated = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 8.dp),
        ) {
            // Top controls row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onBack != null) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("calc_back_button"),
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back",
                            )
                        }
                    }

                    // Mode toggle pill
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    ) {
                        Row(modifier = Modifier.padding(3.dp)) {
                            ModePillItem(
                                label = "Standard",
                                selected = !isScientific,
                                onClick = { isScientific = false },
                                testTag = "mode_standard_tab",
                            )
                            ModePillItem(
                                label = "Scientific",
                                selected = isScientific,
                                onClick = { isScientific = true },
                                testTag = "mode_scientific_tab",
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    AnimatedVisibility(visible = isScientific) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .clip(RoundedCornerShape(50))
                                .clickable {
                                    val next = if (settings.angleMode == AngleMode.DEG) {
                                        AngleMode.RAD
                                    } else {
                                        AngleMode.DEG
                                    }
                                    viewModel.setAngleMode(next)
                                    updatePreview(expression)
                                }
                                .testTag("angle_mode_toggle"),
                        ) {
                            Text(
                                text = settings.angleMode.label,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            )
                        }
                    }

                    IconButton(
                        onClick = onOpenHistory,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("calc_history_button"),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.History,
                            contentDescription = "Calculation history",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                val isWide = maxWidth >= 700.dp
                val availableHeight = maxHeight

                if (isWide) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        CalculatorDisplayCard(
                            previousExpression = previousExpression,
                            expression = expression,
                            resultText = resultText,
                            onCopyResult = {
                                clipboard.setText(AnnotatedString(resultText))
                                scope.launch { snackbarHostState.showSnackbar("Copied $resultText") }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                        )

                        Column(
                            modifier = Modifier
                                .weight(1.25f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.Bottom,
                        ) {
                            CalculatorKeypads(
                                isScientific = isScientific,
                                hapticEnabled = settings.hapticFeedback,
                                soundEnabled = settings.buttonSound,
                                onToken = ::appendToken,
                                onParen = ::toggleSmartParentheses,
                                onClear = ::onClear,
                                onBackspace = ::onBackspace,
                                onEquals = ::onEquals,
                                compactKeyHeight = 52.dp,
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .widthIn(max = 520.dp)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween,
                    ) {
                        CalculatorDisplayCard(
                            previousExpression = previousExpression,
                            expression = expression,
                            resultText = resultText,
                            onCopyResult = {
                                clipboard.setText(AnnotatedString(resultText))
                                scope.launch { snackbarHostState.showSnackbar("Copied $resultText") }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        val adaptiveKeyHeight = if (isScientific) {
                            if (availableHeight < 480.dp) ((availableHeight - 220.dp) / 5f).coerceIn(40.dp, 54.dp) else 54.dp
                        } else {
                            if (availableHeight < 450.dp) ((availableHeight - 120.dp) / 5f).coerceIn(44.dp, 64.dp) else 64.dp
                        }

                        CalculatorKeypads(
                            isScientific = isScientific,
                            hapticEnabled = settings.hapticFeedback,
                            soundEnabled = settings.buttonSound,
                            onToken = ::appendToken,
                            onParen = ::toggleSmartParentheses,
                            onClear = ::onClear,
                            onBackspace = ::onBackspace,
                            onEquals = ::onEquals,
                            compactKeyHeight = adaptiveKeyHeight,
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun ModePillItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String,
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (selected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .testTag(testTag),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
        )
    }
}

@Composable
private fun CalculatorDisplayCard(
    previousExpression: String,
    expression: String,
    resultText: String,
    onCopyResult: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val exprScroll = rememberScrollState()
    LaunchedEffect(expression) {
        exprScroll.animateScrollTo(exprScroll.maxValue)
    }

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onCopyResult,
                    modifier = Modifier.minimumInteractiveComponentSize(),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ContentCopy,
                        contentDescription = "Copy result",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                }

                Text(
                    text = previousExpression,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = JetBrainsMonoFontFamily
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(exprScroll),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Text(
                        text = expression.ifEmpty { "0" },
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 26.sp,
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End,
                        maxLines = 1,
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                val dynamicSize = when {
                    resultText.length > 14 -> 30.sp
                    resultText.length > 10 -> 38.sp
                    else -> 46.sp
                }

                Text(
                    text = resultText,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = dynamicSize,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("calculator_result_display"),
                )
            }
        }
    }
}

@Composable
private fun CalculatorKeypads(
    isScientific: Boolean,
    hapticEnabled: Boolean,
    soundEnabled: Boolean,
    onToken: (String) -> Unit,
    onParen: () -> Unit,
    onClear: () -> Unit,
    onBackspace: () -> Unit,
    onEquals: () -> Unit,
    compactKeyHeight: androidx.compose.ui.unit.Dp,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AnimatedVisibility(visible = isScientific) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                val sciRows = listOf(
                    listOf(
                        "sin" to "sin(",
                        "cos" to "cos(",
                        "tan" to "tan(",
                        "sin⁻¹" to "sin⁻¹(",
                        "cos⁻¹" to "cos⁻¹(",
                        "tan⁻¹" to "tan⁻¹(",
                    ),
                    listOf(
                        "log" to "log(",
                        "ln" to "ln(",
                        "√" to "√(",
                        "x²" to "^2",
                        "x³" to "^3",
                        "xʸ" to "^",
                    ),
                    listOf(
                        "π" to "π",
                        "e" to "e",
                        "x!" to "!",
                        "|x|" to "abs(",
                        "(" to "(",
                        ")" to ")",
                    ),
                )
                sciRows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        row.forEach { (label, token) ->
                            CalcKeypadButton(
                                label = label,
                                variant = KeypadKeyVariant.SCIENTIFIC,
                                onClick = { onToken(token) },
                                hapticEnabled = hapticEnabled,
                                soundEnabled = soundEnabled,
                                fontSize = 13.sp,
                                height = 42.dp,
                                modifier = Modifier.weight(1f),
                                testTag = "sci_key_$label",
                            )
                        }
                    }
                }
            }
        }

        val rows = listOf(
            listOf("AC", "( )", "%", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "−"),
            listOf("1", "2", "3", "+"),
            listOf("0", ".", "⌫", "="),
        )

        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { key ->
                    val variant = when (key) {
                        "AC", "( )", "%", "⌫" -> KeypadKeyVariant.ACTION
                        "÷", "×", "−", "+" -> KeypadKeyVariant.OPERATOR
                        "=" -> KeypadKeyVariant.EQUALS
                        else -> KeypadKeyVariant.NUMBER
                    }
                    CalcKeypadButton(
                        label = key,
                        variant = variant,
                        onClick = {
                            when (key) {
                                "AC" -> onClear()
                                "( )" -> onParen()
                                "⌫" -> onBackspace()
                                "=" -> onEquals()
                                else -> onToken(key)
                            }
                        },
                        hapticEnabled = hapticEnabled,
                        soundEnabled = soundEnabled,
                        height = compactKeyHeight,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}
