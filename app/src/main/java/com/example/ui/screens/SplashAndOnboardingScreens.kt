package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.components.OmniLogoMark
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.VioletPrimary
import com.example.ui.theme.VioletSoft
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

@Composable
fun SplashScreen(
    onFinished: () -> Unit,
) {
    val scale = remember { Animatable(0.76f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            scale.animateTo(1f, spring(dampingRatio = 0.65f, stiffness = 280f))
        }
        launch {
            alpha.animateTo(1f, tween(450, easing = FastOutSlowInEasing))
        }
        delay(950)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        VioletPrimary.copy(alpha = 0.22f),
                        MaterialTheme.colorScheme.background,
                    )
                )
            )
            .clickable { onFinished() },
        contentAlignment = Alignment.Center,
    ) {
        // Subtle geometric rings
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = size.minDimension * 0.24f
            drawCircle(
                color = VioletPrimary.copy(alpha = 0.14f),
                radius = baseRadius * 1.35f,
                center = center,
                style = Stroke(width = 1.5.dp.toPx()),
            )
            drawCircle(
                color = VioletPrimary.copy(alpha = 0.08f),
                radius = baseRadius * 1.9f,
                center = center,
                style = Stroke(width = 1.dp.toPx()),
            )
        }

        Column(
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            OmniLogoMark(size = 84.dp)
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "My Calculator",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Precision Calculators & Smart Converters",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private data class OnboardingPage(
    val title: String,
    val subtitle: String,
    val accent: Color,
    val artworkType: Int,
)

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
) {
    val pages = remember {
        listOf(
            OnboardingPage(
                title = "Everyday & Scientific Precision",
                subtitle = "Switch seamlessly between a clean everyday keypad and full scientific functions with DEG/RAD control.",
                accent = VioletPrimary,
                artworkType = 0,
            ),
            OnboardingPage(
                title = "GST, EMI & Finance Suite",
                subtitle = "Calculate Indian CGST/SGST/IGST, loan EMIs, compound interest, and retail discounts in seconds.",
                accent = AccentEmerald,
                artworkType = 1,
            ),
            OnboardingPage(
                title = "12 Converters, Programmer & Plotter",
                subtitle = "Convert units and sample currencies, inspect binary/hex bitwise logic, and visualize curves.",
                accent = AccentCyan,
                artworkType = 2,
            ),
        )
    }

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OmniLogoMark(size = 28.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "My Calculator",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
                TextButton(
                    onClick = onComplete,
                    modifier = Modifier.testTag("onboarding_skip_button"),
                ) {
                    Text("Skip", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) { index ->
                val page = pages[index]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    OnboardingIllustration(
                        type = page.artworkType,
                        accent = page.accent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = page.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = page.subtitle,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp),
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(pages.size) { idx ->
                        val active = pagerState.currentPage == idx
                        val width by animateDpAsState(
                            targetValue = if (active) 28.dp else 8.dp,
                            label = "dot_width",
                        )
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(width)
                                .clip(CircleShape)
                                .background(
                                    if (active) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                        )
                    }
                }

                val isLast = pagerState.currentPage == pages.lastIndex
                Button(
                    onClick = {
                        if (isLast) {
                            onComplete()
                        } else {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .height(52.dp)
                        .testTag("onboarding_next_button"),
                ) {
                    Text(
                        text = if (isLast) "Get Started" else "Next",
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingIllustration(
    type: Int,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(accent.copy(alpha = 0.22f), Color.Transparent),
                    center = Offset(w * 0.5f, h * 0.5f),
                    radius = w * 0.55f,
                )
            )
            when (type) {
                0 -> {
                    // Stylised calculator card & key matrix
                    val cardW = w * 0.46f
                    val cardH = h * 0.72f
                    val left = (w - cardW) / 2f
                    val top = (h - cardH) / 2f
                    drawRoundRect(
                        color = accent.copy(alpha = 0.25f),
                        topLeft = Offset(left, top),
                        size = Size(cardW, cardH),
                        cornerRadius = CornerRadius(28f, 28f),
                    )
                    drawRoundRect(
                        color = accent,
                        topLeft = Offset(left, top),
                        size = Size(cardW, cardH),
                        cornerRadius = CornerRadius(28f, 28f),
                        style = Stroke(width = 4f),
                    )
                    drawRoundRect(
                        color = VioletSoft.copy(alpha = 0.7f),
                        topLeft = Offset(left + 24f, top + 28f),
                        size = Size(cardW - 48f, cardH * 0.22f),
                        cornerRadius = CornerRadius(14f, 14f),
                    )
                    val cols = 3
                    val rows = 3
                    val cellW = (cardW - 64f) / cols
                    val cellH = (cardH * 0.52f) / rows
                    for (r in 0 until rows) {
                        for (c in 0 until cols) {
                            val isLastCell = r == rows - 1 && c == cols - 1
                            drawRoundRect(
                                color = if (isLastCell) accent else Color.White.copy(alpha = 0.24f),
                                topLeft = Offset(
                                    left + 24f + c * (cellW + 8f),
                                    top + cardH * 0.36f + r * (cellH + 8f),
                                ),
                                size = Size(cellW, cellH),
                                cornerRadius = CornerRadius(10f, 10f),
                            )
                        }
                    }
                }
                1 -> {
                    // Finance stacked bars & ascending curve
                    val barCount = 5
                    val barWidth = w * 0.08f
                    val gap = w * 0.05f
                    val startX = (w - (barCount * barWidth + (barCount - 1) * gap)) / 2f
                    val heights = listOf(0.28f, 0.42f, 0.55f, 0.70f, 0.84f)
                    heights.forEachIndexed { idx, frac ->
                        val bh = h * 0.65f * frac
                        val bx = startX + idx * (barWidth + gap)
                        val by = h * 0.82f - bh
                        drawRoundRect(
                            color = accent.copy(alpha = 0.35f + idx * 0.14f),
                            topLeft = Offset(bx, by),
                            size = Size(barWidth, bh),
                            cornerRadius = CornerRadius(14f, 14f),
                        )
                    }
                }
                else -> {
                    // Sine wave & grid axes
                    val midY = h * 0.5f
                    drawLine(
                        color = Color.White.copy(alpha = 0.15f),
                        start = Offset(w * 0.12f, midY),
                        end = Offset(w * 0.88f, midY),
                        strokeWidth = 3f,
                    )
                    drawLine(
                        color = Color.White.copy(alpha = 0.15f),
                        start = Offset(w * 0.5f, h * 0.15f),
                        end = Offset(w * 0.5f, h * 0.85f),
                        strokeWidth = 3f,
                    )
                    val path = Path()
                    val steps = 60
                    for (i in 0..steps) {
                        val t = i / steps.toFloat()
                        val x = w * 0.14f + t * w * 0.72f
                        val y = midY - sin(t * 2f * Math.PI.toFloat() * 1.5f) * (h * 0.25f)
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(
                        path = path,
                        color = accent,
                        style = Stroke(width = 7f, cap = StrokeCap.Round),
                    )
                }
            }
        }
    }
}
