package io.github.doubleddoge.splithappens

import android.graphics.Paint as AndroidPaint
import android.graphics.Path as AndroidPath
import android.graphics.PathMeasure as AndroidPathMeasure
import android.graphics.RectF
import android.graphics.Typeface
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.doubleddoge.splithappens.components.AppBottomBar
import io.github.doubleddoge.splithappens.components.GameActionBar
import io.github.doubleddoge.splithappens.components.HandRow
import io.github.doubleddoge.splithappens.components.SectionLabel
import io.github.doubleddoge.splithappens.components.UserHeaderCard
import io.github.doubleddoge.splithappens.data.buildSplitHappensDatabase
import io.github.doubleddoge.splithappens.ui.theme.SplitHappensTheme

private val GameBgDarkCenter = Color(0xFF0F3022)
private val GameBgDarkOuter = Color(0xFF071B12)
private val GameBgLightCenter = Color(0xFF1B634A)
private val GameBgLightOuter = Color(0xFF144D38)

private val FeltDarkSurface = Color(0xFF0B2D21)
private val FeltLightSurface = Color(0xFF124B37)

private val WoodRailOuter = Color(0xFF332014)
private val WoodRailInner = Color(0xFF5A3924)
private val GoldFoil = Color(0xFFE5B036)

@Composable
fun GameScreen(
    modifier: Modifier = Modifier,
    displayName: String = "Player 1",
    chipsOwned: Long = 2500L,
    isDarkTheme: Boolean = true,
    onNavigateHome: () -> Unit = {},
    onNavigateMenu: () -> Unit = {}
) {
    val context = LocalContext.current
    val db = remember(context) { buildSplitHappensDatabase(context) }
    val gameViewModel: GameViewModel = viewModel(
        factory = GameViewModelFactory(db.userDao())
    )
    val uiState by gameViewModel.uiState.collectAsState()

    val bgCenter = if (isDarkTheme) GameBgDarkCenter else GameBgLightCenter
    val bgOuter = if (isDarkTheme) GameBgDarkOuter else GameBgLightOuter
    val feltColor = if (isDarkTheme) FeltDarkSurface else FeltLightSurface

    Scaffold(
        modifier = modifier,
        topBar = {
            UserHeaderCard(
                displayName = uiState.displayName.ifBlank { displayName },
                chipsOwned = uiState.chipsOwned
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
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(feltShape)
                            .border(1.dp, GoldFoil.copy(alpha = 0.22f), feltShape)
                    ) {
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

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // DEALER AREA
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                SectionLabel(
                                    title = "DEALER",
                                    total = if (uiState.dealerHand.isNotEmpty()) uiState.dealerHandValue else null
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                HandRow(hand = uiState.dealerHand)
                            }

                            // TABLE CENTER: Banner and Active Bet Badge
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                if (uiState.result.bannerMessage.isNotEmpty()) {
                                    Text(
                                        text = uiState.result.bannerMessage,
                                        color = GoldFoil,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                }

                                // Shows in-game status (hit/stand/dealer outcomes) outside the initial betting phase
                                if (uiState.stage != GameStage.BETTING && uiState.dealerStatusText.isNotEmpty()) {
                                    Text(
                                        text = uiState.dealerStatusText,
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                // Current Bet Pill (tap to clear while in betting mode)
                                if (uiState.currentBet > 0L) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(50.dp),
                                        color = Color(0xFF132A21),
                                        border = BorderStroke(1.dp, GoldFoil.copy(alpha = 0.7f)),
                                        modifier = Modifier.clickable(enabled = uiState.stage == GameStage.BETTING) {
                                            gameViewModel.clearBet()
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = "🪙", fontSize = 13.sp)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "BET: R${uiState.currentBet}",
                                                color = GoldFoil,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.sp
                                            )
                                            if (uiState.stage == GameStage.BETTING) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "✕",
                                                    color = Color.White.copy(alpha = 0.5f),
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // PLAYER AREA
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                HandRow(hand = uiState.playerHand)
                                Spacer(modifier = Modifier.height(8.dp))
                                SectionLabel(
                                    title = "PLAYER",
                                    total = if (uiState.playerHand.isNotEmpty()) uiState.playerHandValue else null
                                )

                                // "PLACE YOUR BETS" displayed under PLAYER only while bet == 0
                                if (uiState.stage == GameStage.BETTING && uiState.currentBet == 0L) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "PLACE YOUR BETS",
                                        color = GoldFoil.copy(alpha = 0.75f),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.5.sp
                                    )
                                }
                            }

                            // ACTION CONTROLS
                            if (uiState.stage == GameStage.ROUND_OVER) {
                                Button(
                                    onClick = { gameViewModel.resetForNextRound() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = GoldFoil,
                                        contentColor = Color(0xFF1A1208)
                                    )
                                ) {
                                    Text(
                                        text = "PLAY AGAIN",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        letterSpacing = 1.5.sp
                                    )
                                }
                            } else {
                                GameActionBar(
                                    isBetting = uiState.stage == GameStage.BETTING,
                                    canBet = uiState.currentBet > 0L,
                                    onChip = { amount -> gameViewModel.addBet(amount) },
                                    onClear = { gameViewModel.clearBet() },
                                    onDeal = { gameViewModel.deal() },
                                    onHit = { gameViewModel.hit() },
                                    onStand = { gameViewModel.stand() },
                                    onDouble = { gameViewModel.doubleDown() },
                                    doubleEnabled = uiState.canDoubleDown
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private const val ArcSweep = 110f

private fun DrawScope.drawArcText(
    text: String,
    center: Offset,
    radius: Float,
    color: Color,
    textSizePx: Float,
    spacingEm: Float = 0.16f
) {
    val oval = RectF(
        center.x - radius, center.y - radius,
        center.x + radius, center.y + radius
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