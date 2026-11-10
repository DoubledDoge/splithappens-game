package io.github.doubleddoge.splithappens.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


private val CasinoDarkColorScheme = darkColorScheme(
	primary = GoldFoil,
	background = DarkBackground,
	surface = CardBackground,
	onPrimary = Color.Black,
	onBackground = Color.White,
	onSurface = Color.White
)

private val CasinoLightColorScheme = lightColorScheme(
	primary = GoldFoil,
	background = LightBackground,
	surface = LightSurface,
	onPrimary = Color.Black,
	onBackground = Color.White,
	onSurface = Color.White
)

@Composable
fun SplitHappensTheme(
	darkTheme: Boolean = true,
	content: @Composable () -> Unit
) {
	val colorScheme = if (darkTheme) CasinoDarkColorScheme else CasinoLightColorScheme

	MaterialTheme(
		colorScheme = colorScheme,
		typography = Typography,
		content = content
	)
}