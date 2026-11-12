package io.github.doubleddoge.splithappens.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

private const val TAG = "FeedbackUtils"
const val DEFAULT_VIBRATION_MILLIS = 35L

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