package io.github.doubleddoge.splithappens

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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import io.github.doubleddoge.splithappens.ui.theme.SplitHappensTheme

// Distinct names to avoid "conflicting declarations" in package io.github.doubleddoge.splithappens
private val MenuTheme_BackgroundDark = Color(0xFF071B12)
private val MenuTheme_GlowCenter = Color(0xFF0F3022)
private val MenuTheme_GreenSurface = Color(0xFF133827)
private val MenuTheme_MutedText = Color(0xFF9EBAAA)
private val MenuTheme_RedAlert = Color(0xFFB3261E)

@Composable
fun MenuScreen(
    uiState: MenuUiState = MenuUiState(),
    onOpenRules: () -> Unit = {},
    onCloseRules: () -> Unit = {},
    onOpenLogoutConfirm: () -> Unit = {},
    onCloseLogoutConfirm: () -> Unit = {},
    onConfirmLogout: () -> Unit = {},
    onResumeGame: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current

    val runFeedback: (() -> Unit) -> Unit = { action ->
        if (!isPreview) {
            if (uiState.soundEnabled) triggerClickSound()
            if (uiState.hapticsEnabled) triggerDeviceVibration(context, 35)
        }
        action()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(MenuTheme_GlowCenter, MenuTheme_BackgroundDark),
                    radius = 1200f
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row: Top-Left Close Button and Centered "MENU" Label
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MenuTheme_GreenSurface)
                        .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                        .clickable { runFeedback(onResumeGame) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✕",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "MENU",
                    color = BorderGold,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp
                )

                // Spacer on the right to keep "MENU" centered
                Spacer(modifier = Modifier.size(40.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Profile
            MenuButton(
                text = "PROFILE",
                icon = "👤",
                onClick = { runFeedback(onNavigateToProfile) }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Continue Active Game
            MenuButton(
                text = "CONTINUE GAME",
                icon = "♠️",
                isPrimary = true,
                onClick = { runFeedback(onResumeGame) }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Rules & Strategy
            MenuButton(
                text = "RULES & STRATEGY",
                icon = "📖",
                onClick = { runFeedback(onOpenRules) }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Settings
            MenuButton(
                text = "SETTINGS",
                icon = "⚙️",
                onClick = { runFeedback(onNavigateToSettings) }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Logout
            MenuButton(
                text = "LOGOUT",
                icon = "🚪",
                borderColor = MenuTheme_RedAlert.copy(alpha = 0.6f),
                onClick = { runFeedback(onOpenLogoutConfirm) }
            )
        }
    }

    // Modal In-Screen Rules Booklet
    if (uiState.isRulesBookletOpen) {
        RulesBookletDialog(onDismiss = onCloseRules)
    }

    // Modal Logout Confirmation
    if (uiState.isLogoutConfirmOpen) {
        AlertDialog(
            onDismissRequest = onCloseLogoutConfirm,
            containerColor = MenuTheme_GreenSurface,
            title = {
                Text(
                    text = "Log Out?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "You will forfeit your current round and return to the login screen.",
                    color = MenuTheme_MutedText,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { runFeedback(onConfirmLogout) }
                ) {
                    Text("LOG OUT", color = MenuTheme_RedAlert, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = onCloseLogoutConfirm) {
                    Text("CANCEL", color = Color.White)
                }
            }
        )
    }
}

@Composable
private fun MenuButton(
    text: String,
    icon: String,
    isPrimary: Boolean = false,
    borderColor: Color = Color.White.copy(alpha = 0.12f),
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPrimary) BorderGold else MenuTheme_GreenSurface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .border(
                width = 1.dp,
                color = if (isPrimary) BorderGold else borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = icon,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = text,
                    color = if (isPrimary) MenuTheme_BackgroundDark else Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
            }
            Text(
                text = "›",
                color = if (isPrimary) MenuTheme_BackgroundDark else Color.White.copy(alpha = 0.5f),
                fontSize = 20.sp,
                fontWeight = FontWeight.Light
            )
        }
    }
}

@Composable
private fun RulesBookletDialog(onDismiss: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Basics", "Wildcards", "Strategy")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MenuTheme_GreenSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, BorderGold, RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "RULEBOOK",
                            color = BorderGold,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.1f))
                                .clickable { onDismiss() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "✕", color = Color.White, fontSize = 14.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    SecondaryTabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MenuTheme_BackgroundDark,
                        contentColor = BorderGold,
                        indicator = {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(selectedTab),
                                color = BorderGold
                            )
                        }
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = {
                                    Text(
                                        text = title,
                                        color = if (selectedTab == index) BorderGold else MenuTheme_MutedText,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        when (selectedTab) {
                            0 -> RulesBasicsContent()
                            1 -> RulesWildcardsContent()
                            2 -> RulesStrategyContent()
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BorderGold),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "BACK TO GAME",
                            color = MenuTheme_BackgroundDark,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RulesBasicsContent() {
    Column {
        Text("THE OBJECTIVE", color = BorderGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Text(
            "Beat the dealer's hand by getting closer to 21 without busting.",
            color = Color.White,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        Text("CARD VALUES", color = BorderGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Text("• 2 through 10: Face value", color = MenuTheme_MutedText, fontSize = 13.sp)
        Text("• Jack, Queen, King: 10 each", color = MenuTheme_MutedText, fontSize = 13.sp)
        Text("• Ace: 1 or 11 (whichever is best)", color = MenuTheme_MutedText, fontSize = 13.sp)

        Spacer(modifier = Modifier.height(12.dp))
        Text("TABLE RULES", color = BorderGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Text("• Dealer stands on 17.", color = MenuTheme_MutedText, fontSize = 13.sp)
        Text("• Natural Blackjack pays 3:2.", color = MenuTheme_MutedText, fontSize = 13.sp)
    }
}

@Composable
private fun RulesWildcardsContent() {
    Column {
        Text("WILDCARD PAYOUTS", color = BorderGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(8.dp))
        PayoutRow("7-Card Charlie", "250x")
        PayoutRow("Triple Sevens (7-7-7)", "100x")
        PayoutRow("Blackjack Ace + Jack of Spades", "50x")
        PayoutRow("Suited 6-7-8", "25x")
        PayoutRow("5-Card Charlie", "5x")
    }
}

@Composable
private fun PayoutRow(name: String, multiplier: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(name, color = MenuTheme_MutedText, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Text(multiplier, color = BorderGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@Composable
private fun RulesStrategyContent() {
    Column {
        Text("BASIC STRATEGY", color = BorderGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text("• Hard 8 or less: Always HIT.", color = MenuTheme_MutedText, fontSize = 13.sp)
        Text("• Hard 11: Always DOUBLE DOWN if allowed, else HIT.", color = MenuTheme_MutedText, fontSize = 13.sp)
        Text("• Hard 12-16: STAND if dealer shows 4, 5, or 6; else HIT.", color = MenuTheme_MutedText, fontSize = 13.sp)
        Text("• Hard 17+: Always STAND.", color = MenuTheme_MutedText, fontSize = 13.sp)
        Text("• Always split Aces and 8s.", color = MenuTheme_MutedText, fontSize = 13.sp)
        Text("• Never split 10s or 5s.", color = MenuTheme_MutedText, fontSize = 13.sp)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MenuScreenPreview() {
    SplitHappensTheme {
        MenuScreen()
    }
}