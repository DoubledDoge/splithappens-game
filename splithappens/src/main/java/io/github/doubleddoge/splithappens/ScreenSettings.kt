package io.github.doubleddoge.splithappens

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.doubleddoge.splithappens.ui.theme.SplitHappensTheme

// Casino Palette
private val CasinoBackgroundDark = Color(0xFF071B12)
private val CasinoGlowCenter = Color(0xFF0F3022)
private val CasinoGoldAccent = Color(0xFFE5B036)
private val CasinoGreenSurface = Color(0xFF133827)
private val CasinoMutedText = Color(0xFF9EBAAA)

@SuppressLint("MissingPermission")
fun triggerDeviceVibration(context: Context, durationMs: Long = 40) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator?.vibrate(
                VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            vibrator?.vibrate(durationMs)
        }
    } catch (_: Exception) {}
}

fun triggerClickSound() {
    try {
        val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 70)
        toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 35)
    } catch (_: Exception) {}
}

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit = {}
) {
    val isPreview = LocalInspectionMode.current
    val context = LocalContext.current
    val prefs = remember(isPreview) {
        if (!isPreview) context.getSharedPreferences("SplitHappensSettings", Context.MODE_PRIVATE) else null
    }

    // Appearance & Display
    var isDarkMode by remember { mutableStateOf(prefs?.getBoolean("dark_mode", true) ?: true) }
    var showCardTotals by remember { mutableStateOf(prefs?.getBoolean("show_card_totals", true) ?: true) }
    var showDealerRulesBanner by remember { mutableStateOf(prefs?.getBoolean("show_dealer_banner", true) ?: true) }

    // Audio & Feedback
    var soundEnabled by remember { mutableStateOf(prefs?.getBoolean("sound_effects", true) ?: true) }
    var hapticsEnabled by remember { mutableStateOf(prefs?.getBoolean("haptics_enabled", true) ?: true) }

    // Gameplay Convenience
    var fastDealEnabled by remember { mutableStateOf(prefs?.getBoolean("fast_deal", false) ?: false) }
    var autoStandOn21 by remember { mutableStateOf(prefs?.getBoolean("auto_stand_21", true) ?: true) }

    val runFeedback: (Boolean) -> Unit = { isClick ->
        if (!isPreview) {
            if (hapticsEnabled) triggerDeviceVibration(context, if (isClick) 35 else 60)
            if (soundEnabled) triggerClickSound()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = if (isDarkMode) {
                        listOf(CasinoGlowCenter, CasinoBackgroundDark)
                    } else {
                        listOf(Color(0xFF144D35), Color(0xFF0A291C))
                    },
                    radius = 1200f
                )
            )
            .padding(horizontal = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CasinoGreenSurface)
                        .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                        .clickable {
                            runFeedback(true)
                            onNavigateBack()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "←",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "SETTINGS",
                    color = CasinoGoldAccent,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            }

            // 1. APPEARANCE & DISPLAY
            Text(
                text = "APPEARANCE & DISPLAY",
                color = CasinoMutedText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CasinoGreenSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    SettingsSwitchRow(
                        title = "Dark Theme",
                        subtitle = "Deep casino midnight table tone",
                        checked = isDarkMode,
                        onCheckedChange = {
                            isDarkMode = it
                            prefs?.edit()?.putBoolean("dark_mode", it)?.apply()
                            runFeedback(true)
                        }
                    )

                    HorizontalDivider(color = Color.White.copy(alpha = 0.06f))

                    SettingsSwitchRow(
                        title = "Show Card Total",
                        subtitle = "Displays current numerical hand sum over cards",
                        checked = showCardTotals,
                        onCheckedChange = {
                            showCardTotals = it
                            prefs?.edit()?.putBoolean("show_card_totals", it)?.apply()
                            runFeedback(true)
                        }
                    )

                    HorizontalDivider(color = Color.White.copy(alpha = 0.06f))

                    SettingsSwitchRow(
                        title = "Dealer Rule Banner",
                        subtitle = "Display table rules ('Dealer stands on 17') on felt",
                        checked = showDealerRulesBanner,
                        onCheckedChange = {
                            showDealerRulesBanner = it
                            prefs?.edit()?.putBoolean("show_dealer_banner", it)?.apply()
                            runFeedback(true)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. AUDIO & FEEDBACK
            Text(
                text = "AUDIO & FEEDBACK",
                color = CasinoMutedText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CasinoGreenSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    SettingsSwitchRow(
                        title = "Sound Effects",
                        subtitle = "Card deals, chip stacks, and win fanfare",
                        checked = soundEnabled,
                        onCheckedChange = {
                            soundEnabled = it
                            prefs?.edit()?.putBoolean("sound_effects", it)?.apply()
                            if (it) runFeedback(true)
                        }
                    )

                    HorizontalDivider(color = Color.White.copy(alpha = 0.06f))

                    SettingsSwitchRow(
                        title = "Haptic Vibration",
                        subtitle = "Physical pulses on hit, stand, double, and bust",
                        checked = hapticsEnabled,
                        onCheckedChange = {
                            hapticsEnabled = it
                            prefs?.edit()?.putBoolean("haptics_enabled", it)?.apply()
                            if (it) triggerDeviceVibration(context, 50)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. GAMEPLAY
            Text(
                text = "GAMEPLAY & CONTROLS",
                color = CasinoMutedText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CasinoGreenSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    SettingsSwitchRow(
                        title = "Fast Deal",
                        subtitle = "Accelerates card sliding and flip animations",
                        checked = fastDealEnabled,
                        onCheckedChange = {
                            fastDealEnabled = it
                            prefs?.edit()?.putBoolean("fast_deal", it)?.apply()
                            runFeedback(true)
                        }
                    )

                    HorizontalDivider(color = Color.White.copy(alpha = 0.06f))

                    SettingsSwitchRow(
                        title = "Auto-Stand on 21",
                        subtitle = "Automatically pass turn when hand reaches 21",
                        checked = autoStandOn21,
                        onCheckedChange = {
                            autoStandOn21 = it
                            prefs?.edit()?.putBoolean("auto_stand_21", it)?.apply()
                            runFeedback(true)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Split Happens v1.0.0 • Double Doge",
                color = Color.White.copy(alpha = 0.3f),
                fontSize = 11.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = CasinoMutedText,
                fontSize = 12.sp
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = CasinoBackgroundDark,
                checkedTrackColor = CasinoGoldAccent,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color.White.copy(alpha = 0.2f)
            )
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SettingsScreenPreview() {
    SplitHappensTheme {
        SettingsScreen()
    }
}