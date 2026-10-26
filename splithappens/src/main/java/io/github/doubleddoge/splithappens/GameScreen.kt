package io.github.doubleddoge.splithappens

import android.app.Application
import android.graphics.RectF
import android.graphics.Typeface
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.doubleddoge.splithappens.components.AppBottomBar
import io.github.doubleddoge.splithappens.components.BetChips
import io.github.doubleddoge.splithappens.components.GameActionBar
import io.github.doubleddoge.splithappens.components.HandRow
import io.github.doubleddoge.splithappens.components.OutcomeBanner
import io.github.doubleddoge.splithappens.components.UserHeaderCard
import io.github.doubleddoge.splithappens.data.buildSplitHappensDatabase
import io.github.doubleddoge.splithappens.data.enum.GameMode
import io.github.doubleddoge.splithappens.game.evaluateHand
import io.github.doubleddoge.splithappens.navigation.Screen
import io.github.doubleddoge.splithappens.ui.theme.SplitHappensTheme
import android.graphics.Paint as AndroidPaint
import android.graphics.Path as AndroidPath
import android.graphics.PathMeasure as AndroidPathMeasure

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
    userId: String = "",
    mode: GameMode = GameMode.TRADITIONAL,
    displayName: String = "Player 1",
    chipsOwned: Long = 2500L,
    isDarkTheme: Boolean = true,
    onNavigateHome: () -> Unit = {},
    onNavigateMenu: () -> Unit = {},
) {
  val context = LocalContext.current
  val db = remember(context) { buildSplitHappensDatabase(context) }
  val gameViewModel: GameViewModel =
      viewModel(
          key = "game_${userId}_${mode.name}",
          factory = GameViewModelFactory(
              application = context.applicationContext as Application,
              userDao = db.userDao(),
              sessionDao = db.sessionDao(),
              roundDao = db.roundDao(),
              targetUserId = userId,
              mode = mode
          )
      )
  val uiState by gameViewModel.uiState.collectAsState()
  val isGameInProgress = uiState.isGameInProgress

  var betChipsList by remember { mutableStateOf(listOf<Int>()) }
  var pendingNavigation by remember { mutableStateOf<(() -> Unit)?>(null) }

  val bgCenter = if (isDarkTheme) GameBgDarkCenter else GameBgLightCenter
  val bgOuter = if (isDarkTheme) GameBgDarkOuter else GameBgLightOuter
  val feltColor = if (isDarkTheme) FeltDarkSurface else FeltLightSurface

  val safeNavigate: (() -> Unit) -> Unit = { destinationAction ->
    if (isGameInProgress) {
      pendingNavigation = destinationAction
    } else {
      destinationAction()
    }
  }

  BackHandler {
    if (isGameInProgress) {
      pendingNavigation = onNavigateHome
    } else {
      onNavigateHome()
    }
  }

  if (pendingNavigation != null) {
    AlertDialog(
        onDismissRequest = { pendingNavigation = null },
        title = { Text("Quit Game?") },
        text = { Text("You have an active bet in progress. Leaving now will forfeit your bet.") },
        confirmButton = {
          TextButton(
              onClick = {
                val action = pendingNavigation
                pendingNavigation = null
                gameViewModel.forfeitRound()
                action?.invoke()
              }
          ) {
            Text("Quit & Forfeit")
          }
        },
        dismissButton = {
          TextButton(onClick = { pendingNavigation = null }) { Text("Cancel") }
        },
    )
  }

  Scaffold(
      modifier = modifier,
      topBar = {
        val displayedChips = when (uiState.stage) {
            GameStage.ROUND_OVER -> uiState.chipsOwned
            else -> uiState.chipsOwned - uiState.currentBet - uiState.insuranceBet
        }
        UserHeaderCard(
            displayName = uiState.displayName.ifBlank { displayName },
            chipsOwned = displayedChips,
            isDarkTheme = isDarkTheme,
        )
      },
      bottomBar = {
        AppBottomBar(
            currentRoute = Screen.Game.route,
            isDarkTheme = isDarkTheme,
            onNavigateHome = { safeNavigate(onNavigateHome) },
            onNavigateGame = { /* Already on Game */ },
            onNavigateMenu = { safeNavigate(onNavigateMenu) },
        )
      },
      containerColor = bgOuter,
  ) { innerPadding ->
    Box(
        modifier =
            Modifier.fillMaxSize()
                .padding(innerPadding)
                .background(
                    brush =
                        Brush.radialGradient(
                            colors = listOf(bgCenter, bgOuter),
                            radius = 1200f,
                        )
                )
    ) {
      Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp)) {
        // Table Rail Structure
        val railRadius = 36.dp
        val railThickness = 10.dp
        val railShape = RoundedCornerShape(topStart = railRadius, topEnd = railRadius)
        val feltShape =
            RoundedCornerShape(
                topStart = railRadius - railThickness,
                topEnd = railRadius - railThickness,
            )

        Box(
            modifier =
                Modifier.fillMaxWidth()
                    .weight(1f)
                    .padding(top = 12.dp, bottom = 12.dp)
                    .shadow(12.dp, railShape)
                    .background(
                        brush =
                            Brush.verticalGradient(
                                colors = listOf(WoodRailInner, WoodRailOuter, WoodRailInner)
                            )
                    )
                    .border(
                        width = 1.dp,
                        brush =
                            Brush.verticalGradient(
                                colors = listOf(Color.White.copy(alpha = 0.22f), Color.Transparent)
                            ),
                        shape = railShape,
                    )
                    .padding(top = railThickness, start = railThickness, end = railThickness)
        ) {
          // Inner Felt Playfield
          Box(
              modifier =
                  Modifier.fillMaxSize()
                      .clip(feltShape)
                      .border(1.dp, GoldFoil.copy(alpha = 0.22f), feltShape)
          ) {
            // Felt Arc Markings Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
              val w = size.width
              val h = size.height

              drawRect(
                  Brush.radialGradient(
                      colors =
                          listOf(
                              lerp(feltColor, Color.White, 0.08f),
                              feltColor,
                              lerp(feltColor, Color.Black, 0.45f),
                          ),
                      center = Offset(w / 2f, h * 0.45f),
                      radius = w * 1.15f,
                  )
              )

              val outerR = w * 0.52f
              val innerR = outerR - 30.dp.toPx()
              val center = Offset(w / 2f, h * 0.50f - outerR)

              fun arc(radius: Float, alpha: Float, strokeWidth: Float) =
                  drawArc(
                      color = GoldFoil.copy(alpha = alpha),
                      startAngle = 90f - ArcSweep / 2f,
                      sweepAngle = ArcSweep,
                      useCenter = false,
                      topLeft = Offset(center.x - radius, center.y - radius),
                      size = Size(radius * 2, radius * 2),
                      style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                  )

              arc(outerR, 0.45f, 1.5.dp.toPx())
              arc(innerR, 0.25f, 1.dp.toPx())

              drawArcText(
                  "BLACKJACK PAYS 3 TO 2",
                  center,
                  outerR - 8.dp.toPx(),
                  GoldFoil.copy(alpha = 0.85f),
                  12.sp.toPx(),
              )
              drawArcText(
                  "INSURANCE PAYS 2 TO 1",
                  center,
                  innerR - 8.dp.toPx(),
                  GoldFoil.copy(alpha = 0.55f),
                  9.sp.toPx(),
                  spacingEm = 0.14f,
              )
            }

            // DEALER AREA (Top)
            Column(
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
              TableLabel(
                  text = "DEALER",
                  total =
                      if (uiState.dealerCards.isNotEmpty() && !uiState.hideHoleCard) {
                        uiState.dealerCards.evaluateHand().total
                      } else {
                        null
                      },
              )
              Spacer(modifier = Modifier.height(6.dp))
              HandRow(hand = uiState.dealerCards.toTableCards(uiState.hideHoleCard))
            }

            // PLAYER AREA, STATUS BANNER & CONTROLS (Bottom)
            Column(
                modifier =
                    Modifier.align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
              // OUTCOME BANNER (Positioned directly above Player's Cards)
                if (uiState.stage != GameStage.BETTING && uiState.statusText.isNotEmpty()) {
                    OutcomeBanner(
                        text = uiState.statusText,
                        results = uiState.results,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

              // PLAYER HAND DISPLAY
              val primaryHand = uiState.playerHands.firstOrNull()
              if (primaryHand != null) {
                // TODO: Replace single HandRow rendering with side-by-side Row layout when split
                // hands are enabled
                HandRow(hand = primaryHand.cards.toTableCards())
              }

              Spacer(modifier = Modifier.height(6.dp))

              TableLabel(
                  text = "PLAYER",
                  total = primaryHand?.value?.total,
              )

              if (uiState.stage == GameStage.BETTING) {
                Spacer(modifier = Modifier.height(6.dp))
                BetChips(
                    chips = betChipsList,
                    onUndo = {
                      if (betChipsList.isNotEmpty()) {
                        gameViewModel.clearBet()
                        betChipsList = emptyList()
                      }
                    },
                )
              }

              Spacer(modifier = Modifier.height(8.dp))

              if (uiState.stage == GameStage.ROUND_OVER) {
                Button(
                    onClick = {
                      betChipsList = emptyList()
                      gameViewModel.resetForNextRound()
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = GoldFoil,
                            contentColor = Color(0xFF1A1208),
                        ),
                ) {
                  Text(
                      text = "PLAY AGAIN",
                      fontWeight = FontWeight.Bold,
                      fontSize = 14.sp,
                      letterSpacing = 1.5.sp,
                  )
                }
              } else {
                GameActionBar(
                    isBetting = uiState.stage == GameStage.BETTING,
                    canBet = uiState.currentBet > 0L,
                    availableChips = uiState.chipsOwned - uiState.currentBet,
                    legalActions = uiState.legalActions,
                    onChip = { amount ->
                      if ((uiState.chipsOwned - uiState.currentBet) >= amount) {
                        betChipsList = betChipsList + amount.toInt()
                        gameViewModel.addBet(amount)
                      }
                    },
                    onDeal = { gameViewModel.deal() },
                    onAction = { action -> gameViewModel.performAction(action) },
                )
              }
            }

            // Insurance overlay prompt (Direct child of the Felt Box so Alignment.Center works)
            if (uiState.stage == GameStage.INSURANCE) {
              val canAffordInsurance = uiState.chipsOwned >= uiState.currentBet / 2
              Surface(
                  shape = RoundedCornerShape(16.dp),
                  color = Color(0xFF092016),
                  border = BorderStroke(2.dp, GoldFoil),
                  shadowElevation = 12.dp,
                  modifier = Modifier.align(Alignment.Center).padding(24.dp),
              ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                  Text(
                      text = "INSURANCE OFFERED",
                      color = GoldFoil,
                      fontSize = 16.sp,
                      fontWeight = FontWeight.Bold,
                      letterSpacing = 1.2.sp,
                  )
                  Spacer(modifier = Modifier.height(12.dp))
                  Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { gameViewModel.resolveInsurance(true) },
                        enabled = canAffordInsurance,
                        colors =
                            ButtonDefaults.buttonColors(
                                contentColor = GoldFoil,
                                containerColor = Color.Black,
                            ),
                    ) {
                      Text("ACCEPT")
                    }
                    Button(
                        onClick = { gameViewModel.resolveInsurance(false) },
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2A3B32),
                                contentColor = Color.White,
                            ),
                    ) {
                      Text("DECLINE")
                    }
                  }
                }
              }
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
    spacingEm: Float = 0.16f,
) {
  val oval =
      RectF(
          center.x - radius,
          center.y - radius,
          center.x + radius,
          center.y + radius,
      )
  val androidPath = AndroidPath().apply { arcTo(oval, 90f + ArcSweep / 2f, -ArcSweep, true) }
  val paint =
      AndroidPaint().apply {
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
        text,
        androidPath,
        (pathLength - textWidth) / 2f,
        0f,
        paint,
    )
  }
}

@Composable
private fun TableLabel(
    text: String,
    total: Int? = null,
    modifier: Modifier = Modifier,
) {
  val labelText = if (total != null) "$text · $total" else text

  Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
    Box(
        Modifier.width(28.dp)
            .height(1.dp)
            .background(
                Brush.horizontalGradient(listOf(Color.Transparent, GoldFoil.copy(alpha = 0.55f)))
            )
    )
    Text(
        text = labelText,
        color = GoldFoil.copy(alpha = 0.75f),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp,
        modifier = Modifier.padding(horizontal = 10.dp),
    )
    Box(
        Modifier.width(28.dp)
            .height(1.dp)
            .background(
                Brush.horizontalGradient(listOf(GoldFoil.copy(alpha = 0.55f), Color.Transparent))
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
        isDarkTheme = true,
    )
  }
}
