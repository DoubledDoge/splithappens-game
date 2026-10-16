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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.doubleddoge.splithappens.components.AppBottomBar
import io.github.doubleddoge.splithappens.ui.theme.SplitHappensTheme

// Dark Theme Palettes
private val SettingsBgDarkCenter = Color(0xFF0F3022)
private val SettingsBgDarkOuter = Color(0xFF071B12)
private val SettingsSurfaceDark = Color(0xFF133827)
private val SettingsBorderDark = Color(0xFF1F4E38)
private val SubtitleColorDark = Color(0xFF8BA697)
private val DividerColorDark = Color(0xFF133B29)

// Light Theme Palettes
private val SettingsBgLightCenter = Color(0xFF1B634A)
private val SettingsBgLightOuter = Color(0xFF144D38)
private val SettingsSurfaceLight = Color(0xFF1D5A43)
private val SettingsBorderLight = Color(0xFF288160)
private val SubtitleColorLight = Color(0xFFBCE0CF)
private val DividerColorLight = Color(0xFF236B50)

private val GoldAccent = Color(0xFFF3B438)
private val CategoryTitleColor = Color(0xFFE5B036)

private val SwitchOnTrack = Color(0xFFF3B438)
private val SwitchOnThumb = Color(0xFF0C2419)
private val SwitchOffTrack = Color(0xFF435A50)
private val SwitchOffThumb = Color(0xFFFFFFFF)

@Composable
fun ScreenSettings(
    uiState: SettingUiState = SettingUiState(),
    isDarkTheme: Boolean = uiState.darkTheme,
    onToggleDarkTheme: (Boolean) -> Unit = {},
    onToggleShowCardTotal: (Boolean) -> Unit = {},
    onToggleDealerRuleBanner: (Boolean) -> Unit = {},
    onToggleSoundEffects: (Boolean) -> Unit = {},
    onToggleHapticVibration: (Boolean) -> Unit = {},
    onToggleFastDeal: (Boolean) -> Unit = {},
    onToggleAutoStandOn21: (Boolean) -> Unit = {},
    onNavigateHome: () -> Unit = {},
    onNavigateGame: () -> Unit = {},
    onNavigateMenu: () -> Unit = {}
) {
    val bgCenter = if (isDarkTheme) SettingsBgDarkCenter else SettingsBgLightCenter
    val bgOuter = if (isDarkTheme) SettingsBgDarkOuter else SettingsBgLightOuter
    val surfaceColor = if (isDarkTheme) SettingsSurfaceDark else SettingsSurfaceLight
    val borderColor = if (isDarkTheme) SettingsBorderDark else SettingsBorderLight
    val subtitleColor = if (isDarkTheme) SubtitleColorDark else SubtitleColorLight
    val dividerColor = if (isDarkTheme) DividerColorDark else DividerColorLight

    Scaffold(
        bottomBar = {
            AppBottomBar(
                currentRoute = "menu",
                isDarkTheme = isDarkTheme,
                onNavigateHome = onNavigateHome,
                onNavigateGame = onNavigateGame,
                onNavigateMenu = onNavigateMenu
            )
        },
        containerColor = bgOuter
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(bgCenter, bgOuter),
                        radius = 1200f
                    )
                )
                .padding(horizontal = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .statusBarsPadding()
                    .padding(top = 20.dp, bottom = 24.dp)
            ) {
                // Header (Back button removed, centered title)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "SETTINGS",
                        color = GoldAccent,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 3.sp
                    )
                }

                SectionLabel(text = "APPEARANCE & DISPLAY")
                SettingsSectionCard(surfaceColor = surfaceColor, borderColor = borderColor) {
                    SettingItemRow(
                        title = "Dark Theme",
                        subtitle = "Deep casino midnight table tone",
                        subtitleColor = subtitleColor,
                        checked = isDarkTheme,
                        onCheckedChange = onToggleDarkTheme
                    )
                    SettingItemDivider(dividerColor)
                    SettingItemRow(
                        title = "Show Card Total",
                        subtitle = "Displays current numerical hand sum over cards",
                        subtitleColor = subtitleColor,
                        checked = uiState.showCardTotal,
                        onCheckedChange = onToggleShowCardTotal
                    )
                    SettingItemDivider(dividerColor)
                    SettingItemRow(
                        title = "Dealer Rule Banner",
                        subtitle = "Display table rules ('Dealer stands on 17') on felt",
                        subtitleColor = subtitleColor,
                        checked = uiState.dealerRuleBanner,
                        onCheckedChange = onToggleDealerRuleBanner
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                SectionLabel(text = "AUDIO & FEEDBACK")
                SettingsSectionCard(surfaceColor = surfaceColor, borderColor = borderColor) {
                    SettingItemRow(
                        title = "Sound Effects",
                        subtitle = "Card deals, chip stacks, and win fanfare",
                        subtitleColor = subtitleColor,
                        checked = uiState.soundEffects,
                        onCheckedChange = onToggleSoundEffects
                    )
                    SettingItemDivider(dividerColor)
                    SettingItemRow(
                        title = "Haptic Vibration",
                        subtitle = "Physical pulses on hit, stand, double, and bust",
                        subtitleColor = subtitleColor,
                        checked = uiState.hapticVibration,
                        onCheckedChange = onToggleHapticVibration
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                SectionLabel(text = "GAMEPLAY & CONTROLS")
                SettingsSectionCard(surfaceColor = surfaceColor, borderColor = borderColor) {
                    SettingItemRow(
                        title = "Fast Deal",
                        subtitle = "Accelerates card sliding and flip animations",
                        subtitleColor = subtitleColor,
                        checked = uiState.fastDeal,
                        onCheckedChange = onToggleFastDeal
                    )
                    SettingItemDivider(dividerColor)
                    SettingItemRow(
                        title = "Auto-Stand on 21",
                        subtitle = "Automatically pass turn when hand reaches 21",
                        subtitleColor = subtitleColor,
                        checked = uiState.autoStandOn21,
                        onCheckedChange = onToggleAutoStandOn21
                    )
                }

                Spacer(modifier = Modifier.height(36.dp))

                Text(
                    text = "Split Happens v1.0.0 • Double Doge",
                    color = subtitleColor.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
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
private fun SettingsSectionCard(
    surfaceColor: Color,
    borderColor: Color,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
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
    subtitleColor: Color,
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
                color = subtitleColor,
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
        ScreenSettings(isDarkTheme = true)
    }
}