package io.github.doubleddoge.splithappens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import io.github.doubleddoge.splithappens.ui.theme.SplitHappensTheme
import kotlin.math.abs

// Color Palette
val DarkBackground = Color(0xFF0B1B15)
val CardBackground = Color(0xFF0F3A2E)
val BorderGold = Color(0xFFD4A311)
val ButtonBackground = Color(0xFFD8D8D8)
val ButtonTextColor = Color(0xFF1E1E1E)

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onPlayClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    HomeScreenContent(
        uiState = uiState,
        onPlayClick = onPlayClick
    )
}

@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    onPlayClick: () -> Unit = {}
) {
    var showStatsDialog by remember { mutableStateOf(false) }
    var showRulesDialog by remember { mutableStateOf(false) }
    var selectedIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = {
            // Anchored Bottom Navigation Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(60.dp),
                shape = RoundedCornerShape(12.dp),
                color = CardBackground
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Home Icon
                    IconButton(onClick = { selectedIndex = 0 }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_home),
                            contentDescription = "Home",
                            tint = if (selectedIndex == 0) BorderGold else Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Text(text = "|", color = BorderGold.copy(alpha = 0.3f), fontSize = 18.sp)

                    // 2. Games / Cards Icon
                    IconButton(onClick = { selectedIndex = 1 }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_blackjackicon),
                            contentDescription = "Games",
                            tint = if (selectedIndex == 1) BorderGold else Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Text(text = "|", color = BorderGold.copy(alpha = 0.3f), fontSize = 18.sp)

                    // 3. Settings Icon
                    IconButton(onClick = { selectedIndex = 2 }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_settings),
                            contentDescription = "Settings",
                            tint = if (selectedIndex == 2) BorderGold else Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = BorderGold
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // 1. User Header Banner
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = CardBackground,
                        border = BorderStroke(1.dp, Color(0xFF1A5243))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color.Transparent, CircleShape)
                                    .border(2.dp, BorderGold, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = uiState.displayName.take(1).uppercase(),
                                    color = BorderGold,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(verticalArrangement = Arrangement.Center) {
                                Text(
                                    text = uiState.displayName,
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF08261E), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 10.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "🪙 R${uiState.chipsOwned}",
                                        color = BorderGold,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2. STATS & RULES Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clickable { showStatsDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            color = CardBackground,
                            border = BorderStroke(1.dp, Color(0xFF1A5243))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "📊", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "STATS",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clickable { showRulesDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            color = CardBackground,
                            border = BorderStroke(1.dp, Color(0xFF1A5243))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "📖", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "RULES",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 3. Classic Table Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderGold, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Text(
                                text = "Classic Table",
                                color = BorderGold,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Standard Rules • Minimum Bet R100",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = onPlayClick,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ButtonBackground,
                                    contentColor = ButtonTextColor
                                )
                            ) {
                                Text(
                                    text = "PLAY NOW",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                if (showStatsDialog) {
                    StatsDialog(
                        uiState = uiState,
                        onDismiss = { showStatsDialog = false }
                    )
                }

                if (showRulesDialog) {
                    RulesDialog(onDismiss = { showRulesDialog = false })
                }
            }
        }
    }
}

@Composable
fun StatsDialog(
    uiState: HomeUiState,
    onDismiss: () -> Unit
) {
    val winRate = if (uiState.totalGamesPlayed > 0) {
        (uiState.totalWins.toDouble() / uiState.totalGamesPlayed * 100).toInt()
    } else {
        0
    }

    val netChipsFormatted = if (uiState.netChipsEarned >= 0) {
        "+R${uiState.netChipsEarned}"
    } else {
        "-R${abs(uiState.netChipsEarned)}"
    }

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            shape = RoundedCornerShape(20.dp),
            color = CardBackground,
            border = BorderStroke(1.dp, BorderGold),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {

                // HEADER
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Text(
                        text = "♠  PLAYER STATS  ♠",
                        color = BorderGold,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "YOUR BLACKJACK PERFORMANCE",
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider(
                    color = BorderGold.copy(alpha = 0.45f),
                    thickness = 1.dp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // STATISTICS GRID
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    StatCard(
                        title = "GAMES",
                        value = uiState.totalGamesPlayed.toString(),
                        modifier = Modifier.weight(1f)
                    )

                    StatCard(
                        title = "WINS",
                        value = uiState.totalWins.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    StatCard(
                        title = "WIN RATE",
                        value = "$winRate%",
                        modifier = Modifier.weight(1f)
                    )

                    StatCard(
                        title = "NET CHIPS",
                        value = netChipsFormatted,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // PERFORMANCE SECTION
                Text(
                    text = "PERFORMANCE",
                    color = BorderGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.04f))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = if (winRate >= 50) "♛" else "♟",
                        fontSize = 28.sp,
                        color = BorderGold
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = when {
                                uiState.totalGamesPlayed == 0 -> "NO GAMES YET"
                                winRate >= 50 -> "STRONG PERFORMANCE"
                                else -> "KEEP PLAYING"
                            },
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(3.dp))

                        Text(
                            text = when {
                                uiState.totalGamesPlayed == 0 ->
                                    "Play your first game to see your stats."
                                else ->
                                    "Your current win rate is $winRate%."
                            },
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // CLOSE BUTTON
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BorderGold,
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = "CLOSE",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .border(
                width = 1.dp,
                color = BorderGold.copy(alpha = 0.18f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = title,
            color = Color.White.copy(alpha = 0.55f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = value,
            color = BorderGold,
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
@Composable
fun RulesDialog(
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            shape = RoundedCornerShape(20.dp),
            color = CardBackground,
            border = BorderStroke(1.dp, BorderGold),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {

                // HEADER
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Text(
                        text = "♠  BLACKJACK  ♠",
                        color = BorderGold,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "HOW TO PLAY",
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 2.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // GOLD DIVIDER
                HorizontalDivider(
                    color = BorderGold.copy(alpha = 0.45f),
                    thickness = 1.dp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // RULE 1
                RuleItem(
                    icon = "🎯",
                    title = "OBJECTIVE",
                    description = "Get closer to 21 than the dealer without going over."
                )

                Spacer(modifier = Modifier.height(10.dp))

                // RULE 2
                RuleItem(
                    icon = "🃏",
                    title = "CARD VALUES",
                    description = "Face cards are worth 10. Aces count as 1 or 11."
                )

                Spacer(modifier = Modifier.height(10.dp))

                // RULE 3
                RuleItem(
                    icon = "✋",
                    title = "YOUR TURN",
                    description = "Hit to take another card or Stand to keep your current hand."
                )

                Spacer(modifier = Modifier.height(10.dp))

                // RULE 4
                RuleItem(
                    icon = "♣",
                    title = "DEALER RULES",
                    description = "The dealer must Hit on 16 or below and Stand on 17 or higher."
                )

                Spacer(modifier = Modifier.height(18.dp))

                // GOT IT BUTTON
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BorderGold,
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = "GOT IT",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}
@Composable
private fun RuleItem(
    icon: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {

        // ICON
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(BorderGold.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                fontSize = 17.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // TEXT
        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = title,
                color = BorderGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = description,
                color = Color.White.copy(alpha = 0.82f),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    SplitHappensTheme {
        HomeScreenContent(
            uiState = HomeUiState(
                displayName = "Player 1",
                chipsOwned = 2500L,
                totalGamesPlayed = 12,
                totalWins = 7,
                netChipsEarned = 1400L,
                isLoading = false
            )
        )
    }
}