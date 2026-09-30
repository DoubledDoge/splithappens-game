package io.github.doubleddoge.splithappens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.doubleddoge.splithappens.data.buildSplitHappensDatabase
import io.github.doubleddoge.splithappens.ui.theme.SplitHappensTheme

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()

		setContent {
			SplitHappensTheme {
				val database = remember { buildSplitHappensDatabase(applicationContext) }
				val factory = remember {
					SettingsViewModelFactory(
						userDao = database.userDao(),
						gameHistoryDao = database.gameHistoryDao()
					)
				}
				val viewModel: SettingsViewModel = viewModel(factory = factory)
				val uiState by viewModel.uiState.collectAsState()

				var currentScreen by remember { mutableStateOf("settings") }

				when (currentScreen) {
					"settings" -> {
						SettingsScreen(
							playerName = uiState.displayName,
							chipsOwned = uiState.chipsOwned,
							onNavigateBack = { /* Game home navigation */ },
							onNavigateToRules = { currentScreen = "rules" },
							onNavigateToStats = { currentScreen = "stats" },
							onResetChips = { viewModel.resetBankroll(2500L) }
						)
					}
					"rules" -> {
						RulesScreen(onNavigateBack = { currentScreen = "settings" })
					}
					"stats" -> {
						StatsScreen(
							totalHands = uiState.totalHands,
							handsWon = uiState.handsWon,
							bestStreak = uiState.bestStreak,
							netEarnings = uiState.netEarnings,
							onNavigateBack = { currentScreen = "settings" }
						)
					}
				}
			}
		}
	}
}

// ------------------------------------------------------------------
// 1. RULES & HOW TO PLAY SCREEN
// ------------------------------------------------------------------
@Composable
fun RulesScreen(onNavigateBack: () -> Unit) {
	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(
				brush = Brush.radialGradient(
					colors = listOf(Color(0xFF0F3022), Color(0xFF071B12)),
					radius = 1200f
				)
			)
			.padding(horizontal = 20.dp)
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.verticalScroll(rememberScrollState())
				.padding(vertical = 32.dp)
		) {
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(top = 8.dp, bottom = 20.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				Box(
					modifier = Modifier
						.size(40.dp)
						.clip(CircleShape)
						.background(Color(0xFF133827))
						.border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
						.clickable { onNavigateBack() },
					contentAlignment = Alignment.Center
				) {
					Text(
						text = "←",
						color = Color.White,
						fontSize = 20.sp,
						fontWeight = FontWeight.Bold
					)
				}

				Spacer(modifier = Modifier.width(16.dp))

				Text(
					text = "HOW TO PLAY",
					color = Color(0xFFE5B036),
					fontSize = 22.sp,
					fontWeight = FontWeight.Black,
					letterSpacing = 2.sp
				)
			}

			RuleCard(
				title = "🎯 Objective",
				body = "Beat the dealer by getting a hand value closer to 21 than the dealer without exceeding 21 (busting)."
			)

			RuleCard(
				title = "🃏 Card Values",
				body = "• Number cards (2–10): Face value\n• Jacks, Queens, Kings: 10\n• Aces: 1 or 11 (whichever makes the best hand)"
			)

			RuleCard(
				title = "⚡ Player Actions",
				body = "• HIT: Draw another card.\n• STAND: Keep your hand.\n• DOUBLE DOWN: Double your original wager, take exactly one more card, and stand.\n• SPLIT: If your initial cards share rank, split them into separate hands with equal wagers.\n• SURRENDER: Forfeit your hand and recover half your bet."
			)

			RuleCard(
				title = "🔥 Special Wildcard Payouts",
				body = "• 7-Card Charlie (7 cards without busting): 250x\n• 6-Card Charlie (6 cards without busting): 120x\n• Triple Seven (21 with 7-7-7): 100x\n• Natural Blackjack (Jack + Ace): 50x\n• Queen Jack (Queen + Ace): 10x\n• Standard Blackjack (10-value + Ace): 3x\n• Regular Win: 2x (1:1 net profit)\n• Push (Tie): Bet returned (1x)\n• Surrender: Half bet returned (0.5x)"
			)

			RuleCard(
				title = "🛡️ Insurance & Dealer Rules",
				body = "• Insurance: Pays 2:1 (3x return) if dealer holds blackjack.\n• Dealer draws cards until reaching a total of 17 or higher."
			)
		}
	}
}

