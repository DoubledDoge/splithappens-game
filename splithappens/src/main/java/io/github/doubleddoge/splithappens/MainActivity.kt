package io.github.doubleddoge.splithappens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		setContent {
			SplitHappensTheme {
				SplashScreen()
			}
		}
	}
}
// Figma Color Palette
val CasinoBackgroundDark = Color(0xFF071B12)
val CasinoGlowCenter = Color(0xFF0F3022)
val CasinoGoldAccent = Color(0xFFE5B036)
val CasinoCardBackground = Color(0xFFF9F9F9)


/**
 * Screen 01 - Splash Screen matching Figma spec.
 */
@Composable
fun SplashScreen(
	onTimeout: () -> Unit = {}
) {
	// Track loading progress (0.0f to 1.0f)
	var progressTarget by remember { mutableFloatStateOf(0f) }
	val animatedProgress by animateFloatAsState(
		targetValue = progressTarget,
		animationSpec = tween(durationMillis = 2500),
		label = "LoadingProgress"
	)

	LaunchedEffect(Unit) {
		progressTarget = 1.0f // Animate progress bar to 100%
		delay(2800L)          // Total splash duration
		onTimeout()
	}

	// Outer background with subtle gradient glow in center
	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(
				brush = Brush.radialGradient(
					colors = listOf(CasinoGlowCenter, CasinoBackgroundDark),
					radius = 1200f
				)
			),
		contentAlignment = Alignment.Center
	) {
		// Main content column centered vertically
		Column(
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.Center,
			modifier = Modifier.padding(24.dp)
		) {
			// --- 1. Ace Card Graphic ---
			Surface(
				modifier = Modifier.size(width = 110.dp, height = 150.dp),
				shape = RoundedCornerShape(12.dp),
				color = CasinoCardBackground,
				shadowElevation = 8.dp
			) {
				Box(
					modifier = Modifier
						.fillMaxSize()
						.padding(8.dp)
				) {
					// Top-left card rank
					Text(
						text = "A",
						color = Color.Black,
						fontSize = 18.sp,
						fontWeight = FontWeight.Bold,
						modifier = Modifier.align(Alignment.TopStart)
					)
					// Centered Spade symbol
					Text(
						text = "♠",
						color = Color.Black,
						fontSize = 54.sp,
						modifier = Modifier.align(Alignment.Center)
					)
				}
			}

			Spacer(modifier = Modifier.height(32.dp))

			// --- 2. Title & Subtitle ---
			Text(
				text = "BLACKJACK",
				color = CasinoGoldAccent,
				fontSize = 28.sp,
				fontWeight = FontWeight.Black,
				letterSpacing = 4.sp
			)

			Spacer(modifier = Modifier.height(4.dp))

			Text(
				text = "CLASSIC 21",
				color = Color.White.copy(alpha = 0.7f),
				fontSize = 12.sp,
				fontWeight = FontWeight.Medium,
				letterSpacing = 2.sp
			)

			Spacer(modifier = Modifier.height(100.dp))

			// --- 3. Loading Bar & Text ---
			Text(
				text = "Loading",
				color = Color.White,
				fontSize = 12.sp,
				fontWeight = FontWeight.Medium
			)

			Spacer(modifier = Modifier.height(8.dp))

			LinearProgressIndicator(
				progress = { animatedProgress },
				modifier = Modifier
					.width(140.dp)
					.height(3.dp),
				color = CasinoGoldAccent,
				trackColor = Color.White.copy(alpha = 0.2f),
			)
		}
	}
}

// Notice this is now OUTSIDE the SplashScreen function:
@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
	SplitHappensTheme {
		SplashScreen()
	}
}