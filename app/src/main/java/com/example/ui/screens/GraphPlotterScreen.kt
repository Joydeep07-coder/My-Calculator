package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.ZoomIn
import androidx.compose.material.icons.rounded.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SimpleExprHelper
import com.example.model.PlotFunction
import com.example.ui.components.OmniCard
import com.example.ui.components.OmniTopBar
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentPink
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.VioletPrimary
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraphPlotterScreen(
    onBack: () -> Unit,
) {
    val palette = remember {
        listOf(VioletPrimary, AccentCyan, AccentEmerald, AccentPink, AccentAmber)
    }
    var functions by remember {
        mutableStateOf<List<PlotFunction>>(emptyList())
    }
    var exprInput by remember { mutableStateOf("") }
    var zoomRange by remember { mutableFloatStateOf(6f) } // -zoomRange .. +zoomRange
    var showGrid by remember { mutableStateOf(true) }
    var showAxesLabels by remember { mutableStateOf(true) }
    var thickLines by remember { mutableStateOf(true) }
    var showGraphSettings by remember { mutableStateOf(false) }

    val textMeasurer = rememberTextMeasurer()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            OmniTopBar(
                title = "Graph Plotter",
                subtitle = "Multi-function coordinate canvas",
                onBack = onBack,
                actions = {
                    IconButton(
                        onClick = { showGraphSettings = true },
                        modifier = Modifier.testTag("graph_settings_button"),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Tune,
                            contentDescription = "Graph settings",
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
                    .widthIn(max = 720.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Graph Display Canvas
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(270.dp),
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        PlotCanvas(
                            functions = functions,
                            xRange = zoomRange,
                            showGrid = showGrid,
                            showAxesLabels = showAxesLabels,
                            strokeWidthDp = if (thickLines) 3f else 1.8f,
                            textMeasurer = textMeasurer,
                            axisColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                            gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxSize(),
                        )

                        // Floating Zoom & Reset Controls
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            ) {
                                Row {
                                    IconButton(
                                        onClick = { zoomRange = (zoomRange * 0.75f).coerceAtLeast(2f) },
                                        modifier = Modifier.testTag("graph_zoom_in"),
                                    ) {
                                        Icon(Icons.Rounded.ZoomIn, contentDescription = "Zoom in")
                                    }
                                    IconButton(
                                        onClick = { zoomRange = (zoomRange * 1.35f).coerceAtMost(24f) },
                                        modifier = Modifier.testTag("graph_zoom_out"),
                                    ) {
                                        Icon(Icons.Rounded.ZoomOut, contentDescription = "Zoom out")
                                    }
                                    IconButton(
                                        onClick = { zoomRange = 6f },
                                        modifier = Modifier.testTag("graph_reset"),
                                    ) {
                                        Icon(Icons.Rounded.Refresh, contentDescription = "Reset graph")
                                    }
                                }
                            }
                        }
                    }
                }

                // Expression Input & Add Button
                OmniCard {
                    Text(
                        text = "Add Mathematical Function f(x)",
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedTextField(
                            value = exprInput,
                            onValueChange = { exprInput = it },
                            prefix = {
                                Text(
                                    "f(x) = ",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = JetBrainsMonoFontFamily
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("graph_expr_input"),
                        )
                        Button(
                            onClick = {
                                if (exprInput.isNotBlank()) {
                                    val nextColor = palette[functions.size % palette.size]
                                    functions = functions + PlotFunction(
                                        id = UUID.randomUUID().toString(),
                                        expression = exprInput.trim(),
                                        color = nextColor,
                                        isVisible = true,
                                    )
                                    exprInput = ""
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 15.dp),
                            modifier = Modifier.testTag("graph_add_button"),
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = "Add function")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf("sin(x)", "cos(x)", "x^2", "0.1*x^3", "sqrt(abs(x))")) { preset ->
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                onClick = { exprInput = preset },
                            ) {
                                Text(
                                    text = preset,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontFamily = JetBrainsMonoFontFamily
                                    ),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                )
                            }
                        }
                    }
                }

                // Active Function List
                Text(
                    text = "Plotted Functions (${functions.size})",
                    style = MaterialTheme.typography.titleMedium,
                )

                if (functions.isEmpty()) {
                    OmniCard(contentPadding = PaddingValues(14.dp)) {
                        Text(
                            text = "No functions plotted yet. Enter an expression or tap a preset above.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    functions.forEach { fn ->
                        OmniCard(contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(fn.color)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "f(x) = ${fn.expression}",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontFamily = JetBrainsMonoFontFamily
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f),
                                )
                                IconButton(
                                    onClick = {
                                        functions = functions.map {
                                            if (it.id == fn.id) it.copy(isVisible = !it.isVisible) else it
                                        }
                                    },
                                ) {
                                    Icon(
                                        imageVector = if (fn.isVisible) {
                                            Icons.Rounded.Visibility
                                        } else {
                                            Icons.Rounded.VisibilityOff
                                        },
                                        contentDescription = "Toggle function visibility",
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        functions = functions.filterNot { it.id == fn.id }
                                    },
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.DeleteOutline,
                                        contentDescription = "Remove function",
                                        tint = MaterialTheme.colorScheme.error,
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (showGraphSettings) {
            val sheetState = rememberModalBottomSheetState()
            ModalBottomSheet(
                onDismissRequest = { showGraphSettings = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("Graph Settings", style = MaterialTheme.typography.titleLarge)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Show Coordinate Grid", style = MaterialTheme.typography.bodyLarge)
                        Switch(checked = showGrid, onCheckedChange = { showGrid = it })
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Show Axis Numbers", style = MaterialTheme.typography.bodyLarge)
                        Switch(checked = showAxesLabels, onCheckedChange = { showAxesLabels = it })
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Bold Curve Strokes", style = MaterialTheme.typography.bodyLarge)
                        Switch(checked = thickLines, onCheckedChange = { thickLines = it })
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun PlotCanvas(
    functions: List<PlotFunction>,
    xRange: Float,
    showGrid: Boolean,
    showAxesLabels: Boolean,
    strokeWidthDp: Float,
    textMeasurer: TextMeasurer,
    axisColor: Color,
    gridColor: Color,
    labelColor: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val yRange = xRange * (h / w)

        if (showGrid) {
            val gridLines = 6
            for (i in -gridLines..gridLines) {
                val gx = cx + (i / gridLines.toFloat()) * (w / 2f)
                drawLine(gridColor, Offset(gx, 0f), Offset(gx, h), strokeWidth = 1f)
                val gy = cy + (i / gridLines.toFloat()) * (h / 2f)
                drawLine(gridColor, Offset(0f, gy), Offset(w, gy), strokeWidth = 1f)
            }
        }

        // X and Y Axes
        drawLine(axisColor, Offset(0f, cy), Offset(w, cy), strokeWidth = 2.5f)
        drawLine(axisColor, Offset(cx, 0f), Offset(cx, h), strokeWidth = 2.5f)

        if (showAxesLabels) {
            val style = TextStyle(
                color = labelColor,
                fontSize = 10.sp,
                fontFamily = JetBrainsMonoFontFamily,
            )
            val maxTick = xRange.toInt()
            drawText(
                textMeasurer = textMeasurer,
                text = "-$maxTick",
                topLeft = Offset(8f, cy + 4f),
                style = style,
            )
            drawText(
                textMeasurer = textMeasurer,
                text = "+$maxTick",
                topLeft = Offset(w - 30f, cy + 4f),
                style = style,
            )
        }

        // Plot each visible function
        val samples = 140
        functions.filter { it.isVisible }.forEach { fn ->
            val path = Path()
            var started = false
            for (i in 0..samples) {
                val px = (i / samples.toFloat()) * w
                val xVal = ((px - cx) / (w / 2f)) * xRange
                val yVal = SimpleExprHelper.evaluate(fn.expression, isDegrees = false, xValue = xVal.toDouble())
                if (yVal != null && yVal.isFinite()) {
                    val py = cy - ((yVal.toFloat() / yRange) * (h / 2f))
                    if (py in -h..2f * h) {
                        if (!started) {
                            path.moveTo(px, py)
                            started = true
                        } else {
                            path.lineTo(px, py)
                        }
                    } else {
                        started = false
                    }
                } else {
                    started = false
                }
            }
            drawPath(
                path = path,
                color = fn.color,
                style = Stroke(width = strokeWidthDp.dp.toPx(), cap = StrokeCap.Round),
            )
        }
    }
}
