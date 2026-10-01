package io.github.doubleddoge.splithappens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModelProvider
import io.github.doubleddoge.splithappens.ui.theme.SplitHappensTheme

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()

		setContent {
			SplitHappensTheme {
				val factory = remember { SettingsViewModelFactory(application) }
				val viewModel = remember {
					ViewModelProvider(this@MainActivity, factory)[SettingsViewModel::class.java]
				}
				val uiState by viewModel.uiState.collectAsState()

				SettingsScreen(
					uiState = uiState,
					onToggleDarkMode = viewModel::toggleDarkMode,
					onToggleShowCardTotals = viewModel::toggleShowCardTotals,
					onToggleDealerBanner = viewModel::toggleShowDealerBanner,
					onToggleSound = viewModel::toggleSound,
					onToggleHaptics = viewModel::toggleHaptics,
					onToggleFastDeal = viewModel::toggleFastDeal,
					onToggleAutoStandOn21 = viewModel::toggleAutoStandOn21,
					onNavigateBack = { finish() }
				)
			}
		}
	}
}