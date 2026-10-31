@file:Suppress("UsePropertyAccessSyntax")

package io.github.doubleddoge.splithappens.components

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

private const val AD_HEIGHT = 60

// this would be replaced by the organization Google AdMob ID.
private const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/9214589741"

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun BannerAd(modifier: Modifier = Modifier) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current

  val widthDp = LocalConfiguration.current.screenWidthDp

  // Rebuilt only if the width changes (rotation)
  val adView =
      remember(widthDp) {
        AdView(context).apply {
          adUnitId = TEST_BANNER_ID

          /*
          Could also decide to use
          AdSize.getLargeAnchoredAdaptiveBannerAdSize(context, widthDp)
          */
          setAdSize(AdSize.getInlineAdaptiveBannerAdSize(widthDp, AD_HEIGHT))

          adListener =
              object : AdListener() {
                override fun onAdLoaded() {
                  Log.d("BannerAd", "loaded")
                }

                // error checking for any local problems such as width.
                override fun onAdFailedToLoad(error: LoadAdError) {
                  Log.e("BannerAd", "failed: ${error.code} ${error.message}")
                }
              }
          loadAd(AdRequest.Builder().build())
        }
      }

  // Makes ad lifecycle disposable and repeatable, ensures ads can resume on pause
  DisposableEffect(adView, lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      when (event) {
        Lifecycle.Event.ON_RESUME -> adView.resume()
        Lifecycle.Event.ON_PAUSE -> adView.pause()
        else -> Unit
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)

    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
      adView.destroy()
    }
  }

  AndroidView(factory = { adView }, modifier = modifier.fillMaxWidth())
}
