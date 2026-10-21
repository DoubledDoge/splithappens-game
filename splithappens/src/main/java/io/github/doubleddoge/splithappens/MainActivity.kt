package io.github.doubleddoge.splithappens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.graphics.toColorInt
import io.github.doubleddoge.splithappens.navigation.AppNavigation
import io.github.doubleddoge.splithappens.ui.theme.SplitHappensTheme

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		// Force window canvas to match deep casino green so route fades never show white
		window.decorView.setBackgroundColor("#071B12".toColorInt())

		enableEdgeToEdge()

		setContent {
			SplitHappensTheme {
				AppNavigation()
			}
		}
	}
}