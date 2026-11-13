package io.github.doubleddoge.splithappens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.graphics.toArgb
import com.google.android.gms.ads.MobileAds
import io.github.doubleddoge.pulsemonitor.ui.ProfilerOverlay
import io.github.doubleddoge.splithappens.ui.navigation.AppNavigation
import io.github.doubleddoge.splithappens.ui.theme.SplitHappensTheme
import io.github.doubleddoge.splithappens.ui.theme.WindowCanvasBackground
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		// Force window canvas to match deep casino green so route fades never show white
		window.decorView.setBackgroundColor(WindowCanvasBackground.toArgb())

		enableEdgeToEdge()

		CoroutineScope(Dispatchers.IO).launch { MobileAds.initialize(this@MainActivity) {} }
		setContent {
			SplitHappensTheme {
				AppNavigation()
			}
		}

		ProfilerOverlay.install(this)
	}
}
