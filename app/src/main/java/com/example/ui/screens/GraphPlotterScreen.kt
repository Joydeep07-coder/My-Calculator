package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleCatalogs
import com.example.data.SimpleExprHelper
import com.example.model.AngleMode
import com.example.model.AppSettings
import com.example.model.PlotFunction
import com.example.state.OmniCalcViewModel
import com.example.ui.components.ErrorStateBanner
import com.example.ui.components.OmniCard
import com.example.ui.components.OmniTopBar
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentPink
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.VioletPrimary
import java.util.UUID
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

sealed class PlotValidationResult {
    data class Valid(val normalizedExpression: String) : PlotValidationResult()
    data class Invalid(val message: String) : PlotValidationResult()
}

data class GraphPoint(val x: Double, val y: Double)

data class GraphViewport(
    val centerX: Float = 0f,
    val centerY: Float = 0f,
    val xHalfSpan: Float = 6f,
) {
    fun yHalfSpan(widthPx: Float, heightPx: Float): Float {
        if (widthPx <= 0f || heightPx <= 0f) return xHalfSpan
        return xHalfSpan * (heightPx / widthPx)
    }

    fun xMin(): Float = centerX - xHalfSpan
    fun xMax(): Float = centerX + xHalfSpan
    fun yMin(widthPx: Float, heightPx: Float): Float = centerY - yHalfSpan(widthPx, heightPx)
    fun yMax(widthPx: Float, heightPx: Float): Float = centerY + yHalfSpan(widthPx, heightPx)

    fun worldToScreen(x: Double, y: Double, widthPx: Float, heightPx: Float): Offset {
        val pxPerUnit = widthPx / (2.0 * xHalfSpan)
        val yMinWorld = yMin(widthPx, heightPx).toDouble()
        val px = ((x - xMin()) * pxPerUnit).toFloat()
        val py = (heightPx - (y - yMinWorld) * pxPerUnit).toFloat()
        return Offset(px, py)
    }

    fun screenToWorld(px: Float, py: Float, widthPx: Float, heightPx: Float): GraphPoint {
        val pxPerUnit = widthPx / (2.0 * xHalfSpan)
        val yMinWorld = yMin(widthPx, heightPx).toDouble()
        val x = xMin() + (px / pxPerUnit)
        val y = yMinWorld + ((heightPx - py) / pxPerUnit)
        return GraphPoint(x, y)
    }

    fun zoomedBy(scaleFactor: Float): GraphViewport {
        val nextSpan = (xHalfSpan * scaleFactor).coerceIn(0.5f, 50f)
        return copy(xHalfSpan = nextSpan)
    }

    fun pannedBy(dragDxPx: Float, dragDyPx: Float, widthPx: Float): GraphViewport {
        if (widthPx <= 0f) return this
        val pxPerUnit = widthPx / (2f * xHalfSpan)
        return copy(
            centerX = (centerX - dragDxPx / pxPerUnit).coerceIn(-500f, 500f),
            centerY = (centerY + dragDyPx / pxPerUnit).coerceIn(-500f, 500f),
        )
    }
}

private data class CompiledPlotItem(
    val fn: PlotFunction,
    val compiled: SimpleExprHelper.CompiledExpr,
)

fun normalizePlotExpressionInput(raw: String): String {
    val trimmed = raw.trim()
    val prefixRegex = Regex("^(?:y|f\\s*\\(\\s*x\\s*\\))\\s*=\\s*", RegexOption.IGNORE_CASE)
    return trimmed.replaceFirst(prefixRegex, "").trim()
}

