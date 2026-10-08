package io.github.doubleddoge.splithappens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.doubleddoge.splithappens.components.AppBottomBar
import io.github.doubleddoge.splithappens.ui.theme.SplitHappensTheme

// Consistent Palette matching HomeScreen
private val SettingsBackground = DarkBackground
private val SectionCardBackground = CardBackground
private val SectionCardBorder = Color(0xFF134530)
private val SubtitleColor = Color(0xFF8BA697)
private val CategoryTitleColor = Color(0xFF789D8B)
private val DividerColor = Color(0xFF133B29)

private val SwitchOnTrack = BorderGold
private val SwitchOnThumb = Color(0xFF0C2419)
private val SwitchOffTrack = Color(0xFF435A50)
private val SwitchOffThumb = Color(0xFFFFFFFF)

@Composable
fun ScreenSettings(
    uiState: SettingUiState = SettingUiState(),
    onToggleDarkTheme: (Boolean) -> Unit = {},
    onToggleShowCardTotal: (Boolean) -> Unit = {},
    onToggleDealerRuleBanner: (Boolean) -> Unit = {},
    onToggleSoundEffects: (Boolean) -> Unit = {},
    onToggleHapticVibration: (Boolean) -> Unit = {},
    onToggleFastDeal: (Boolean) -> Unit = {},
    onToggleAutoStandOn21: (Boolean) -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onNavigateHome: () -> Unit = {},
    onNavigateGame: () -> Unit = {},
    onNavigateMenu: () -> Unit = onNavigateBack
) {
    val context = LocalContext.current

    Scaffold(
        bottomBar = {
            AppBottomBar(
                currentRoute = "settings",
                onNavigateHome = onNavigateHome,
                onNavigateGame = onNavigateGame,
                onNavigateMenu = onNavigateMenu
            )
        },
        containerColor = SettingsBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Top Bar / Header with aligned vector back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onNavigateBack() },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(SectionCardBackground)
                        .border(1.dp, SectionCardBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "SETTINGS",
                    color = BorderGold,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            }

            // Appearance Section
            SectionLabel(text = "APPEARANCE & DISPLAY")
            SettingsSectionCard {
                SettingItemRow(
                    title = "Dark Theme",
                    subtitle = "Deep casino midnight table tone",
                    checked = uiState.darkTheme,
                    onCheckedChange = onToggleDarkTheme
                )
                SettingItemDivider()
                SettingItemRow(
                    title = "Show Card Total",
                    subtitle = "Displays current numerical hand sum over cards",
                    checked = uiState.showCardTotal,
                    onCheckedChange = onToggleShowCardTotal
                )
                SettingItemDivider()
                SettingItemRow(
                    title = "Dealer Rule Banner",
                    subtitle = "Display table rules ('Dealer stands on 17') on felt",
                    checked = uiState.dealerRuleBanner,
                    onCheckedChange = onToggleDealerRuleBanner
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Audio & Feedback Section
            SectionLabel(text = "AUDIO & FEEDBACK")
            SettingsSectionCard {
                SettingItemRow(
                    title = "Sound Effects",
                    subtitle = "Card deals, chip stacks, and win fanfare",
                    checked = uiState.soundEffects,
                    onCheckedChange = { enabled ->
                        onToggleSoundEffects(enabled)
                        if (enabled) {
                            // Trigger sample card deal sound effect
                            triggerClickSound()
                        }
                    }
                )
                SettingItemDivider()
                SettingItemRow(
                    title = "Haptic Vibration",
                    subtitle = "Physical pulses on hit, stand, double, and bust",
                    checked = uiState.hapticVibration,
                    onCheckedChange = { enabled ->
                        onToggleHapticVibration(enabled)
                        if (enabled) {
                            // Trigger tactile feedback pulse
                            triggerDeviceVibration(context, durationMillis = 40L)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Gameplay Section
            SectionLabel(text = "GAMEPLAY & CONTROLS")
            SettingsSectionCard {
                SettingItemRow(
                    title = "Fast Deal",
                    subtitle = "Accelerates card sliding and flip animations",
                    checked = uiState.fastDeal,
                    onCheckedChange = onToggleFastDeal
                )
                SettingItemDivider()
                SettingItemRow(
                    title = "Auto-Stand on 21",
                    subtitle = "Automatically pass turn when hand reaches 21",
                    checked = uiState.autoStandOn21,
                    onCheckedChange = onToggleAutoStandOn21
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Split Happens v1.0.0 • Double Doge",
                color = SubtitleColor.copy(alpha = 0.5f),
                fontSize = 11.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        color = CategoryTitleColor,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsSectionCard(content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SectionCardBackground),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SectionCardBorder, RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) {
            content()
        }
    }
}

@Composable
private fun SettingItemRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = SubtitleColor,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = SwitchOnThumb,
                checkedTrackColor = SwitchOnTrack,
                checkedBorderColor = Color.Transparent,
                uncheckedThumbColor = SwitchOffThumb,
                uncheckedTrackColor = SwitchOffTrack,
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

@Composable
private fun SettingItemDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(DividerColor)
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ScreenSettingsPreview() {
    SplitHappensTheme {
        ScreenSettings()
    }
}