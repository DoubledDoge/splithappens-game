package io.github.doubleddoge.splithappens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

// TODO: Integrate actual sound pool/audio asset playback when table audio assets are merged
fun triggerClickSound() {
    // TODO: SoundPool or MediaPlayer click sound implementation
}

fun triggerDeviceVibration(context: Context, durationMillis: Long = 35L) {
    try {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

        if (vibrator != null && vibrator.hasVibrator()) {
            val effect = VibrationEffect.createOneShot(
                durationMillis,
                VibrationEffect.DEFAULT_AMPLITUDE
            )
            vibrator.vibrate(effect)
        }
    } catch (e: Exception) {
        // Exception caught gracefully without crashing UI interactions
    }
}