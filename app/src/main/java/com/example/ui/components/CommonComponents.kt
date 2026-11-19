package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ShowChart
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.CurrencyExchange
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Functions
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.MonitorWeight
import androidx.compose.material.icons.rounded.Percent
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ToolCategory
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentPink
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.VioletPrimary
import com.example.ui.theme.VioletSoft

fun toolIconFor(toolId: String): ImageVector = when (toolId) {
    "standard_calc" -> Icons.Rounded.Calculate
    "scientific_calc" -> Icons.Rounded.Functions
    "gst_calc" -> Icons.Rounded.ReceiptLong
    "finance_hub" -> Icons.Rounded.AccountBalance
    "emi_calc" -> Icons.Rounded.AccountBalance
    "simple_interest" -> Icons.Rounded.Savings
    "compound_interest" -> Icons.Rounded.TrendingUp
    "discount_calc" -> Icons.Rounded.LocalOffer
    "unit_converter" -> Icons.Rounded.Straighten
    "currency_converter" -> Icons.Rounded.CurrencyExchange
    "age_calc" -> Icons.Rounded.Cake
    "bmi_calc" -> Icons.Rounded.MonitorWeight
    "programmer_calc" -> Icons.Rounded.Code
    "graph_plotter" -> Icons.AutoMirrored.Rounded.ShowChart
    else -> Icons.Rounded.Percent
}

fun toolAccentFor(category: ToolCategory): Color = when (category) {
    ToolCategory.CORE -> VioletPrimary
    ToolCategory.FINANCE -> AccentEmerald
    ToolCategory.CONVERTERS -> AccentCyan
    ToolCategory.HEALTH_DATE -> AccentPink
    ToolCategory.TECH -> AccentAmber
}

@Composable
fun OmniLogoMark(
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val corner = w * 0.24f
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF281F4D), Color(0xFF171525))
            ),
            cornerRadius = CornerRadius(corner, corner),
        )
        drawRoundRect(
            color = VioletPrimary,
            cornerRadius = CornerRadius(corner, corner),
            style = Stroke(width = w * 0.06f),
        )
        // Top display bar
        drawRoundRect(
            color = VioletSoft,
            topLeft = Offset(w * 0.22f, h * 0.20f),
            size = Size(w * 0.56f, h * 0.16f),
            cornerRadius = CornerRadius(w * 0.05f, w * 0.05f),
        )
        // Keypad dots
        val dotSize = w * 0.14f
        val dotCorner = CornerRadius(w * 0.04f, w * 0.04f)
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(w * 0.22f, h * 0.45f),
            size = Size(dotSize, dotSize),
            cornerRadius = dotCorner,
        )
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(w * 0.42f, h * 0.45f),
            size = Size(dotSize, dotSize),
            cornerRadius = dotCorner,
        )
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(w * 0.22f, h * 0.65f),
            size = Size(dotSize, dotSize),
            cornerRadius = dotCorner,
        )
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(w * 0.42f, h * 0.65f),
            size = Size(dotSize, dotSize),
            cornerRadius = dotCorner,
        )
        // Equals pill
        drawRoundRect(
            color = VioletPrimary,
            topLeft = Offset(w * 0.62f, h * 0.45f),
            size = Size(w * 0.16f, h * 0.34f),
            cornerRadius = CornerRadius(w * 0.06f, w * 0.06f),
        )
    }
}

@Composable
fun OmniTopBar(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("top_bar_back"),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Navigate back",
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
        } else {
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            content = actions,
        )
    }
}

@Composable
fun OmniCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val baseModifier = if (onClick != null) {
        modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
    } else {
        modifier
    }
    Surface(
        modifier = baseModifier,
        shape = MaterialTheme.shapes.medium,
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content,
        )
    }
}

