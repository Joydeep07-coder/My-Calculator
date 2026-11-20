package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppSettings
import com.example.model.NumberBase
import com.example.state.OmniCalcViewModel
import com.example.ui.components.CalcKeypadButton
import com.example.ui.components.ErrorStateBanner
import com.example.ui.components.KeypadKeyVariant
import com.example.ui.components.OmniTopBar
import com.example.ui.theme.JetBrainsMonoFontFamily
import kotlinx.coroutines.launch

enum class WordSize(val bits: Int, val label: String) {
    BIT_8(8, "8-bit"),
    BIT_16(16, "16-bit"),
    BIT_32(32, "32-bit"),
    BIT_64(64, "64-bit"),
}

sealed class ProgrammerEvalResult {
    data class Success(val value: Long) : ProgrammerEvalResult()
    data class Error(val message: String) : ProgrammerEvalResult()
}

fun maskToWordSize(value: Long, wordSize: WordSize): Long = when (wordSize) {
    WordSize.BIT_8 -> value and 0xFFL
    WordSize.BIT_16 -> value and 0xFFFFL
    WordSize.BIT_32 -> value and 0xFFFFFFFFL
    WordSize.BIT_64 -> value
}

fun maxUnsignedForWordSize(wordSize: WordSize): ULong = when (wordSize) {
    WordSize.BIT_8 -> 0xFFuL
    WordSize.BIT_16 -> 0xFFFFuL
    WordSize.BIT_32 -> 0xFFFFFFFFuL
    WordSize.BIT_64 -> ULong.MAX_VALUE
}

fun minSignedForWordSize(wordSize: WordSize): Long = when (wordSize) {
    WordSize.BIT_8 -> Byte.MIN_VALUE.toLong()
    WordSize.BIT_16 -> Short.MIN_VALUE.toLong()
    WordSize.BIT_32 -> Int.MIN_VALUE.toLong()
    WordSize.BIT_64 -> Long.MIN_VALUE
}

fun maxSignedForWordSize(wordSize: WordSize): Long = when (wordSize) {
    WordSize.BIT_8 -> Byte.MAX_VALUE.toLong()
    WordSize.BIT_16 -> Short.MAX_VALUE.toLong()
    WordSize.BIT_32 -> Int.MAX_VALUE.toLong()
    WordSize.BIT_64 -> Long.MAX_VALUE
}

fun normalizeProgrammerValue(
    value: Long,
    wordSize: WordSize,
    isSigned: Boolean,
): Long {
    val masked = maskToWordSize(value, wordSize)
    return if (isSigned) {
        when (wordSize) {
            WordSize.BIT_8 -> masked.toByte().toLong()
            WordSize.BIT_16 -> masked.toShort().toLong()
            WordSize.BIT_32 -> masked.toInt().toLong()
            WordSize.BIT_64 -> masked
        }
    } else {
        masked
    }
}

fun formatBinaryNibbles(
    value: Long,
    wordSize: WordSize = WordSize.BIT_64,
): String {
    val bits = maskToWordSize(value, wordSize)
    val raw = bits.toULong().toString(2)
    val pad = (4 - raw.length % 4) % 4
    return ("0".repeat(pad) + raw).chunked(4).joinToString(" ")
}

fun formatProgrammerValue(
    value: Long,
    base: NumberBase,
    wordSize: WordSize,
    isSigned: Boolean,
): String {
    val bits = maskToWordSize(value, wordSize)
    return when (base) {
        NumberBase.DEC -> {
            if (isSigned) {
                normalizeProgrammerValue(bits, wordSize, true).toString(10)
            } else {
                bits.toULong().toString(10)
            }
        }
        NumberBase.HEX -> bits.toULong().toString(16).uppercase()
        NumberBase.OCT -> bits.toULong().toString(8)
        NumberBase.BIN -> formatBinaryNibbles(bits, wordSize)
    }
}

