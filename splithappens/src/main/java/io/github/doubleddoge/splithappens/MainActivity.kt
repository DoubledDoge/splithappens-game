package io.github.doubleddoge.splithappens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.doubleddoge.splithappens.ui.theme.SplitHappensTheme
import kotlinx.coroutines.delay
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room3.Database
import io.github.doubleddoge.splithappens.data.buildSplitHappensDatabase

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
				var currentScreen by remember { mutableStateOf("splash") }

				when (currentScreen) {
					"splash" -> SplashScreen(
						onTimeout = { currentScreen = "home" }
					)
					"home" -> HomeScreen(
						viewModel = homeViewModel,
						onPlayClick = {
							// TODO: Add navigation to Game screen
						}
					)
				}
			}
		}
	}
}

