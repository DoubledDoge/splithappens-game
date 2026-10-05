package io.github.doubleddoge.splithappens

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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.doubleddoge.splithappens.ui.theme.SplitHappensTheme

// Screenshot-Matched Colors
private val ScreenBackground = Color(0xFF072417)
private val CardBackground = Color(0xFF0C2F20)
private val CardBorderColor = Color(0xFF134530)
private val GoldAccent = Color(0xFFF3B438)
private val SubtitleColor = Color(0xFF8BA697)
private val CategoryTitleColor = Color(0xFF789D8B)
private val DividerColor = Color(0xFF133B29)

// Switch Colors
private val SwitchOnTrack = Color(0xFFF3B438)
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
    onNavigateBack: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground)
            .padding(horizontal = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 28.dp, bottom = 24.dp)
        ) {
            // Header: Circular back button + SETTINGS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 28.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(CardBackground)
                        .border(1.dp, CardBorderColor, CircleShape)
                        .clickable { onNavigateBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "←",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "SETTINGS",
                    color = GoldAccent,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            }

            // 1. APPEARANCE & DISPLAY
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

            Spacer(modifier = Modifier.height(24.dp))

            // 2. AUDIO & FEEDBACK
            SectionLabel(text = "AUDIO & FEEDBACK")
            SettingsSectionCard {
                SettingItemRow(
                    title = "Sound Effects",
                    subtitle = "Card deals, chip stacks, and win fanfare",
                    checked = uiState.soundEffects,
                    onCheckedChange = onToggleSoundEffects
                )
                SettingItemDivider()
                SettingItemRow(
                    title = "Haptic Vibration",
                    subtitle = "Physical pulses on hit, stand, double, and bust",
                    checked = uiState.hapticVibration,
                    onCheckedChange = onToggleHapticVibration
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. GAMEPLAY & CONTROLS
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

            Spacer(modifier = Modifier.height(36.dp))

            // Version Footer
            Text(
                text = "Split Happens v1.0.0 • Double Doge",
                color = SubtitleColor.copy(alpha = 0.5f),
                fontSize = 12.sp,
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
        modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
    )
}

@Composable
private fun SettingsSectionCard(content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
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
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = subtitle,
                color = SubtitleColor,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

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