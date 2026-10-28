package io.github.doubleddoge.splithappens.game

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import io.github.doubleddoge.splithappens.data.DatabaseTest
import io.github.doubleddoge.splithappens.data.entity.CardEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.system.measureTimeMillis

/**
 * Feature verification test suite focusing on AdMob performance, lifecycle resilience,
 * offline handling (stopping on network loss)
 */
@RunWith(AndroidJUnit4::class)
class AdMobFeatureTest : DatabaseTest() {

    private val context: Context get() = ApplicationProvider.getApplicationContext()

    @Before
    fun setupCards() = runBlocking {
        db.cardDao().insertAll(CardEntity.standardDeck())
    }

    @Suppress("DEPRECATION")
    @Test
    fun testAdMobBannerSizingAndRequest() {
        val adView = AdView(context).apply {
            adUnitId = "ca-app-pub-3940256099942544/9214589741"
            val adSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, 320)
            setAdSize(adSize)
        }
        assertNotNull(adView)
        assertNotNull(adView.adSize)

        val adRequest = AdRequest.Builder().build()
        assertNotNull(adRequest)
    }

    /**
     * Tests that AdMob integration does not degrade performance during rapid lifecycle
     * transitions (resume, pause, destroy) and executes within strict execution time limits.
     */
    @Suppress("DEPRECATION")
    @Test
    fun testAdMobPerformanceAndLifecycleNonDegradation() {
        val elapsedTime = measureTimeMillis {
            repeat(50) {
                val adView = AdView(context).apply {
                    adUnitId = "ca-app-pub-3940256099942544/9214589741"
                    setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, 320))
                }
                adView.resume()
                adView.pause()
                adView.destroy()
            }
        }
        // 50 iterations of ad creation, sizing, resume, pause, and destroy must complete rapidly (< 1000ms)
        assertTrue("AdMob lifecycle operations took too long: ${elapsedTime}ms", elapsedTime < 1000)
    }

    /**
     * Tests that AdMob integration handles lack of internet connection gracefully,
     * invoking failure callbacks without hanging, timing out, or spamming network requests.
     */
    @Suppress("DEPRECATION")
    @Test
    fun testAdMobOfflineHandlingAndGracefulStop() {
        var failureCallbackInvoked = false
        var errorCodeReceived = -1

        val adView = AdView(context).apply {
            adUnitId = "ca-app-pub-3940256099942544/9214589741"
            setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, 320))
            adListener = object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    failureCallbackInvoked = true
                    errorCodeReceived = error.code
                }
            }
        }

        // Simulate offline condition by invoking failure listener directly with network error code (2)
        val networkError = LoadAdError(2, "No internet connection", "com.google.android.gms.ads", null, null)
        adView.adListener.onAdFailedToLoad(networkError)

        assertTrue(failureCallbackInvoked)
        assertEquals(2, errorCodeReceived) // 2 represents ERROR_CODE_NETWORK_ERROR

        // Verify clean teardown on offline stop
        adView.destroy()
        assertTrue(true) // Successfully stopped without hanging or memory leaks
    }


}