fun validatePlotExpression(rawExpr: String): PlotValidationResult {
    if (rawExpr.isBlank()) {
        return PlotValidationResult.Invalid("Please enter a function of x (e.g. sin(x), x^2, 1/x).")
    }
    val normalized = normalizePlotExpressionInput(rawExpr)
    if (normalized.isEmpty()) {
        return PlotValidationResult.Invalid("Please enter an expression after '='.")
    }
    if (normalized.contains('=')) {
        return PlotValidationResult.Invalid("Enter an expression of x without '='.")
    }
    return when (val compileRes = SimpleExprHelper.compile(normalized, allowVariableX = true)) {
        is SimpleExprHelper.CompileResult.Success -> PlotValidationResult.Valid(normalized)
        is SimpleExprHelper.CompileResult.Error -> PlotValidationResult.Invalid(compileRes.message)
    }
}

fun niceTickStep(span: Float, targetTicks: Int = 6): Float {
    if (span <= 0f || !span.isFinite()) return 1f
    val rawStep = (span / targetTicks.coerceAtLeast(1)).toDouble()
    val exponent = floor(log10(rawStep))
    val base = 10.0.pow(exponent)
    val fraction = rawStep / base
    val niceFraction = when {
        fraction <= 1.2 -> 1.0
        fraction <= 2.5 -> 2.0
        fraction <= 5.5 -> 5.0
        else -> 10.0
    }
    return (niceFraction * base).toFloat()
}

fun formatAxisTick(value: Float): String {
    val clean = if (abs(value) < 1e-4f) 0f else value
    val absVal = abs(clean)
    val precision = when {
        absVal >= 10f -> 1
        absVal >= 1f -> 2
        else -> 3
    }
    return SampleCatalogs.formatNumber(clean.toDouble(), precision = precision, useGrouping = false)
}

