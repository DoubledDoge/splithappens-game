package io.github.doubleddoge.splithappens

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
private val CasinoDangerRed = Color(0xFFE55353)

fun triggerDeviceVibration(context: Context, durationMs: Long = 40) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator.vibrate(
                VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(durationMs)
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
    playerName: String = "Player 1",
    chipsOwned: Long = 2500L,
    onNavigateBack: () -> Unit = {},
    onNavigateToRules: () -> Unit = {},
    onNavigateToStats: () -> Unit = {},
    onResetChips: () -> Unit = {}
) {
    val isPreview = LocalInspectionMode.current
    val context = LocalContext.current
    val prefs = remember(isPreview) {
        if (!isPreview) context.getSharedPreferences("SplitHappensSettings", Context.MODE_PRIVATE) else null
    }

    var soundEnabled by remember { mutableStateOf(prefs?.getBoolean("sound_effects", true) ?: true) }
    var hapticsEnabled by remember { mutableStateOf(prefs?.getBoolean("haptics_enabled", true) ?: true) }
    var fastDealEnabled by remember { mutableStateOf(prefs?.getBoolean("fast_deal", false) ?: false) }

    var showResetDialog by remember { mutableStateOf(false) }

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
                    colors = listOf(CasinoGlowCenter, CasinoBackgroundDark),
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

            // Room Database Player Profile
            Text(
                text = "PLAYER PROFILE",
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(52.dp),
                        shape = CircleShape,
                        color = CasinoGoldAccent.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, CasinoGoldAccent)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = "♠", color = CasinoGoldAccent, fontSize = 24.sp)
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = playerName,
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Bankroll: $chipsOwned chips",
                            color = CasinoGoldAccent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Audio & Preferences
            Text(
                text = "AUDIO & GAMEPLAY",
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
                        subtitle = "Card deals, chip sounds, and cues",
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
                        subtitle = "Device feedback during gameplay actions",
                        checked = hapticsEnabled,
                        onCheckedChange = {
                            hapticsEnabled = it
                            prefs?.edit()?.putBoolean("haptics_enabled", it)?.apply()
                            if (it) triggerDeviceVibration(context, 50)
                        }
                    )

                    HorizontalDivider(color = Color.White.copy(alpha = 0.06f))

                    SettingsSwitchRow(
                        title = "Fast Deal",
                        subtitle = "Accelerate round deal speed",
                        checked = fastDealEnabled,
                        onCheckedChange = {
                            fastDealEnabled = it
                            prefs?.edit()?.putBoolean("fast_deal", it)?.apply()
                            runFeedback(true)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Guides & Records
            Text(
                text = "GUIDES & RECORDS",
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
                    SettingsNavigationRow(
                        icon = "📖",
                        title = "How to Play / Rules",
                        subtitle = "Wildcard mode, actions & payouts",
                        onClick = {
                            runFeedback(true)
                            onNavigateToRules()
                        }
                    )

                    HorizontalDivider(color = Color.White.copy(alpha = 0.06f))

                    SettingsNavigationRow(
                        icon = "📊",
                        title = "Player Statistics",
                        subtitle = "Win rates, streaks, and net chips",
                        onClick = {
                            runFeedback(true)
                            onNavigateToStats()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Data Management
            Text(
                text = "DATA MANAGEMENT",
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            runFeedback(false)
                            showResetDialog = true
                        }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Reset Bankroll",
                            color = CasinoDangerRed,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Restore balance back to starting 2,500 chips",
                            color = CasinoMutedText,
                            fontSize = 12.sp
                        )
                    }
                    Text(text = "🪙", fontSize = 18.sp)
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

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            containerColor = CasinoGreenSurface,
            title = { Text(text = "Reset Bankroll?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text(text = "This will restore your chip balance back to 2,500 chips in your database.", color = CasinoMutedText) },
            confirmButton = {
                TextButton(onClick = {
                    onResetChips()
                    runFeedback(false)
                    showResetDialog = false
                }) {
                    Text("Confirm Reset", color = CasinoDangerRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
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
            Text(text = title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, color = CasinoMutedText, fontSize = 12.sp)
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

@Composable
private fun SettingsNavigationRow(
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(text = title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text(text = subtitle, color = CasinoMutedText, fontSize = 12.sp)
            }
        }
        Text(text = "›", color = CasinoMutedText, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SettingsScreenPreview() {
    SplitHappensTheme {
        SettingsScreen()
    }
}