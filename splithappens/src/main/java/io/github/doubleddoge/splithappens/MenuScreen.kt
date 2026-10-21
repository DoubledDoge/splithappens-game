package io.github.doubleddoge.splithappens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.github.doubleddoge.splithappens.components.AppBottomBar
import io.github.doubleddoge.splithappens.ui.theme.SplitHappensTheme
import kotlin.math.abs

// Standard Dark Theme Colors
private val MenuTheme_BackgroundDark = Color(0xFF071B12)
private val MenuTheme_GlowCenter = Color(0xFF0F3022)
private val MenuTheme_GreenSurface = Color(0xFF133827)
private val MenuTheme_MutedText = Color(0xFF9EBAAA)

@Composable
fun MenuScreen(
    modifier: Modifier = Modifier,
    uiState: MenuUiState = MenuUiState(),
    homeUiState: HomeUiState = HomeUiState(),
    isDarkTheme: Boolean = true,
    onOpenRules: () -> Unit = {},
    onCloseRules: () -> Unit = {},
    onOpenLogoutConfirm: () -> Unit = {},
    onCloseLogoutConfirm: () -> Unit = {},
    onConfirmLogout: () -> Unit = {},
    onResumeGame: () -> Unit = {},
    onNavigateHome: () -> Unit = {},
    onNavigateGame: () -> Unit = onResumeGame,
    onNavigateToProfile: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
) {
  val context = LocalContext.current
  val isPreview = LocalInspectionMode.current
  var showStatsDialog by remember { mutableStateOf(false) }

  // Dynamic color palette based on theme
  val bgGlowCenter = if (isDarkTheme) MenuTheme_GlowCenter else Color(0xFF1B634A)
  val bgOuterDark = if (isDarkTheme) MenuTheme_BackgroundDark else Color(0xFF144D38)
  val surfaceColor = if (isDarkTheme) MenuTheme_GreenSurface else Color(0xFF1C6349)
  val surfaceBorder = if (isDarkTheme) Color.White.copy(alpha = 0.12f) else Color(0xFF288160)
  val mutedTextColor = if (isDarkTheme) MenuTheme_MutedText else Color(0xFFBCE0CF)

  val runFeedback: (() -> Unit) -> Unit = { action ->
    if (!isPreview) {
      if (uiState.soundEnabled) triggerClickSound()
      if (uiState.hapticsEnabled) triggerDeviceVibration(context, 35)
    }
    action()
  }

  val hasActiveOverlay =
      uiState.isRulesBookletOpen || uiState.isLogoutConfirmOpen || showStatsDialog

  BackHandler(enabled = hasActiveOverlay) {
    when {
      uiState.isRulesBookletOpen -> onCloseRules()
      uiState.isLogoutConfirmOpen -> onCloseLogoutConfirm()
      showStatsDialog -> showStatsDialog = false
    }
  }

  Scaffold(
      modifier = modifier,
      bottomBar = {
        AppBottomBar(
            currentRoute = "menu",
            isDarkTheme = isDarkTheme,
            onNavigateHome = { runFeedback(onNavigateHome) },
            onNavigateGame = { runFeedback(onNavigateGame) },
            onNavigateMenu = { /* Already on Menu */ },
        )
      },
      containerColor = bgOuterDark,
  ) { innerPadding ->
    Box(
        modifier =
            Modifier.fillMaxSize()
                .padding(innerPadding)
                .background(
                    brush =
                        Brush.radialGradient(
                            colors = listOf(bgGlowCenter, bgOuterDark),
                            radius = 1200f,
                        )
                )
                .padding(horizontal = 24.dp)
    ) {
      Column(
          modifier =
              Modifier.fillMaxSize()
                  .verticalScroll(rememberScrollState())
                  .statusBarsPadding()
                  .padding(vertical = 20.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        // Centered "MENU" Header
        Box(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
          Text(
              text = "MENU",
              color = BorderGold,
              fontSize = 26.sp,
              fontWeight = FontWeight.Black,
              letterSpacing = 4.sp,
          )
        }

        // 1. Profile
        MenuButton(
            text = "PROFILE",
            icon = "👤",
            surfaceColor = surfaceColor,
            borderColor = surfaceBorder,
            onClick = { runFeedback(onNavigateToProfile) },
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Stats
        MenuButton(
            text = "STATS",
            icon = "📊",
            surfaceColor = surfaceColor,
            borderColor = surfaceBorder,
            onClick = { runFeedback { showStatsDialog = true } },
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Rules & Strategy
        MenuButton(
            text = "RULES & STRATEGY",
            icon = "📖",
            surfaceColor = surfaceColor,
            borderColor = surfaceBorder,
            onClick = { runFeedback(onOpenRules) },
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Settings
        MenuButton(
            text = "SETTINGS",
            icon = "⚙️",
            surfaceColor = surfaceColor,
            borderColor = surfaceBorder,
            onClick = { runFeedback(onNavigateToSettings) },
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 5. Switch Profile
        MenuButton(
            text = "SWITCH PROFILE",
            icon = "👥",
            surfaceColor = surfaceColor,
            borderColor = BorderGold.copy(alpha = 0.45f),
            onClick = { runFeedback(onOpenLogoutConfirm) },
        )
      }
    }
  }

  // Modal In-Screen Stats Dialog
  if (showStatsDialog) {
    MenuStatsDialog(
        uiState = homeUiState,
        surfaceColor = surfaceColor,
        mutedTextColor = mutedTextColor,
        onDismiss = { showStatsDialog = false },
    )
  }

  // Modal In-Screen Rules Booklet (Dismissed only via top-right ✕)
  if (uiState.isRulesBookletOpen) {
    RulesBookletDialog(
        surfaceColor = surfaceColor,
        backgroundColor = bgOuterDark,
        mutedTextColor = mutedTextColor,
        onDismiss = onCloseRules,
    )
  }

  // Modal Switch Profile Confirmation
  if (uiState.isLogoutConfirmOpen) {
    AlertDialog(
        onDismissRequest = onCloseLogoutConfirm,
        containerColor = surfaceColor,
        title = {
          Text(
              text = "Switch Profile?",
              color = Color.White,
              fontWeight = FontWeight.Bold,
          )
        },
        text = {
          Text(
              text = "You will forfeit your current round and return to profile selection.",
              color = mutedTextColor,
              fontSize = 14.sp,
          )
        },
        confirmButton = {
          TextButton(onClick = { runFeedback(onConfirmLogout) }) {
            Text("SWITCH", color = BorderGold, fontWeight = FontWeight.Bold)
          }
        },
        dismissButton = {
          TextButton(onClick = onCloseLogoutConfirm) {
            Text("CANCEL", color = Color.White)
          }
        },
    )
  }
}

@Composable
private fun MenuButton(
    text: String,
    icon: String,
    surfaceColor: Color,
    borderColor: Color,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false,
    onClick: () -> Unit,
) {
  Card(
      shape = RoundedCornerShape(16.dp),
      colors =
          CardDefaults.cardColors(containerColor = if (isPrimary) BorderGold else surfaceColor),
      modifier =
          modifier
              .fillMaxWidth()
              .height(58.dp)
              .border(
                  width = 1.dp,
                  color = if (isPrimary) BorderGold else borderColor,
                  shape = RoundedCornerShape(16.dp),
              )
              .clickable { onClick() },
  ) {
    Row(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = icon,
            fontSize = 18.sp,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            color = if (isPrimary) MenuTheme_BackgroundDark else Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
        )
      }
      Text(
          text = "›",
          color = if (isPrimary) MenuTheme_BackgroundDark else Color.White.copy(alpha = 0.5f),
          fontSize = 20.sp,
          fontWeight = FontWeight.Light,
      )
    }
  }
}

@Composable
private fun MenuStatsDialog(
    uiState: HomeUiState,
    surfaceColor: Color,
    mutedTextColor: Color,
    onDismiss: () -> Unit,
) {
  val winRate =
      if (uiState.totalGamesPlayed > 0) {
        (uiState.totalWins.toDouble() / uiState.totalGamesPlayed * 100).toInt()
      } else {
        0
      }

  val netChipsFormatted =
      if (uiState.netChipsEarned >= 0) {
        "+R${uiState.netChipsEarned}"
      } else {
        "-R${abs(uiState.netChipsEarned)}"
      }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
        shape = RoundedCornerShape(20.dp),
        color = surfaceColor,
        border = BorderStroke(1.dp, BorderGold),
        tonalElevation = 8.dp,
    ) {
      Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
          Text(
              text = "♠  PLAYER STATS  ♠",
              color = BorderGold,
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.5.sp,
          )

          Spacer(modifier = Modifier.height(4.dp))

          Text(
              text = "YOUR BLACKJACK PERFORMANCE",
              color = Color.White.copy(alpha = 0.65f),
              fontSize = 10.sp,
              fontWeight = FontWeight.Medium,
              letterSpacing = 1.5.sp,
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        HorizontalDivider(
            color = BorderGold.copy(alpha = 0.45f),
            thickness = 1.dp,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          MenuStatCard(
              title = "GAMES",
              value = uiState.totalGamesPlayed.toString(),
              modifier = Modifier.weight(1f),
          )

          MenuStatCard(
              title = "WINS",
              value = uiState.totalWins.toString(),
              modifier = Modifier.weight(1f),
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          MenuStatCard(
              title = "WIN RATE",
              value = "$winRate%",
              modifier = Modifier.weight(1f),
          )

          MenuStatCard(
              title = "NET CHIPS",
              value = netChipsFormatted,
              modifier = Modifier.weight(1f),
          )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "PERFORMANCE",
            color = BorderGold,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.04f))
                    .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
              text = if (winRate >= 50) "♛" else "♟",
              fontSize = 28.sp,
              color = BorderGold,
          )

          Spacer(modifier = Modifier.width(12.dp))

          Column {
            Text(
                text =
                    when {
                      uiState.totalGamesPlayed == 0 -> "NO GAMES YET"
                      winRate >= 50 -> "STRONG PERFORMANCE"
                      else -> "KEEP PLAYING"
                    },
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text =
                    when {
                      uiState.totalGamesPlayed == 0 -> "Play your first game to see your stats."
                      else -> "Your current win rate is $winRate%."
                    },
                color = mutedTextColor,
                fontSize = 11.sp,
            )
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = BorderGold,
                    contentColor = Color.Black,
                ),
        ) {
          Text(
              text = "CLOSE",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp,
          )
        }
      }
    }
  }
}