@Composable
fun OmniResultCard(
    label: String,
    primaryValue: String,
    modifier: Modifier = Modifier,
    badgeText: String? = null,
    onCopy: ((String) -> Unit)? = null,
    breakdown: List<Pair<String, String>> = emptyList(),
) {
    val clipboard = LocalClipboardManager.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    if (!badgeText.isNullOrBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primary,
                        ) {
                            Text(
                                text = badgeText,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            )
                        }
                    }
                }
                IconButton(
                    onClick = {
                        clipboard.setText(AnnotatedString(primaryValue))
                        onCopy?.invoke(primaryValue)
                    },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("copy_result_button"),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ContentCopy,
                        contentDescription = "Copy result",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            Text(
                text = primaryValue,
                style = MaterialTheme.typography.displayMedium.copy(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            if (breakdown.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                breakdown.forEachIndexed { index, (k, v) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = k,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = v,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = JetBrainsMonoFontFamily
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    if (index < breakdown.lastIndex) {
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun OmniInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    prefixText: String? = null,
    suffixText: String? = null,
    keyboardType: KeyboardType = KeyboardType.Decimal,
    testTag: String = "omni_input",
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = if (placeholder.isNotEmpty()) {
            { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else null,
        prefix = if (!prefixText.isNullOrBlank()) {
            {
                Text(
                    text = prefixText,
                    fontFamily = JetBrainsMonoFontFamily,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        } else null,
        suffix = if (!suffixText.isNullOrBlank()) {
            {
                Text(
                    text = suffixText,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else null,
        trailingIcon = if (value.isNotEmpty()) {
            {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Clear input",
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        } else null,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        textStyle = MaterialTheme.typography.titleMedium.copy(
            fontFamily = JetBrainsMonoFontFamily
        ),
        shape = MaterialTheme.shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag),
    )
}

enum class KeypadKeyVariant {
    NUMBER,
    OPERATOR,
    ACTION,
    EQUALS,
    SCIENTIFIC,
}

@Composable
fun CalcKeypadButton(
    label: String,
    variant: KeypadKeyVariant,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    hapticEnabled: Boolean = true,
    fontSize: TextUnit = 22.sp,
    height: Dp = 62.dp,
    testTag: String = "key_$label",
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.93f else 1f,
        animationSpec = spring(stiffness = 700f),
        label = "keypad_scale",
    )

    val containerColor = when (variant) {
        KeypadKeyVariant.NUMBER -> MaterialTheme.colorScheme.surfaceContainerHigh
        KeypadKeyVariant.OPERATOR -> MaterialTheme.colorScheme.primaryContainer
        KeypadKeyVariant.ACTION -> MaterialTheme.colorScheme.surfaceVariant
        KeypadKeyVariant.EQUALS -> MaterialTheme.colorScheme.primary
        KeypadKeyVariant.SCIENTIFIC -> MaterialTheme.colorScheme.surfaceContainer
    }.let { if (enabled) it else it.copy(alpha = 0.35f) }

    val contentColor = when (variant) {
        KeypadKeyVariant.NUMBER -> MaterialTheme.colorScheme.onSurface
        KeypadKeyVariant.OPERATOR -> MaterialTheme.colorScheme.onPrimaryContainer
        KeypadKeyVariant.ACTION -> MaterialTheme.colorScheme.onSurfaceVariant
        KeypadKeyVariant.EQUALS -> MaterialTheme.colorScheme.onPrimary
        KeypadKeyVariant.SCIENTIFIC -> MaterialTheme.colorScheme.onSurface
    }.let { if (enabled) it else it.copy(alpha = 0.4f) }

    val shape = if (variant == KeypadKeyVariant.SCIENTIFIC) {
        RoundedCornerShape(14.dp)
    } else {
        RoundedCornerShape(20.dp)
    }

    Surface(
        modifier = modifier
            .height(height)
            .minimumInteractiveComponentSize()
            .scale(scale)
            .clip(shape)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = androidx.compose.material3.ripple(),
                onClick = {
                    if (hapticEnabled) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                    onClick()
                },
            )
            .testTag(testTag),
        shape = shape,
        color = containerColor,
        border = if (variant == KeypadKeyVariant.SCIENTIFIC) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        } else null,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontFamily = if (variant == KeypadKeyVariant.NUMBER) {
                    JetBrainsMonoFontFamily
                } else {
                    MaterialTheme.typography.titleMedium.fontFamily
                },
                fontSize = fontSize,
                fontWeight = if (variant == KeypadKeyVariant.EQUALS || variant == KeypadKeyVariant.OPERATOR) {
                    FontWeight.Bold
                } else {
                    FontWeight.SemiBold
                },
                color = contentColor,
                maxLines = 1,
            )
        }
    }
}

@Composable
fun EmptyStateView(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(34.dp),
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (!actionLabel.isNullOrBlank() && onAction != null) {
            Spacer(modifier = Modifier.height(18.dp))
            Button(onClick = onAction) {
                Text(actionLabel)
            }
        }
    }
}

@Composable
fun ErrorStateBanner(
    message: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.error.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Rounded.ErrorOutline,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
fun ConfirmActionDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