fun appendProgrammerDigit(
    currentValue: Long,
    digitChar: Char,
    activeBase: NumberBase,
    wordSize: WordSize,
    isSigned: Boolean,
    resetOnNextDigit: Boolean,
): Long? {
    val digit = digitChar.uppercaseChar()
    val startFresh = resetOnNextDigit || currentValue == 0L

    if (activeBase == NumberBase.DEC && isSigned) {
        val candidate = if (startFresh) {
            digit.toString()
        } else {
            normalizeProgrammerValue(currentValue, wordSize, true).toString(10) + digit
        }
        val parsed = candidate.toLongOrNull(10) ?: return null
        val minVal = minSignedForWordSize(wordSize)
        val maxVal = maxSignedForWordSize(wordSize)
        return if (parsed in minVal..maxVal) parsed else null
    }

    val currentBitsStr = if (startFresh) {
        ""
    } else {
        maskToWordSize(currentValue, wordSize).toULong().toString(activeBase.radix)
    }
    val candidate = currentBitsStr + digit
    val parsedULong = candidate.toULongOrNull(activeBase.radix) ?: return null
    if (parsedULong > maxUnsignedForWordSize(wordSize)) return null
    return normalizeProgrammerValue(parsedULong.toLong(), wordSize, isSigned)
}

fun backspaceProgrammerValue(
    currentValue: Long,
    activeBase: NumberBase,
    wordSize: WordSize,
    isSigned: Boolean,
): Long {
    val rawStr = if (activeBase == NumberBase.DEC && isSigned) {
        normalizeProgrammerValue(currentValue, wordSize, true).toString(10)
    } else {
        maskToWordSize(currentValue, wordSize).toULong().toString(activeBase.radix)
    }
    if (rawStr.length <= 1) return 0L
    val dropped = rawStr.dropLast(1)
    if (dropped == "-") return 0L
    return if (activeBase == NumberBase.DEC && isSigned) {
        dropped.toLongOrNull(10)?.let { normalizeProgrammerValue(it, wordSize, true) } ?: 0L
    } else {
        dropped.toULongOrNull(activeBase.radix)
            ?.let { normalizeProgrammerValue(it.toLong(), wordSize, isSigned) }
            ?: 0L
    }
}

fun negateProgrammerValue(
    currentValue: Long,
    wordSize: WordSize,
    isSigned: Boolean,
): Long {
    val bits = maskToWordSize(currentValue, wordSize)
    return normalizeProgrammerValue(-bits, wordSize, isSigned)
}

fun bitwiseNotProgrammerValue(
    currentValue: Long,
    wordSize: WordSize,
    isSigned: Boolean,
): Long {
    val bits = maskToWordSize(currentValue, wordSize)
    return normalizeProgrammerValue(bits.inv(), wordSize, isSigned)
}