fun samplePlotPolylines(
    compiledExpr: SimpleExprHelper.CompiledExpr,
    xMin: Double,
    xMax: Double,
    yMin: Double,
    yMax: Double,
    isDegrees: Boolean,
    sampleCount: Int = 300,
): List<List<GraphPoint>> {
    if (sampleCount < 2 || xMax <= xMin || yMax <= yMin) return emptyList()

    val ySpan = (yMax - yMin).coerceAtLeast(1e-6)
    val clipLow = yMin - ySpan * 2.0
    val clipHigh = yMax + ySpan * 2.0

    fun isPoleOrJump(
        x0: Double,
        y0: Double,
        x1: Double,
        y1: Double,
        depth: Int,
    ): Boolean {
        val crossesOppositeEdges = (y0 > yMax && y1 < yMin) || (y0 < yMin && y1 > yMax)
        val largeJump = abs(y1 - y0) > ySpan * 0.45
        if (!crossesOppositeEdges && !largeJump) return false

        val xm = 0.5 * (x0 + x1)
        val ym = compiledExpr.evaluate(isDegrees = isDegrees, xValue = xm)
        if (ym == null || !ym.isFinite()) return true
        if (ym < clipLow || ym > clipHigh) return true

        val low = min(y0, y1)
        val high = max(y0, y1)
        // If midpoint shoots outside [low, high] on a large jump, we are straddling a pole/singularity
        if (ym < low - 1e-6 || ym > high + 1e-6) return true

        if (depth < 2 && crossesOppositeEdges) {
            return isPoleOrJump(x0, y0, xm, ym, depth + 1) ||
                isPoleOrJump(xm, ym, x1, y1, depth + 1)
        }
        return false
    }

    val polylines = mutableListOf<MutableList<GraphPoint>>()
    var currentSegment = mutableListOf<GraphPoint>()
    var prevX = 0.0
    var prevY: Double? = null

    fun finishSegment() {
        if (currentSegment.size >= 2) {
            polylines.add(currentSegment)
        }
        currentSegment = mutableListOf()
    }

    for (i in 0..sampleCount) {
        val x = xMin + (i.toDouble() / sampleCount) * (xMax - xMin)
        val rawY = compiledExpr.evaluate(isDegrees = isDegrees, xValue = x)
        val validY = rawY?.takeIf { it.isFinite() && it in clipLow..clipHigh }

        if (validY == null) {
            finishSegment()
            prevY = null
            continue
        }

        val pY = prevY
        if (pY != null) {
            val bothOutsideTop = pY > yMax && validY > yMax
            val bothOutsideBottom = pY < yMin && validY < yMin
            if (bothOutsideTop || bothOutsideBottom) {
                finishSegment()
                currentSegment.add(GraphPoint(x, validY))
            } else if (isPoleOrJump(prevX, pY, x, validY, depth = 0)) {
                finishSegment()
                currentSegment.add(GraphPoint(x, validY))
            } else {
                currentSegment.add(GraphPoint(x, validY))
            }
        } else {
            currentSegment.add(GraphPoint(x, validY))
        }

        prevX = x
        prevY = validY
    }
    finishSegment()

    return polylines
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraphPlotterScreen(
    onBack: () -> Unit,
    viewModel: OmniCalcViewModel? = null,
) {
    val palette = remember {
        listOf(VioletPrimary, AccentCyan, AccentEmerald, AccentPink, AccentAmber)
    }
    val settingsState = viewModel?.settings?.collectAsState()
    var localIsDegrees by remember { mutableStateOf(false) }
    val isDegrees = if (viewModel != null) {
        (settingsState?.value ?: AppSettings()).angleMode == AngleMode.DEG
    } else {
        localIsDegrees
    }

    var functions by remember { mutableStateOf<List<PlotFunction>>(emptyList()) }
    var exprInput by remember { mutableStateOf("") }
    var exprError by remember { mutableStateOf<String?>(null) }
    var viewport by remember { mutableStateOf(GraphViewport()) }
    var showGrid by remember { mutableStateOf(true) }
    var showAxesLabels by remember { mutableStateOf(true) }
    var thickLines by remember { mutableStateOf(true) }
    var showGraphSettings by remember { mutableStateOf(false) }

    val compiledFunctions = remember(functions) {
        functions.mapNotNull { fn ->
            if (!fn.isVisible) {
                null
            } else {
                when (val compiled = SimpleExprHelper.compile(fn.expression, allowVariableX = true)) {
                    is SimpleExprHelper.CompileResult.Success -> CompiledPlotItem(fn, compiled.expr)
                    is SimpleExprHelper.CompileResult.Error -> null
                }
            }
        }
    }

    val textMeasurer = rememberTextMeasurer()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            OmniTopBar(
                title = "Graph Plotter",
                subtitle = "Multi-function coordinate canvas",
                onBack = onBack,
                actions = {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .clip(RoundedCornerShape(50))
                            .clickable {
                                if (viewModel != null) {
                                    val next = if (isDegrees) AngleMode.RAD else AngleMode.DEG
                                    viewModel.setAngleMode(next)
                                } else {
                                    localIsDegrees = !localIsDegrees
                                }
                            }
                            .testTag("graph_angle_toggle"),
                    ) {
                        Text(
                            text = if (isDegrees) "DEG" else "RAD",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        )
                    }
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
                        .height(280.dp),
                ) {
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val density = LocalDensity.current
                        val widthPx = with(density) { maxWidth.toPx() }
                        val heightPx = with(density) { maxHeight.toPx() }
                        val currentViewport by rememberUpdatedState(viewport)
                        val currentWidthPx by rememberUpdatedState(widthPx)

                        PlotCanvas(
                            compiledFunctions = compiledFunctions,
                            viewport = viewport,
                            isDegrees = isDegrees,
                            showGrid = showGrid,
                            showAxesLabels = showAxesLabels,
                            strokeWidthDp = if (thickLines) 3f else 1.8f,
                            textMeasurer = textMeasurer,
                            axisColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxSize()
                                .clipToBounds()
                                .pointerInput(Unit) {
                                    detectTransformGestures { _, pan, zoom, _ ->
                                        var next = currentViewport
                                        if (zoom != 1f && zoom.isFinite() && zoom > 0f) {
                                            next = next.zoomedBy(1f / zoom)
                                        }
                                        if (pan != Offset.Zero) {
                                            next = next.pannedBy(pan.x, pan.y, currentWidthPx)
                                        }
                                        viewport = next
                                    }
                                }
                                .testTag("graph_canvas"),
                        )

                        // Viewport range indicator pill (never truncates fractional zoom)
                        if (showAxesLabels) {
                            val xMinLabel = formatAxisTick(viewport.xMin())
                            val xMaxLabel = formatAxisTick(viewport.xMax())
                            val yMinLabel = formatAxisTick(viewport.yMin(widthPx, heightPx))
                            val yMaxLabel = formatAxisTick(viewport.yMax(widthPx, heightPx))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.84f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(8.dp),
                            ) {
                                Text(
                                    text = "X [$xMinLabel, $xMaxLabel] · Y [$yMinLabel, $yMaxLabel]",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 10.sp,
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                        .testTag("graph_viewport_label"),
                                )
                            }
                        }

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
                                        onClick = { viewport = viewport.zoomedBy(0.75f) },
                                        modifier = Modifier.testTag("graph_zoom_in"),
                                    ) {
                                        Icon(Icons.Rounded.ZoomIn, contentDescription = "Zoom in")
                                    }
                                    IconButton(
                                        onClick = { viewport = viewport.zoomedBy(1.35f) },
                                        modifier = Modifier.testTag("graph_zoom_out"),
                                    ) {
                                        Icon(Icons.Rounded.ZoomOut, contentDescription = "Zoom out")
                                    }
                                    IconButton(
                                        onClick = { viewport = GraphViewport() },
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
                            onValueChange = {
                                exprInput = it
                                exprError = null
                            },
                            isError = exprError != null,
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
                                when (val validation = validatePlotExpression(exprInput)) {
                                    is PlotValidationResult.Valid -> {
                                        val nextColor = palette[functions.size % palette.size]
                                        functions = functions + PlotFunction(
                                            id = UUID.randomUUID().toString(),
                                            expression = validation.normalizedExpression,
                                            color = nextColor,
                                            isVisible = true,
                                        )
                                        exprInput = ""
                                        exprError = null
                                    }
                                    is PlotValidationResult.Invalid -> {
                                        exprError = validation.message
                                    }
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 15.dp),
                            modifier = Modifier.testTag("graph_add_button"),
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = "Add function")
                        }
                    }

                    if (!exprError.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        ErrorStateBanner(
                            message = exprError!!,
                            modifier = Modifier.testTag("graph_error_banner"),
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(
                            listOf("sin(x)", "cos(x)", "x^2", "0.1*x^3", "sqrt(abs(x))", "tan(x)", "1/x", "ln(x)")
                        ) { preset ->
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                onClick = {
                                    exprInput = preset
                                    exprError = null
                                },
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
    compiledFunctions: List<CompiledPlotItem>,
    viewport: GraphViewport,
    isDegrees: Boolean,
    showGrid: Boolean,
    showAxesLabels: Boolean,
    strokeWidthDp: Float,
    textMeasurer: TextMeasurer,
    axisColor: Color,
    gridColor: Color,
    labelColor: Color,
    modifier: Modifier = Modifier,
) {
    val labelStyle = remember(labelColor) {
        TextStyle(
            color = labelColor,
            fontSize = 10.sp,
            fontFamily = JetBrainsMonoFontFamily,
        )
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas

        val xMin = viewport.xMin()
        val xMax = viewport.xMax()
        val yMin = viewport.yMin(w, h)
        val yMax = viewport.yMax(w, h)
        val xSpan = (xMax - xMin).coerceAtLeast(1e-4f)
        val step = niceTickStep(xSpan, targetTicks = 6)

        val originScreen = viewport.worldToScreen(0.0, 0.0, w, h)
        val axisX = originScreen.x
        val axisY = originScreen.y

        if (showGrid) {
            val firstXTick = ceil((xMin / step).toDouble()).toInt()
            val lastXTick = floor((xMax / step).toDouble()).toInt()
            for (k in firstXTick..lastXTick) {
                val gx = viewport.worldToScreen((k * step).toDouble(), 0.0, w, h).x
                drawLine(gridColor, Offset(gx, 0f), Offset(gx, h), strokeWidth = 1f)
            }

            val firstYTick = ceil((yMin / step).toDouble()).toInt()
            val lastYTick = floor((yMax / step).toDouble()).toInt()
            for (k in firstYTick..lastYTick) {
                val gy = viewport.worldToScreen(0.0, (k * step).toDouble(), w, h).y
                drawLine(gridColor, Offset(0f, gy), Offset(w, gy), strokeWidth = 1f)
            }
        }

        // X and Y Axes (drawn wherever inside viewport)
        if (axisY in 0f..h) {
            drawLine(axisColor, Offset(0f, axisY), Offset(w, axisY), strokeWidth = 2.5f)
        }
        if (axisX in 0f..w) {
            drawLine(axisColor, Offset(axisX, 0f), Offset(axisX, h), strokeWidth = 2.5f)
        }

        if (showAxesLabels) {
            // Position X labels along X-axis (or clamped to bottom edge if panned off-screen)
            val xLabelY = axisY.coerceIn(4f, h - 22f)
            val firstXTick = ceil((xMin / step).toDouble()).toInt()
            val lastXTick = floor((xMax / step).toDouble()).toInt()
            for (k in firstXTick..lastXTick) {
                val tickVal = k * step
                if (abs(tickVal) < 1e-4f) continue
                val gx = viewport.worldToScreen(tickVal.toDouble(), 0.0, w, h).x
                if (gx in 18f..(w - 28f)) {
                    drawText(
                        textMeasurer = textMeasurer,
                        text = formatAxisTick(tickVal),
                        topLeft = Offset(gx + 2f, xLabelY + 2f),
                        style = labelStyle,
                    )
                }
            }

            // Position Y labels along Y-axis (or clamped to left edge if panned off-screen)
            val yLabelX = axisX.coerceIn(4f, w - 42f)
            val firstYTick = ceil((yMin / step).toDouble()).toInt()
            val lastYTick = floor((yMax / step).toDouble()).toInt()
            for (k in firstYTick..lastYTick) {
                val tickVal = k * step
                if (abs(tickVal) < 1e-4f) continue
                val gy = viewport.worldToScreen(0.0, tickVal.toDouble(), w, h).y
                if (gy in 24f..(h - 18f)) {
                    drawText(
                        textMeasurer = textMeasurer,
                        text = formatAxisTick(tickVal),
                        topLeft = Offset(yLabelX + 4f, gy - 14f),
                        style = labelStyle,
                    )
                }
            }

            if (axisX in 4f..(w - 20f) && axisY in 4f..(h - 20f)) {
                drawText(
                    textMeasurer = textMeasurer,
                    text = "0",
                    topLeft = Offset(axisX + 4f, axisY + 2f),
                    style = labelStyle,
                )
            }
        }

        // Plot each visible pre-compiled function with pole/asymptote splitting
        val strokePx = strokeWidthDp.dp.toPx()
        val path = Path()
        compiledFunctions.forEach { item ->
            val polylines = samplePlotPolylines(
                compiledExpr = item.compiled,
                xMin = xMin.toDouble(),
                xMax = xMax.toDouble(),
                yMin = yMin.toDouble(),
                yMax = yMax.toDouble(),
                isDegrees = isDegrees,
                sampleCount = 300,
            )
            path.reset()
            polylines.forEach { segment ->
                segment.forEachIndexed { idx, pt ->
                    val screenPt = viewport.worldToScreen(pt.x, pt.y, w, h)
                    if (idx == 0) {
                        path.moveTo(screenPt.x, screenPt.y)
                    } else {
                        path.lineTo(screenPt.x, screenPt.y)
                    }
                }
            }
            drawPath(
                path = path,
                color = item.fn.color,
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
            )
        }
    }
}
