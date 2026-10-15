package io.github.doubleddoge.splithappens

import android.graphics.Paint as AndroidPaint
import android.graphics.Path as AndroidPath
import android.graphics.PathMeasure as AndroidPathMeasure
import android.graphics.RectF
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.doubleddoge.splithappens.components.AppBottomBar
import io.github.doubleddoge.splithappens.components.BetChips
import io.github.doubleddoge.splithappens.components.GameActionBar
import io.github.doubleddoge.splithappens.components.UserHeaderCard
import io.github.doubleddoge.splithappens.ui.theme.SplitHappensTheme

// Theme Palettes
private val GameBgDarkCenter = Color(0xFF0F3022)
private val GameBgDarkOuter = Color(0xFF071B12)
private val GameBgLightCenter = Color(0xFF1B634A)
private val GameBgLightOuter = Color(0xFF144D38)

private val FeltDarkSurface = Color(0xFF0B2D21)
private val FeltLightSurface = Color(0xFF124B37)

private val WoodRailOuter = Color(0xFF332014)
private val WoodRailInner = Color(0xFF5A3924)
private val GoldFoil = Color(0xFFE5B036)

private const val ArcSweep = 110f

@Composable
fun GameScreen(
    modifier: Modifier = Modifier,
    displayName: String = "Player 1",
    chipsOwned: Long = 2500L,
    isDarkTheme: Boolean = true,
    onNavigateHome: () -> Unit = {},
    onNavigateMenu: () -> Unit = {}
) {
    val bgCenter = if (isDarkTheme) GameBgDarkCenter else GameBgLightCenter
    val bgOuter = if (isDarkTheme) GameBgDarkOuter else GameBgLightOuter
    val feltColor = if (isDarkTheme) FeltDarkSurface else FeltLightSurface
    var betChips by remember { mutableStateOf(listOf<Int>()) }
    val betTotal = betChips.sum()

    Scaffold(
        modifier = modifier,
        topBar = {
            UserHeaderCard(
                displayName = displayName,
                chipsOwned = chipsOwned
            )
        },
        bottomBar = {
            AppBottomBar(
                currentRoute = "game",
                isDarkTheme = isDarkTheme,
                onNavigateHome = onNavigateHome,
                onNavigateGame = { /* Already on Game */ },
                onNavigateMenu = onNavigateMenu
            )
        },
        containerColor = bgOuter
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(bgCenter, bgOuter),
                        radius = 1200f
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
            ) {
                // Table Rail Structure
                val railRadius = 36.dp
                val railThickness = 10.dp
                val railShape = RoundedCornerShape(topStart = railRadius, topEnd = railRadius)
                val feltShape = RoundedCornerShape(
                    topStart = railRadius - railThickness,
                    topEnd = railRadius - railThickness
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(top = 12.dp, bottom = 12.dp)
                        .shadow(12.dp, railShape)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(WoodRailInner, WoodRailOuter, WoodRailInner)
                            )
                        )
                        .border(
                            width = 1.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.White.copy(alpha = 0.22f), Color.Transparent)
                            ),
                            shape = railShape
                        )
                        .padding(top = railThickness, start = railThickness, end = railThickness)
                ) {
                    // Inner Felt Playfield
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(feltShape)
                            .border(1.dp, GoldFoil.copy(alpha = 0.22f), feltShape)
                    ) {
                        // Felt Arc Markings Canvas
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            drawRect(
                                Brush.radialGradient(
                                    colors = listOf(
                                        lerp(feltColor, Color.White, 0.08f),
                                        feltColor,
                                        lerp(feltColor, Color.Black, 0.45f)
                                    ),
                                    center = Offset(w / 2f, h * 0.45f),
                                    radius = w * 1.15f
                                )
                            )

                            val outerR = w * 0.52f
                            val innerR = outerR - 30.dp.toPx()
                            val center = Offset(w / 2f, h * 0.50f - outerR)

                            fun arc(radius: Float, alpha: Float, strokeWidth: Float) = drawArc(
                                color = GoldFoil.copy(alpha = alpha),
                                startAngle = 90f - ArcSweep / 2f,
                                sweepAngle = ArcSweep,
                                useCenter = false,
                                topLeft = Offset(center.x - radius, center.y - radius),
                                size = Size(radius * 2, radius * 2),
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )

                            arc(outerR, 0.45f, 1.5.dp.toPx())
                            arc(innerR, 0.25f, 1.dp.toPx())

                            drawArcText(
                                "BLACKJACK PAYS 3 TO 2", center, outerR - 8.dp.toPx(),
                                GoldFoil.copy(alpha = 0.85f), 12.sp.toPx()
                            )
                            drawArcText(
                                "INSURANCE PAYS 2 TO 1", center, innerR - 8.dp.toPx(),
                                GoldFoil.copy(alpha = 0.55f), 9.sp.toPx(), spacingEm = 0.14f
                            )
                        }

                        // Top Dealer Header Label
                        TableLabel(
                            text = "DEALER",
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 14.dp)
                        )

                        // Bottom Layout Container: Player Header -> Bet Area -> Action Bar
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // 1. PLAYER label at the top
                            TableLabel(text = "PLAYER")

                            Spacer(modifier = Modifier.height(12.dp))

                            // 2. Bet Chips Area (Shows 'TAP A CHIP TO BET' prompt or stacked chips)
                            if (betChips.isEmpty()) {
                                Text(
                                    text = "TAP A CHIP TO BET",
                                    color = GoldFoil.copy(alpha = 0.65f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            } else {
                                BetChips(
                                    chips = betChips,
                                    onUndo = { betChips = betChips.dropLast(1) }
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // 3. Action Bar with Chip Selectors and Deal Button
                            GameActionBar(
                                isBetting = true,
                                canBet = betTotal > 0,
                                onChip = { value ->
                                    if (chipsOwned - betTotal >= value) {
                                        betChips = betChips + value.toInt()
                                    }
                                },
                                onDeal = { /* Start game round */ }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawArcText(
    text: String,
    center: Offset,
    radius: Float,
    color: Color,
    textSizePx: Float,
    spacingEm: Float = 0.16f
) {
    val oval = RectF(
        center.x - radius,
        center.y - radius,
        center.x + radius,
        center.y + radius
    )
    val androidPath = AndroidPath().apply {
        arcTo(oval, 90f + ArcSweep / 2f, -ArcSweep, true)
    }
    val paint = AndroidPaint().apply {
        isAntiAlias = true
        this.color = color.toArgb()
        textSize = textSizePx
        typeface = Typeface.DEFAULT_BOLD
        letterSpacing = spacingEm
    }
    val pathLength = AndroidPathMeasure(androidPath, false).length
    val textWidth = paint.measureText(text)

    drawIntoCanvas { canvas ->
        canvas.nativeCanvas.drawTextOnPath(
            text, androidPath, (pathLength - textWidth) / 2f, 0f, paint
        )
    }
}

@Composable
private fun TableLabel(text: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .width(28.dp)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, GoldFoil.copy(alpha = 0.55f))
                    )
                )
        )
        Text(
            text = text,
            color = GoldFoil.copy(alpha = 0.75f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(horizontal = 10.dp)
        )
        Box(
            Modifier
                .width(28.dp)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(GoldFoil.copy(alpha = 0.55f), Color.Transparent)
                    )
                )
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GameScreenPreview() {
    SplitHappensTheme {
        GameScreen(
            displayName = "Player 1",
            chipsOwned = 2500L,
            isDarkTheme = true
        )
    }
}