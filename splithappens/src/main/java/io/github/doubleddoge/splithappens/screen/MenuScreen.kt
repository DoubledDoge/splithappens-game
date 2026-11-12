package io.github.doubleddoge.splithappens.screen

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import io.github.doubleddoge.splithappens.game.enum.HandResult
import io.github.doubleddoge.splithappens.ui.components.AppBottomBar
import io.github.doubleddoge.splithappens.ui.theme.CardBackground
import io.github.doubleddoge.splithappens.ui.theme.DarkBackground
import io.github.doubleddoge.splithappens.ui.theme.FeltDarkCard
import io.github.doubleddoge.splithappens.ui.theme.FeltLightBorder
import io.github.doubleddoge.splithappens.ui.theme.GoldFoil
import io.github.doubleddoge.splithappens.ui.theme.LightBackground
import io.github.doubleddoge.splithappens.ui.theme.LightSurface
import io.github.doubleddoge.splithappens.ui.theme.SplitHappensTheme
import io.github.doubleddoge.splithappens.ui.theme.TextMint
import io.github.doubleddoge.splithappens.ui.theme.TextMuted
import io.github.doubleddoge.splithappens.util.triggerClickSound
import io.github.doubleddoge.splithappens.util.triggerDeviceVibration
import io.github.doubleddoge.splithappens.viewmodel.HomeUiState
import io.github.doubleddoge.splithappens.viewmodel.MenuUiState
import kotlin.math.abs

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
	val bgGlowCenter = if (isDarkTheme) CardBackground else Color(0xFF1B634A)
	val bgOuterDark = if (isDarkTheme) DarkBackground else LightBackground
	val surfaceColor = if (isDarkTheme) FeltDarkCard else LightSurface
	val surfaceBorder = if (isDarkTheme) Color.White.copy(alpha = 0.12f) else FeltLightBorder
	val mutedTextColor = if (isDarkTheme) TextMuted else TextMint

	val runFeedback: (() -> Unit) -> Unit = { action ->
		if (!isPreview) {
			if (uiState.soundEnabled) triggerClickSound()
			if (uiState.hapticsEnabled) triggerDeviceVibration(context, 35)
		}
		action()
	}

	BackHandler {
		when {
			uiState.isRulesBookletOpen -> onCloseRules()
			uiState.isLogoutConfirmOpen -> onCloseLogoutConfirm()
			showStatsDialog -> showStatsDialog = false
			else -> onNavigateHome()
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
				Modifier
					.fillMaxSize()
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
					Modifier
						.fillMaxSize()
						.verticalScroll(rememberScrollState())
						.statusBarsPadding()
						.padding(vertical = 20.dp),
				horizontalAlignment = Alignment.CenterHorizontally,
			) {
				// Centered "MENU" Header
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.padding(bottom = 24.dp),
					contentAlignment = Alignment.Center,
				) {
					Text(
						text = "MENU",
						color = GoldFoil,
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

				// 2. Settings (Moved directly underneath Profile)
				MenuButton(
					text = "SETTINGS",
					icon = "⚙️",
					surfaceColor = surfaceColor,
					borderColor = surfaceBorder,
					onClick = { runFeedback(onNavigateToSettings) },
				)

				Spacer(modifier = Modifier.height(14.dp))

				// 3. Stats
				MenuButton(
					text = "STATS",
					icon = "📊",
					surfaceColor = surfaceColor,
					borderColor = surfaceBorder,
					onClick = { runFeedback { showStatsDialog = true } },
				)

				Spacer(modifier = Modifier.height(14.dp))

				// 4. Rules & Strategy
				MenuButton(
					text = "RULES & STRATEGY",
					icon = "📖",
					surfaceColor = surfaceColor,
					borderColor = surfaceBorder,
					onClick = { runFeedback(onOpenRules) },
				)

				Spacer(modifier = Modifier.height(14.dp))

				// 5. Switch Profile
				MenuButton(
					text = "SWITCH PROFILE",
					icon = "👥",
					surfaceColor = surfaceColor,
					borderColor = GoldFoil.copy(alpha = 0.45f),
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

	// Modal In-Screen Rules Booklet (Dismissed via bottom CLOSE button, outside tap, or back)
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
					Text("SWITCH", color = GoldFoil, fontWeight = FontWeight.Bold)
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
			CardDefaults.cardColors(containerColor = if (isPrimary) GoldFoil else surfaceColor),
		modifier =
			modifier
				.fillMaxWidth()
				.height(58.dp)
				.border(
					width = 1.dp,
					color = if (isPrimary) GoldFoil else borderColor,
					shape = RoundedCornerShape(16.dp),
				)
				.clickable { onClick() },
	) {
		Row(
			modifier = Modifier
				.fillMaxSize()
				.padding(horizontal = 20.dp),
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
					color = if (isPrimary) DarkBackground else Color.White,
					fontSize = 14.sp,
					fontWeight = FontWeight.Bold,
					letterSpacing = 1.2.sp,
				)
			}
			Text(
				text = "›",
				color = if (isPrimary) DarkBackground else Color.White.copy(alpha = 0.5f),
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
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 12.dp),
			shape = RoundedCornerShape(20.dp),
			color = surfaceColor,
			border = BorderStroke(1.dp, GoldFoil),
			tonalElevation = 8.dp,
		) {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.padding(20.dp)
			) {
				Column(
					horizontalAlignment = Alignment.CenterHorizontally,
					modifier = Modifier.fillMaxWidth(),
				) {
					Text(
						text = "♠  PLAYER STATS  ♠",
						color = GoldFoil,
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
					color = GoldFoil.copy(alpha = 0.45f),
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
					color = GoldFoil,
					fontSize = 11.sp,
					fontWeight = FontWeight.Bold,
					letterSpacing = 1.2.sp,
				)

				Spacer(modifier = Modifier.height(8.dp))

				Row(
					modifier =
						Modifier
							.fillMaxWidth()
							.clip(RoundedCornerShape(12.dp))
							.background(Color.White.copy(alpha = 0.04f))
							.padding(14.dp),
					verticalAlignment = Alignment.CenterVertically,
				) {
					Text(
						text = if (winRate >= 50) "♛" else "♟",
						fontSize = 28.sp,
						color = GoldFoil,
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
					modifier = Modifier
						.fillMaxWidth()
						.height(48.dp),
					shape = RoundedCornerShape(12.dp),
					colors =
						ButtonDefaults.buttonColors(
							containerColor = GoldFoil,
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
					color = GoldFoil.copy(alpha = 0.18f),
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
			color = GoldFoil,
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
				Modifier
					.fillMaxSize()
					.background(Color.Black.copy(alpha = 0.75f))
					.padding(20.dp),
			contentAlignment = Alignment.Center,
		) {
			Card(
				shape = RoundedCornerShape(20.dp),
				colors = CardDefaults.cardColors(containerColor = surfaceColor),
				modifier = Modifier
					.fillMaxWidth()
					.border(1.5.dp, GoldFoil, RoundedCornerShape(20.dp)),
			) {
				Column(
					modifier = Modifier
						.fillMaxWidth()
						.padding(20.dp)
				) {
					Row(
						modifier = Modifier.fillMaxWidth(),
						horizontalArrangement = Arrangement.SpaceBetween,
						verticalAlignment = Alignment.CenterVertically,
					) {
						Text(
							text = "RULEBOOK",
							color = GoldFoil,
							fontSize = 18.sp,
							fontWeight = FontWeight.Black,
							letterSpacing = 1.sp,
						)
					}

					Spacer(modifier = Modifier.height(16.dp))

					SecondaryTabRow(
						selectedTabIndex = selectedTab,
						containerColor = backgroundColor,
						contentColor = GoldFoil,
						indicator = {
							TabRowDefaults.SecondaryIndicator(
								modifier = Modifier.tabIndicatorOffset(selectedTab),
								color = GoldFoil,
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
										color = if (selectedTab == index) GoldFoil else mutedTextColor,
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
							Modifier
								.fillMaxWidth()
								.height(300.dp)
								.verticalScroll(rememberScrollState())
					) {
						when (selectedTab) {
							0 -> RulesBasicsContent(mutedTextColor)
							1 -> RulesWildcardsContent(mutedTextColor)
							2 -> RulesStrategyContent(mutedTextColor)
						}
					}

					Spacer(modifier = Modifier.height(18.dp))

					// Bottom Close Button matching Stats Dialog
					Button(
						onClick = onDismiss,
						modifier = Modifier
							.fillMaxWidth()
							.height(48.dp),
						shape = RoundedCornerShape(12.dp),
						colors =
							ButtonDefaults.buttonColors(
								containerColor = GoldFoil,
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
}

@Composable
private fun RulesBasicsContent(mutedColor: Color) {
	Column {
		Text("THE OBJECTIVE", color = GoldFoil, fontWeight = FontWeight.Bold, fontSize = 13.sp)
		Text(
			"Beat the dealer's hand by getting closer to 21 without busting.",
			color = Color.White,
			fontSize = 13.sp,
			modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
		)

		Text("CARD VALUES", color = GoldFoil, fontWeight = FontWeight.Bold, fontSize = 13.sp)
		Text("• 2 through 10: Face value", color = mutedColor, fontSize = 13.sp)
		Text("• Jack, Queen, King: 10 each", color = mutedColor, fontSize = 13.sp)
		Text("• Ace: 1 or 11 (whichever is best)", color = mutedColor, fontSize = 13.sp)

		Spacer(modifier = Modifier.height(12.dp))
		Text("TABLE RULES", color = GoldFoil, fontWeight = FontWeight.Bold, fontSize = 13.sp)
		Text("• Dealer stands on 17.", color = mutedColor, fontSize = 13.sp)
		Text("• Natural Blackjack pays 3:2.", color = mutedColor, fontSize = 13.sp)
	}
}

@Composable
private fun RulesWildcardsContent(mutedColor: Color) {
	Column {
		Text("WILDCARD PAYOUTS", color = GoldFoil, fontWeight = FontWeight.Bold, fontSize = 13.sp)
		Spacer(modifier = Modifier.height(8.dp))
		PayoutRow("7-Card Charlie", "${HandResult.SEVEN_CARD.multiplier.toInt()}x", mutedColor)
		PayoutRow("6-Card Charlie", "${HandResult.SIX_CARD.multiplier.toInt()}x", mutedColor)
		PayoutRow("Triple Sevens (7-7-7)", "${HandResult.TRIPLE_SEVEN.multiplier.toInt()}x", mutedColor)
		PayoutRow("Natural Blackjack (Ace + Jack)", "${HandResult.NATURAL_BLACKJACK.multiplier.toInt()}x", mutedColor)
		PayoutRow("Queen + Ace Blackjack", "${HandResult.QUEEN_JACK.multiplier.toInt()}x", mutedColor)
	}
}

@Composable
private fun PayoutRow(name: String, multiplier: String, mutedColor: Color) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(vertical = 4.dp),
		horizontalArrangement = Arrangement.SpaceBetween,
	) {
		Text(name, color = mutedColor, fontSize = 12.sp, modifier = Modifier.weight(1f))
		Text(multiplier, color = GoldFoil, fontWeight = FontWeight.Bold, fontSize = 12.sp)
	}
}

@Composable
private fun RulesStrategyContent(mutedColor: Color) {
	Column {
		Text("BASIC STRATEGY", color = GoldFoil, fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