fun evaluateProgrammerOp(
    a: Long,
    b: Long,
    op: String,
    wordSize: WordSize,
    isSigned: Boolean,
): ProgrammerEvalResult {
    val lhsSigned = normalizeProgrammerValue(a, wordSize, true)
    val rhsSigned = normalizeProgrammerValue(b, wordSize, true)
    val lhsBits = maskToWordSize(a, wordSize)
    val rhsBits = maskToWordSize(b, wordSize)

    return when (op) {
        "+" -> ProgrammerEvalResult.Success(
            normalizeProgrammerValue(lhsBits + rhsBits, wordSize, isSigned)
        )
        "−", "-" -> ProgrammerEvalResult.Success(
            normalizeProgrammerValue(lhsBits - rhsBits, wordSize, isSigned)
        )
        "×", "*" -> ProgrammerEvalResult.Success(
            normalizeProgrammerValue(lhsBits * rhsBits, wordSize, isSigned)
        )
        "÷", "/" -> {
            if (rhsBits == 0L) {
                ProgrammerEvalResult.Error("Cannot divide by zero")
            } else if (isSigned) {
                val quotient = lhsSigned / rhsSigned
                ProgrammerEvalResult.Success(normalizeProgrammerValue(quotient, wordSize, true))
            } else {
                val quotient = (lhsBits.toULong() / rhsBits.toULong()).toLong()
                ProgrammerEvalResult.Success(normalizeProgrammerValue(quotient, wordSize, false))
            }
        }
        "AND" -> ProgrammerEvalResult.Success(
            normalizeProgrammerValue(lhsBits and rhsBits, wordSize, isSigned)
        )
        "OR" -> ProgrammerEvalResult.Success(
            normalizeProgrammerValue(lhsBits or rhsBits, wordSize, isSigned)
        )
        "XOR" -> ProgrammerEvalResult.Success(
            normalizeProgrammerValue(lhsBits xor rhsBits, wordSize, isSigned)
        )
        "<<" -> {
            if (isSigned && rhsSigned < 0L) {
                ProgrammerEvalResult.Error("Shift count cannot be negative")
            } else {
                val shiftULong = if (isSigned) rhsSigned.toULong() else rhsBits.toULong()
                val shifted = if (shiftULong >= wordSize.bits.toULong()) {
                    0L
                } else {
                    lhsBits shl shiftULong.toInt()
                }
                ProgrammerEvalResult.Success(normalizeProgrammerValue(shifted, wordSize, isSigned))
            }
        }
        ">>" -> {
            if (isSigned && rhsSigned < 0L) {
                ProgrammerEvalResult.Error("Shift count cannot be negative")
            } else {
                val shiftULong = if (isSigned) rhsSigned.toULong() else rhsBits.toULong()
                val shifted = if (shiftULong >= wordSize.bits.toULong()) {
                    if (isSigned && lhsSigned < 0L) -1L else 0L
                } else if (isSigned) {
                    lhsSigned shr shiftULong.toInt()
                } else {
                    (lhsBits.toULong() shr shiftULong.toInt()).toLong()
                }
                ProgrammerEvalResult.Success(normalizeProgrammerValue(shifted, wordSize, isSigned))
            }
        }
        else -> ProgrammerEvalResult.Success(normalizeProgrammerValue(b, wordSize, isSigned))
    }
}

