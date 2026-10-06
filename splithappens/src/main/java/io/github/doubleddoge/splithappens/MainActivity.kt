package io.github.doubleddoge.splithappens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModelProvider
import io.github.doubleddoge.splithappens.ui.theme.SplitHappensTheme

enum class AppDestination {
	MENU,
	SETTINGS
}

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()

		setContent {
			SplitHappensTheme {
				var currentDestination by remember { mutableStateOf(AppDestination.MENU) }

				// SettingViewModel setup
				val settingsFactory = remember { SettingViewModelFactory(application) }
				val settingsViewModel = remember {
					ViewModelProvider(this@MainActivity, settingsFactory)[SettingViewModel::class.java]
				}
				val settingsUiState by settingsViewModel.uiState.collectAsState()

				// MenuViewModel setup
				val menuFactory = remember { MenuViewModelFactory(application) }
				val menuViewModel = remember {
					ViewModelProvider(this@MainActivity, menuFactory)[MenuViewModel::class.java]
				}
				val menuUiState by menuViewModel.uiState.collectAsState()

				when (currentDestination) {
					AppDestination.MENU -> {
						MenuScreen(
							uiState = menuUiState,
							onOpenRules = { menuViewModel.openRulesBooklet() },
							onCloseRules = { menuViewModel.closeRulesBooklet() },
							onOpenLogoutConfirm = { menuViewModel.openLogoutConfirmation() },
							onCloseLogoutConfirm = { menuViewModel.closeLogoutConfirmation() },
							onConfirmLogout = {
								menuViewModel.logout(onLoggedOut = { finish() })
							},
							onResumeGame = { finish() },
							onNavigateToProfile = { /* Hook for Profile screen */ },
							onNavigateToSettings = { currentDestination = AppDestination.SETTINGS }
						)
					}

					AppDestination.SETTINGS -> {
						ScreenSettings(
							uiState = settingsUiState,
							onToggleDarkTheme = { settingsViewModel.toggleDarkTheme(it) },
							onToggleShowCardTotal = { settingsViewModel.toggleShowCardTotal(it) },
							onToggleDealerRuleBanner = { settingsViewModel.toggleDealerRuleBanner(it) },
							onToggleSoundEffects = { settingsViewModel.toggleSoundEffects(it) },
							onToggleHapticVibration = { settingsViewModel.toggleHapticVibration(it) },
							onToggleFastDeal = { settingsViewModel.toggleFastDeal(it) },
							onToggleAutoStandOn21 = { settingsViewModel.toggleAutoStandOn21(it) },
							onNavigateBack = { currentDestination = AppDestination.MENU }
						)
					}
				}
			}
		}
	}
}