@Composable
private fun RuleCard(title: String, body: String) {
	Card(
		shape = RoundedCornerShape(14.dp),
		colors = CardDefaults.cardColors(containerColor = Color(0xFF133827)),
		modifier = Modifier
			.fillMaxWidth()
			.padding(vertical = 6.dp)
			.border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
	) {
		Column(modifier = Modifier.padding(16.dp)) {
			Text(
				text = title,
				color = Color(0xFFE5B036),
				fontWeight = FontWeight.Bold,
				fontSize = 16.sp
			)
			Spacer(modifier = Modifier.height(6.dp))
			Text(
				text = body,
				color = Color.White.copy(alpha = 0.85f),
				fontSize = 13.sp,
				lineHeight = 20.sp
			)
		}
	}
}

// ------------------------------------------------------------------
// 2. PLAYER STATISTICS SCREEN
// ------------------------------------------------------------------
@Composable
fun StatsScreen(
	totalHands: Int = 0,
	handsWon: Int = 0,
	bestStreak: Int = 0,
	netEarnings: Long = 0L,
	onNavigateBack: () -> Unit
) {
	val winRate = if (totalHands > 0) {
		"%.1f".format((handsWon.toFloat() / totalHands.toFloat()) * 100f)
	} else {
		"0.0"
	}

	val earningsPrefix = if (netEarnings >= 0) "+" else ""

	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(
				brush = Brush.radialGradient(
					colors = listOf(Color(0xFF0F3022), Color(0xFF071B12)),
					radius = 1200f
				)
			)
			.padding(horizontal = 20.dp)
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.verticalScroll(rememberScrollState())
				.padding(vertical = 32.dp)
		) {
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(top = 8.dp, bottom = 20.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				Box(
					modifier = Modifier
						.size(40.dp)
						.clip(CircleShape)
						.background(Color(0xFF133827))
						.border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
						.clickable { onNavigateBack() },
					contentAlignment = Alignment.Center
				) {
					Text(
						text = "←",
						color = Color.White,
						fontSize = 20.sp,
						fontWeight = FontWeight.Bold
					)
				}

				Spacer(modifier = Modifier.width(16.dp))

				Text(
					text = "STATISTICS",
					color = Color(0xFFE5B036),
					fontSize = 22.sp,
					fontWeight = FontWeight.Black,
					letterSpacing = 2.sp
				)
			}

			StatRowDisplay(label = "Total Hands Played", value = "$totalHands")
			StatRowDisplay(label = "Hands Won", value = "$handsWon ($winRate%)")
			StatRowDisplay(label = "Best Win Streak", value = "$bestStreak Hands")
			StatRowDisplay(label = "Net Chip Earnings", value = "$earningsPrefix$netEarnings 🪙")
		}
	}
}

@Composable
private fun StatRowDisplay(label: String, value: String) {
	Surface(
		shape = RoundedCornerShape(12.dp),
		color = Color(0xFF133827),
		modifier = Modifier
			.fillMaxWidth()
			.padding(vertical = 6.dp)
			.border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			Text(label, color = Color(0xFF9EBAAA), fontSize = 14.sp)
			Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
		}
	}
}

// ------------------------------------------------------------------
// 3. COMPOSE PREVIEWS
// ------------------------------------------------------------------
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SettingsPreview() {
	SplitHappensTheme {
		SettingsScreen()
	}
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RulesPreview() {
	SplitHappensTheme {
		RulesScreen(onNavigateBack = {})
	}
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun StatsPreview() {
	SplitHappensTheme {
		StatsScreen(onNavigateBack = {})
	}
}