@Composable
fun ProgrammerCalculatorScreen(
    onBack: () -> Unit,
    viewModel: OmniCalcViewModel? = null,
) {
    val settingsFlow = viewModel?.settings
    val settings by settingsFlow?.collectAsState() ?: remember { mutableStateOf(AppSettings()) }
    val hapticEnabled = settings.hapticFeedback
    val soundEnabled = settings.buttonSound

    val haptic = LocalHapticFeedback.current
    val clipboard = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var activeBase by remember { mutableStateOf(NumberBase.DEC) }
    var wordSize by remember { mutableStateOf(WordSize.BIT_64) }
    var isSigned by remember { mutableStateOf(true) }
    var currentValue by remember { mutableLongStateOf(0L) }
    var pendingOp by remember { mutableStateOf<String?>(null) }
    var storedOperand by remember { mutableLongStateOf(0L) }
    var resetOnNextDigit by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun triggerHaptic() {
        if (hapticEnabled) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    fun copyNumericValue(base: NumberBase, formattedValue: String) {
        triggerHaptic()
        clipboard.setText(AnnotatedString(formattedValue))
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar("Copied ${base.label}: $formattedValue")
        }
    }

    fun appendDigit(digitChar: Char) {
        errorMessage = null
        val updated = appendProgrammerDigit(
            currentValue = currentValue,
            digitChar = digitChar,
            activeBase = activeBase,
            wordSize = wordSize,
            isSigned = isSigned,
            resetOnNextDigit = resetOnNextDigit,
        )
        if (updated != null) {
            currentValue = updated
            resetOnNextDigit = false
        }
    }

    fun applyOperation(op: String) {
        errorMessage = null
        if (op == "NOT") {
            currentValue = bitwiseNotProgrammerValue(currentValue, wordSize, isSigned)
            resetOnNextDigit = true
            return
        }
        if (pendingOp != null && !resetOnNextDigit) {
            when (val res = evaluateProgrammerOp(storedOperand, currentValue, pendingOp!!, wordSize, isSigned)) {
                is ProgrammerEvalResult.Success -> {
                    currentValue = res.value
                }
                is ProgrammerEvalResult.Error -> {
                    errorMessage = res.message
                    pendingOp = null
                    resetOnNextDigit = true
                    return
                }
            }
        }
        storedOperand = currentValue
        pendingOp = op
        resetOnNextDigit = true
    }

    fun evaluatePending() {
        val op = pendingOp ?: return
        when (val res = evaluateProgrammerOp(storedOperand, currentValue, op, wordSize, isSigned)) {
            is ProgrammerEvalResult.Success -> {
                currentValue = res.value
                errorMessage = null
            }
            is ProgrammerEvalResult.Error -> {
                errorMessage = res.message
            }
        }
        pendingOp = null
        resetOnNextDigit = true
    }

    fun negateCurrent() {
        errorMessage = null
        currentValue = negateProgrammerValue(currentValue, wordSize, isSigned)
        if (resetOnNextDigit && pendingOp == null) {
            resetOnNextDigit = false
        }
    }

    fun backspace() {
        errorMessage = null
        if (resetOnNextDigit) {
            currentValue = 0L
            resetOnNextDigit = false
            return
        }
        currentValue = backspaceProgrammerValue(currentValue, activeBase, wordSize, isSigned)
    }

    fun clearAll() {
        currentValue = 0L
        pendingOp = null
        storedOperand = 0L
        resetOnNextDigit = false
        errorMessage = null
    }

    fun updateWordSize(nextSize: WordSize) {
        triggerHaptic()
        wordSize = nextSize
        currentValue = normalizeProgrammerValue(currentValue, nextSize, isSigned)
        storedOperand = normalizeProgrammerValue(storedOperand, nextSize, isSigned)
        errorMessage = null
    }

    fun toggleSignedMode() {
        triggerHaptic()
        val nextSigned = !isSigned
        isSigned = nextSigned
        currentValue = normalizeProgrammerValue(currentValue, wordSize, nextSigned)
        storedOperand = normalizeProgrammerValue(storedOperand, wordSize, nextSigned)
        errorMessage = null
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            OmniTopBar(
                title = "Programmer Calculator",
                subtitle = "Base conversion, arithmetic & bitwise logic",
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
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Word Size & Signed/Unsigned Mode Selector Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    WordSize.entries.forEach { size ->
                        val selected = wordSize == size
                        ModeChipPill(
                            label = size.label,
                            selected = selected,
                            onClick = { updateWordSize(size) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("prog_word_${size.bits}"),
                        )
                    }
                    ModeChipPill(
                        label = if (isSigned) "Signed" else "Unsigned",
                        selected = !isSigned,
                        onClick = { toggleSignedMode() },
                        accentHighlight = true,
                        modifier = Modifier
                            .weight(1.15f)
                            .testTag("prog_signed_toggle"),
                    )
                }

                // Multi-base Display Card
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        if (pendingOp != null) {
                            val operandFormatted = formatProgrammerValue(
                                storedOperand,
                                activeBase,
                                wordSize,
                                isSigned,
                            )
                            Text(
                                text = "$operandFormatted $pendingOp",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontFamily = JetBrainsMonoFontFamily
                                ),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.testTag("prog_pending_expr"),
                            )
                        }

                        listOf(
                            NumberBase.HEX,
                            NumberBase.DEC,
                            NumberBase.OCT,
                            NumberBase.BIN,
                        ).forEach { base ->
                            val formatted = formatProgrammerValue(
                                value = currentValue,
                                base = base,
                                wordSize = wordSize,
                                isSigned = isSigned,
                            )
                            BaseDisplayRow(
                                base = base,
                                value = formatted,
                                selected = activeBase == base,
                                onSelect = {
                                    if (activeBase == base) {
                                        copyNumericValue(base, formatted)
                                    } else {
                                        triggerHaptic()
                                        activeBase = base
                                    }
                                },
                                onCopy = { copyNumericValue(base, formatted) },
                            )
                        }
                    }
                }

                if (!errorMessage.isNullOrBlank()) {
                    ErrorStateBanner(
                        message = errorMessage!!,
                        modifier = Modifier.testTag("prog_error_banner"),
                    )
                }

                // Bitwise Operations Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    listOf("AND", "OR", "XOR", "NOT", "<<", ">>").forEach { op ->
                        CalcKeypadButton(
                            label = op,
                            variant = KeypadKeyVariant.SCIENTIFIC,
                            onClick = { applyOperation(op) },
                            hapticEnabled = hapticEnabled,
                            soundEnabled = soundEnabled,
                            fontSize = 13.sp,
                            height = 42.dp,
                            modifier = Modifier.weight(1f),
                            testTag = "prog_op_$op",
                        )
                    }
                }

                // Hex + Numeric + Arithmetic Keypad (4 columns x 6 rows)
                val keypadRows = listOf(
                    listOf("A", "B", "C", "AC"),
                    listOf("D", "E", "F", "⌫"),
                    listOf("7", "8", "9", "÷"),
                    listOf("4", "5", "6", "×"),
                    listOf("1", "2", "3", "−"),
                    listOf("+/−", "0", "+", "="),
                )

                keypadRows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        row.forEach { key ->
                            val isEnabled = when (key) {
                                in "A".."F" -> activeBase == NumberBase.HEX
                                "8", "9" -> activeBase == NumberBase.DEC || activeBase == NumberBase.HEX
                                in "2".."7" -> activeBase != NumberBase.BIN
                                else -> true
                            }
                            val variant = when (key) {
                                "AC", "⌫", "+/−" -> KeypadKeyVariant.ACTION
                                "÷", "×", "−", "+" -> KeypadKeyVariant.OPERATOR
                                "=" -> KeypadKeyVariant.EQUALS
                                else -> KeypadKeyVariant.NUMBER
                            }
                            val tag = when (key) {
                                "=" -> "prog_key_equals"
                                "+/−" -> "prog_key_negate"
                                else -> "prog_key_$key"
                            }
                            CalcKeypadButton(
                                label = key,
                                variant = variant,
                                enabled = isEnabled,
                                hapticEnabled = hapticEnabled,
                                soundEnabled = soundEnabled,
                                onClick = {
                                    when (key) {
                                        "AC" -> clearAll()
                                        "⌫" -> backspace()
                                        "+/−" -> negateCurrent()
                                        "=" -> evaluatePending()
                                        "÷", "×", "−", "+" -> applyOperation(key)
                                        else -> appendDigit(key.first())
                                    }
                                },
                                height = 48.dp,
                                fontSize = 17.sp,
                                modifier = Modifier.weight(1f),
                                testTag = tag,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModeChipPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentHighlight: Boolean = false,
) {
    val containerColor = when {
        selected && accentHighlight -> MaterialTheme.colorScheme.secondaryContainer
        selected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceContainer
    }
    val textColor = when {
        selected && accentHighlight -> MaterialTheme.colorScheme.onSecondaryContainer
        selected -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = containerColor,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            else MaterialTheme.colorScheme.outlineVariant,
        ),
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                ),
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun BaseDisplayRow(
    base: NumberBase,
    value: String,
    selected: Boolean,
    onSelect: () -> Unit,
    onCopy: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.45f)
        },
        border = if (selected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onSelect)
            .testTag("prog_base_${base.label}"),
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = base.label,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                ),
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(42.dp),
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .testTag("prog_value_${base.label}"),
            )
            IconButton(
                onClick = onCopy,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("prog_copy_${base.label}"),
            ) {
                Icon(
                    imageVector = Icons.Rounded.ContentCopy,
                    contentDescription = "Copy ${base.label} value",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}
