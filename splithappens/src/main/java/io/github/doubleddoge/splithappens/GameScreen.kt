package io.github.doubleddoge.splithappens

import android.icu.number.IntegerWidth
import android.view.RoundedCorner
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.doubleddoge.splithappens.components.DealerBust
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.doubleddoge.splithappens.components.AppBottomBar
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
                // LAYER 1: The Croupier / Dealer standing behind the table
                DealerBust(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 0.dp)
                )

                // LAYER 2: Casino Table with Arched Wooden Bumper Rail & Felt
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
                        .background(brush = Brush.verticalGradient(
                            colors = listOf(WoodRailInner,WoodRailOuter,WoodRailInner)))
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
                            .border(1.dp, GoldFoil.copy(alpha = 0.22f),feltShape)

                    ) {
                        // Table Felt Markings & Arc Lines
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            // Outer gold accent arc
                            drawRect(Brush.radialGradient(
                                colors = listOf(
                                    lerp(feltColor, Color.White, 0.08f),
                                    feltColor,
                                    lerp(feltColor, Color.Black, 0.45f)
                                ),
                                center = Offset(w / 2f, h * 0.35f),
                                radius = w * 1.15f
                            ))
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color.Black.copy(alpha = 0.40f), Color.Transparent),
                                    startY = 0f,
                                    endY = h * 0.12f
                                ),
                                size = Size(w, h * 0.12f)
                            )
                            val centerX = w / 2f
                            val centerY = h * 0.02f
                            val outerR = w * 0.50f
                            val innerR = outerR * 0.86f
                            // Inner gold arc
                            fun arc (radius: Float, color: Color, strokeWidth: Float){
                                drawArc(
                                    color = color,
                                    startAngle = 25f,
                                    sweepAngle = 130f,
                                    useCenter = false,
                                    topLeft = Offset(centerX - radius, centerY - radius),
                                    size = Size(radius * 2, radius * 2),
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                            }
                            val innerArcPath = Path().apply {
                                moveTo(w * 0.16f, h * 0.26f)
                                quadraticTo(
                                    w * 0.50f, h * 0.38f,
                                    w * 0.84f, h * 0.26f
                                )
                            }
                            drawPath(
                                path = innerArcPath,
                                color = GoldFoil.copy(alpha = 0.25f),
                                style = Stroke(width = 1.5f)
                            )
                            arc(outerR, GoldFoil.copy(alpha = 0.45f), 1.5.dp.toPx())
                            arc(innerR, GoldFoil.copy(alpha = 0.25f), 1.dp.toPx())
                        }


                        // Felt Center Information
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically
                            ) {
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
                                    text = "BlACKJACK PAYS 3 TO 2",
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
                            Text(
                                text = "INSURANCE PAYS 2 TO 1",
                                color = GoldFoil.copy(alpha = 0.40f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 1.5.sp,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    }
                }
            }
        }
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