@Composable
private fun MenuStatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
  Column(
      modifier =
          modifier
              .clip(RoundedCornerShape(12.dp))
              .background(Color.White.copy(alpha = 0.04f))
              .border(
                  width = 1.dp,
                  color = BorderGold.copy(alpha = 0.18f),
                  shape = RoundedCornerShape(12.dp),
              )
              .padding(vertical = 14.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
        text = title,
        color = Color.White.copy(alpha = 0.55f),
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
    )

    Spacer(modifier = Modifier.height(5.dp))

    Text(
        text = value,
        color = BorderGold,
        fontSize = 21.sp,
        fontWeight = FontWeight.Bold,
    )
  }
}

@Composable
private fun RulesBookletDialog(
    surfaceColor: Color,
    backgroundColor: Color,
    mutedTextColor: Color,
    onDismiss: () -> Unit,
) {
  var selectedTab by remember { mutableIntStateOf(0) }
  val tabs = listOf("Basics", "Wildcards", "Strategy")

  Dialog(
      onDismissRequest = onDismiss,
      properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Box(
        modifier =
            Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.75f)).padding(20.dp),
        contentAlignment = Alignment.Center,
    ) {
      Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = surfaceColor),
          modifier = Modifier.fillMaxWidth().border(1.5.dp, BorderGold, RoundedCornerShape(20.dp)),
      ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
                text = "RULEBOOK",
                color = BorderGold,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
            )
            Box(
                modifier =
                    Modifier.size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                        .clickable { onDismiss() },
                contentAlignment = Alignment.Center,
            ) {
              Text(text = "✕", color = Color.White, fontSize = 14.sp)
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          SecondaryTabRow(
              selectedTabIndex = selectedTab,
              containerColor = backgroundColor,
              contentColor = BorderGold,
              indicator = {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(selectedTab),
                    color = BorderGold,
                )
              },
          ) {
            tabs.forEachIndexed { index, title ->
              Tab(
                  selected = selectedTab == index,
                  onClick = { selectedTab = index },
                  text = {
                    Text(
                        text = title,
                        color = if (selectedTab == index) BorderGold else mutedTextColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                    )
                  },
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          Box(
              modifier =
                  Modifier.fillMaxWidth().height(300.dp).verticalScroll(rememberScrollState())
          ) {
            when (selectedTab) {
              0 -> RulesBasicsContent(mutedTextColor)
              1 -> RulesWildcardsContent(mutedTextColor)
              2 -> RulesStrategyContent(mutedTextColor)
            }
          }

          Spacer(modifier = Modifier.height(16.dp))
        }
      }
    }
  }
}

