package io.github.doubleddoge.splithappens

import AppNavigation
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import io.github.doubleddoge.splithappens.data.buildSplitHappensDatabase
import io.github.doubleddoge.splithappens.ui.theme.SplitHappensTheme

class MainActivity : ComponentActivity() {

	private val database by lazy {
		buildSplitHappensDatabase(applicationContext)
	}

	private val homeViewModel: HomeViewModel by viewModels {
		HomeViewModelFactory(
			userDao = database.userDao(),
			gameHistoryDao = database.gameHistoryDao()
		)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		setContent {
			SplitHappensTheme {
				AppNavigation(
					homeViewModel = homeViewModel
				)
			}
		}
	}
}