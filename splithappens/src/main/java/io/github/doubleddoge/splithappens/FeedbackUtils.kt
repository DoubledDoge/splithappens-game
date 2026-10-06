package io.github.doubleddoge.splithappens

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

@SuppressLint("MissingPermission")
@Suppress("UNUSED_ANONYMOUS_PARAMETER", "UNUSED_VARIABLE", "unused")
fun triggerDeviceVibration(context: Context, durationMs: Long = 30) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.let { manager ->
                val effect = VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                manager.vibrate(CombinedVibration.createParallel(effect))
            }
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            @Suppress("DEPRECATION")
            vibrator?.vibrate(
                VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        }
    } catch (_e: Exception) {
        // Safe fallback if device lacks vibrator hardware or permission
    }
}

@Suppress("unused")
fun triggerClickSound() {
    // Sound playback hook
}