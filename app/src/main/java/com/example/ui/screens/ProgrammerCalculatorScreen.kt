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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NumberBase
import com.example.ui.components.CalcKeypadButton
import com.example.ui.components.KeypadKeyVariant
import com.example.ui.components.OmniTopBar
import com.example.ui.theme.JetBrainsMonoFontFamily

@Composable
fun ProgrammerCalculatorScreen(
    onBack: () -> Unit,
) {
    var activeBase by remember { mutableStateOf(NumberBase.DEC) }
    var currentValue by remember { mutableLongStateOf(0L) }
    var pendingOp by remember { mutableStateOf<String?>(null) }
    var storedOperand by remember { mutableLongStateOf(0L) }
    var resetOnNextDigit by remember { mutableStateOf(false) }

    fun formatBinaryNibbles(v: Long): String {
        val raw = v.toString(2)
        val pad = (4 - raw.length % 4) % 4
        return ("0".repeat(pad) + raw).chunked(4).joinToString(" ")
    }

    fun appendDigit(digitChar: Char) {
        val baseStr = if (resetOnNextDigit || currentValue == 0L) {
            resetOnNextDigit = false
            digitChar.toString()
        } else {
            currentValue.toString(activeBase.radix) + digitChar
        }
        val parsed = baseStr.toLongOrNull(activeBase.radix)
        if (parsed != null) {
            currentValue = parsed
        }
    }

    fun applyBitwiseOp(op: String) {
        if (op == "NOT") {
            currentValue = currentValue.inv() and 0xFFFFFFFFL
            return
        }
        if (pendingOp != null && !resetOnNextDigit) {
            currentValue = evaluateBitwise(storedOperand, currentValue, pendingOp!!)
        }
        storedOperand = currentValue
        pendingOp = op
        resetOnNextDigit = true
    }

    fun evaluatePending() {
        if (pendingOp != null) {
            currentValue = evaluateBitwise(storedOperand, currentValue, pendingOp!!)
            pendingOp = null
            resetOnNextDigit = true
        }
    }

    fun backspace() {
        val s = currentValue.toString(activeBase.radix)
        currentValue = if (s.length <= 1) 0L else s.dropLast(1).toLongOrNull(activeBase.radix) ?: 0L
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            OmniTopBar(
                title = "Programmer Calculator",
                subtitle = "Base conversion & bitwise logic",
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
                // Multi-base Display Card
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        if (pendingOp != null) {
                            Text(
                                text = "${storedOperand.toString(activeBase.radix).uppercase()} $pendingOp",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontFamily = JetBrainsMonoFontFamily
                                ),
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }

                        BaseDisplayRow(
                            base = NumberBase.HEX,
                            value = currentValue.toString(16).uppercase(),
                            selected = activeBase == NumberBase.HEX,
                            onClick = { activeBase = NumberBase.HEX },
                        )
                        BaseDisplayRow(
                            base = NumberBase.DEC,
                            value = currentValue.toString(10),
                            selected = activeBase == NumberBase.DEC,
                            onClick = { activeBase = NumberBase.DEC },
                        )
                        BaseDisplayRow(
                            base = NumberBase.OCT,
                            value = currentValue.toString(8),
                            selected = activeBase == NumberBase.OCT,
                            onClick = { activeBase = NumberBase.OCT },
                        )
                        BaseDisplayRow(
                            base = NumberBase.BIN,
                            value = formatBinaryNibbles(currentValue),
                            selected = activeBase == NumberBase.BIN,
                            onClick = { activeBase = NumberBase.BIN },
                        )
                    }
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
                            onClick = { applyBitwiseOp(op) },
                            fontSize = 13.sp,
                            height = 44.dp,
                            modifier = Modifier.weight(1f),
                            testTag = "prog_op_$op",
                        )
                    }
                }

                // Hex + Numeric Keypad (4 columns x 5 rows)
                val keypadRows = listOf(
                    listOf("A", "B", "C", "AC"),
                    listOf("D", "E", "F", "⌫"),
                    listOf("7", "8", "9", "<<"),
                    listOf("4", "5", "6", ">>"),
                    listOf("1", "2", "3", "0"),
                )

                keypadRows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        row.forEach { key ->
                            val isEnabled = when (key) {
                                "AC", "⌫", "<<", ">>" -> true
                                in "A".."F" -> activeBase == NumberBase.HEX
                                "8", "9" -> activeBase == NumberBase.DEC || activeBase == NumberBase.HEX
                                in "2".."7" -> activeBase != NumberBase.BIN
                                "0", "1" -> true
                                else -> true
                            }
                            val variant = when (key) {
                                "AC", "⌫" -> KeypadKeyVariant.ACTION
                                "<<", ">>" -> KeypadKeyVariant.OPERATOR
                                else -> KeypadKeyVariant.NUMBER
                            }
                            CalcKeypadButton(
                                label = key,
                                variant = variant,
                                enabled = isEnabled,
                                onClick = {
                                    when (key) {
                                        "AC" -> {
                                            currentValue = 0L
                                            pendingOp = null
                                            storedOperand = 0L
                                        }
                                        "⌫" -> backspace()
                                        "<<", ">>" -> applyBitwiseOp(key)
                                        else -> appendDigit(key.first())
                                    }
                                },
                                height = 52.dp,
                                fontSize = 18.sp,
                                modifier = Modifier.weight(1f),
                                testTag = "prog_key_$key",
                            )
                        }
                    }
                }

                // Full-width Equals button
                CalcKeypadButton(
                    label = "=",
                    variant = KeypadKeyVariant.EQUALS,
                    onClick = { evaluatePending() },
                    height = 54.dp,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "prog_key_equals",
                )
            }
        }
    }
}

@Composable
private fun BaseDisplayRow(
    base: NumberBase,
    value: String,
    selected: Boolean,
    onClick: () -> Unit,
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
            .clickable(onClick = onClick)
            .testTag("prog_base_${base.label}"),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
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
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun evaluateBitwise(a: Long, b: Long, op: String): Long = when (op) {
    "AND" -> a and b
    "OR" -> a or b
    "XOR" -> a xor b
    "<<" -> a shl (b.toInt().coerceIn(0, 31))
    ">>" -> a shr (b.toInt().coerceIn(0, 31))
    else -> b
}