@Composable
private fun RulesBasicsContent(mutedColor: Color) {
  Column {
    Text("THE OBJECTIVE", color = BorderGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    Text(
        "Beat the dealer's hand by getting closer to 21 without busting.",
        color = Color.White,
        fontSize = 13.sp,
        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
    )

    Text("CARD VALUES", color = BorderGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    Text("• 2 through 10: Face value", color = mutedColor, fontSize = 13.sp)
    Text("• Jack, Queen, King: 10 each", color = mutedColor, fontSize = 13.sp)
    Text("• Ace: 1 or 11 (whichever is best)", color = mutedColor, fontSize = 13.sp)

    Spacer(modifier = Modifier.height(12.dp))
    Text("TABLE RULES", color = BorderGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    Text("• Dealer stands on 17.", color = mutedColor, fontSize = 13.sp)
    Text("• Natural Blackjack pays 3:2.", color = mutedColor, fontSize = 13.sp)
  }
}

@Composable
private fun RulesWildcardsContent(mutedColor: Color) {
  Column {
    Text("WILDCARD PAYOUTS", color = BorderGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    Spacer(modifier = Modifier.height(8.dp))
    PayoutRow("7-Card Charlie", "250x", mutedColor)
    PayoutRow("Triple Sevens (7-7-7)", "100x", mutedColor)
    PayoutRow("Blackjack Ace + Jack of Spades", "50x", mutedColor)
    PayoutRow("Suited 6-7-8", "25x", mutedColor)
    PayoutRow("5-Card Charlie", "5x", mutedColor)
  }
}

@Composable
private fun PayoutRow(name: String, multiplier: String, mutedColor: Color) {
  Row(
      modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Text(name, color = mutedColor, fontSize = 12.sp, modifier = Modifier.weight(1f))
    Text(multiplier, color = BorderGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
  }
}

@Composable
private fun RulesStrategyContent(mutedColor: Color) {
  Column {
    Text("BASIC STRATEGY", color = BorderGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    Spacer(modifier = Modifier.height(8.dp))
    Text("• Hard 8 or less: Always HIT.", color = mutedColor, fontSize = 13.sp)
    Text(
        "• Hard 11: Always DOUBLE DOWN if allowed, else HIT.",
        color = mutedColor,
        fontSize = 13.sp,
    )
    Text(
        "• Hard 12-16: STAND if dealer shows 4, 5, or 6; else HIT.",
        color = mutedColor,
        fontSize = 13.sp,
    )
    Text("• Hard 17+: Always STAND.", color = mutedColor, fontSize = 13.sp)
    Text("• Always split Aces and 8s.", color = mutedColor, fontSize = 13.sp)
    Text("• Never split 10s or 5s.", color = mutedColor, fontSize = 13.sp)
  }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MenuScreenPreview() {
  SplitHappensTheme {
    MenuScreen(isDarkTheme = true)
  }
}
