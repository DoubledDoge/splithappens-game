package io.github.doubleddoge.splithappens.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import io.github.doubleddoge.splithappens.viewmodel.uistate.SettingsUiState

private const val TAG = "FeedbackUtils"
const val DEFAULT_VIBRATION_MILLIS = 35L

@Stable
class Feedback internal constructor(
	private val context: Context,
	private val soundEnabled: Boolean,
	private val hapticsEnabled: Boolean,
) {
	fun click(vibrationMillis: Long = DEFAULT_VIBRATION_MILLIS) {
		if (soundEnabled) triggerClickSound()
		if (hapticsEnabled) triggerDeviceVibration(context, vibrationMillis)
	}
}

@Composable
fun rememberFeedback(settings: SettingsUiState): Feedback {
	val context = LocalContext.current
	return remember(context, settings.soundEffects, settings.hapticVibration) {
		Feedback(context, settings.soundEffects, settings.hapticVibration)
	}
}

// TODO: Integrate actual sound pool/audio asset playback when table audio assets are merged
fun triggerClickSound() {
	// TODO: SoundPool or MediaPlayer click sound implementation
}

fun triggerDeviceVibration(context: Context, durationMillis: Long = DEFAULT_VIBRATION_MILLIS) {
	try {
		val vibrator = context.findVibrator()?.takeIf { it.hasVibrator() } ?: return
		vibrator.vibrate(
			VibrationEffect.createOneShot(durationMillis, VibrationEffect.DEFAULT_AMPLITUDE),
		)
	} catch (e: IllegalArgumentException) {
		Log.w(TAG, "Invalid vibration duration or amplitude", e)
	} catch (e: UnsupportedOperationException) {
		Log.w(TAG, "Vibration effect unsupported on this device", e)
	}
}

private fun Context.findVibrator(): Vibrator? =
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
		(getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
	} else {
		@Suppress("DEPRECATION")
		getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
	}