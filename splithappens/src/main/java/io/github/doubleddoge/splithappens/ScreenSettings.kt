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

@Composable
fun ScreenSettings(
    uiState: SettingUiState = SettingUiState(darkTheme = true),
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

    // Screen colors
    val currentBackground = if (uiState.darkTheme) DarkBackground else Color(0xFF144D38)
    val currentCardBg = if (uiState.darkTheme) CardBackground else Color(0xFF1C6349)
    val currentCardBorder = if (uiState.darkTheme) Color(0xFF134530) else Color(0xFF288160)
    val currentSubtitle = if (uiState.darkTheme) Color(0xFF8BA697) else Color(0xFFBCE0CF)
    val currentCategoryTitle = if (uiState.darkTheme) Color(0xFF789D8B) else Color(0xFFA5D6BD)
    val currentDivider = if (uiState.darkTheme) Color(0xFF133B29) else Color(0xFF247053)

    // Dynamic Switch Color Palette:
    // Dark ON: Rich deep gold track, dark thumb
    // Dark OFF: Deeper warm yellow track, pure white thumb
    val activeTrackColor = if (uiState.darkTheme) BorderGold else Color(0xFFFFD54F)
    val activeThumbColor = if (uiState.darkTheme) Color(0xFF0C2419) else Color.White
    val inactiveTrackColor = if (uiState.darkTheme) Color(0xFF435A50) else Color(0xFF1B4E3A)
    val inactiveThumbColor = if (uiState.darkTheme) Color(0xFF8BA697) else Color(0xFFBCE0CF)

    Scaffold(
        bottomBar = {
            AppBottomBar(
                currentRoute = "settings",
                isDarkTheme = uiState.darkTheme,
                onNavigateHome = onNavigateHome,
                onNavigateGame = onNavigateGame,
                onNavigateMenu = onNavigateMenu
            )
        },
        containerColor = currentBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onNavigateBack() },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(currentCardBg)
                        .border(1.dp, currentCardBorder, CircleShape)
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
            SectionLabel(text = "APPEARANCE & DISPLAY", color = currentCategoryTitle)
            SettingsSectionCard(cardBg = currentCardBg, cardBorder = currentCardBorder) {
                SettingItemRow(
                    title = "Dark Theme",
                    subtitle = if (uiState.darkTheme) "Deep casino midnight felt tone" else "Vibrant emerald casino felt tone",
                    subtitleColor = currentSubtitle,
                    checked = uiState.darkTheme,
                    onCheckedChange = onToggleDarkTheme,
                    activeTrack = activeTrackColor,
                    activeThumb = activeThumbColor,
                    inactiveTrack = inactiveTrackColor,
                    inactiveThumb = inactiveThumbColor
                )
                SettingItemDivider(dividerColor = currentDivider)
                SettingItemRow(
                    title = "Show Card Total",
                    subtitle = "Displays current numerical hand sum over cards",
                    subtitleColor = currentSubtitle,
                    checked = uiState.showCardTotal,
                    onCheckedChange = onToggleShowCardTotal,
                    activeTrack = activeTrackColor,
                    activeThumb = activeThumbColor,
                    inactiveTrack = inactiveTrackColor,
                    inactiveThumb = inactiveThumbColor
                )
                SettingItemDivider(dividerColor = currentDivider)
                SettingItemRow(
                    title = "Dealer Rule Banner",
                    subtitle = "Display table rules ('Dealer stands on 17') on felt",
                    subtitleColor = currentSubtitle,
                    checked = uiState.dealerRuleBanner,
                    onCheckedChange = onToggleDealerRuleBanner,
                    activeTrack = activeTrackColor,
                    activeThumb = activeThumbColor,
                    inactiveTrack = inactiveTrackColor,
                    inactiveThumb = inactiveThumbColor
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Audio & Feedback Section
            SectionLabel(text = "AUDIO & FEEDBACK", color = currentCategoryTitle)
            SettingsSectionCard(cardBg = currentCardBg, cardBorder = currentCardBorder) {
                SettingItemRow(
                    title = "Sound Effects",
                    subtitle = "Card deals, chip stacks, and win fanfare",
                    subtitleColor = currentSubtitle,
                    checked = uiState.soundEffects,
                    onCheckedChange = { enabled ->
                        onToggleSoundEffects(enabled)
                        if (enabled) {
                            runCatching { triggerClickSound() }
                        }
                    },
                    activeTrack = activeTrackColor,
                    activeThumb = activeThumbColor,
                    inactiveTrack = inactiveTrackColor,
                    inactiveThumb = inactiveThumbColor
                )
                SettingItemDivider(dividerColor = currentDivider)
                SettingItemRow(
                    title = "Haptic Vibration",
                    subtitle = "Physical pulses on hit, stand, double, and bust",
                    subtitleColor = currentSubtitle,
                    checked = uiState.hapticVibration,
                    onCheckedChange = { enabled ->
                        onToggleHapticVibration(enabled)
                        if (enabled) {
                            runCatching { triggerDeviceVibration(context, durationMillis = 40L) }
                        }
                    },
                    activeTrack = activeTrackColor,
                    activeThumb = activeThumbColor,
                    inactiveTrack = inactiveTrackColor,
                    inactiveThumb = inactiveThumbColor
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Gameplay Section
            SectionLabel(text = "GAMEPLAY & CONTROLS", color = currentCategoryTitle)
            SettingsSectionCard(cardBg = currentCardBg, cardBorder = currentCardBorder) {
                SettingItemRow(
                    title = "Fast Deal",
                    subtitle = "Accelerates card sliding and flip animations",
                    subtitleColor = currentSubtitle,
                    checked = uiState.fastDeal,
                    onCheckedChange = onToggleFastDeal,
                    activeTrack = activeTrackColor,
                    activeThumb = activeThumbColor,
                    inactiveTrack = inactiveTrackColor,
                    inactiveThumb = inactiveThumbColor
                )
                SettingItemDivider(dividerColor = currentDivider)
                SettingItemRow(
                    title = "Auto-Stand on 21",
                    subtitle = "Automatically pass turn when hand reaches 21",
                    subtitleColor = currentSubtitle,
                    checked = uiState.autoStandOn21,
                    onCheckedChange = onToggleAutoStandOn21,
                    activeTrack = activeTrackColor,
                    activeThumb = activeThumbColor,
                    inactiveTrack = inactiveTrackColor,
                    inactiveThumb = inactiveThumbColor
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String, color: Color) {
    Text(
        text = text,
        color = color,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsSectionCard(
    cardBg: Color,
    cardBorder: Color,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, cardBorder, RoundedCornerShape(14.dp))
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
    subtitleColor: Color,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    activeTrack: Color,
    activeThumb: Color,
    inactiveTrack: Color,
    inactiveThumb: Color
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
                color = subtitleColor,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = activeTrack,
                checkedThumbColor = activeThumb,
                checkedBorderColor = Color.Transparent,
                uncheckedTrackColor = inactiveTrack,
                uncheckedThumbColor = inactiveThumb,
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

@Composable
private fun SettingItemDivider(dividerColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(dividerColor)
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ScreenSettingsPreview() {
    SplitHappensTheme {
        ScreenSettings()
    }
}