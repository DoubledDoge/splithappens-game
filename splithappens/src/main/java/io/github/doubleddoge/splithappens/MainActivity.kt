package io.github.doubleddoge.splithappens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.github.doubleddoge.splithappens.navigation.AppNavigation
import io.github.doubleddoge.splithappens.ui.theme.SplitHappensTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.google.android.gms.ads.MobileAds
class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()

		CoroutineScope(Dispatchers.IO).launch { MobileAds.initialize(this@MainActivity) {} }
		setContent {
			SplitHappensTheme {
				AppNavigation()
			}
		}
	}
}