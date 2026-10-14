package io.github.doubleddoge.splithappens

import android.view.RoundedCorner
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.doubleddoge.splithappens.components.DealerBust
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(top = 12.dp, bottom = 12.dp)
                        .clip(
                            RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp
                            )
                        )
                        .background(brush = Brush.verticalGradient(
                            colors = listOf(WoodRailInner,WoodRailOuter,WoodRailInner)))
                        .padding(top = 8.dp, start = 6.dp, end = 6.dp)
                ) {
                    // Inner Felt Playfield
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(feltColor.copy(alpha = 1.15f), feltColor),
                                    radius = 800f
                                )
                            )
                    ) {
                        // Table Felt Markings & Arc Lines
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            // Outer gold accent arc
                            val arcPath = Path().apply {
                                moveTo(w * 0.08f, h * 0.28f)
                                quadraticTo(
                                    w * 0.50f, h * 0.44f,
                                    w * 0.92f, h * 0.28f
                                )
                            }
                            drawPath(
                                path = arcPath,
                                color = GoldFoil.copy(alpha = 0.40f),
                                style = Stroke(width = 2.5f)
                            )

                            // Inner gold arc
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
                        }

                        // Felt Center Information
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "BLACKJACK PAYS 3 TO 2",
                                color = GoldFoil.copy(alpha = 0.65f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
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