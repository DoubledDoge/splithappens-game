package io.github.doubleddoge.splithappens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.doubleddoge.splithappens.game.Action

private val Gold = Color(0xFFD4A64A)
private val GoldDark = Color(0xFF9C7425)
private val DealTextColor = Color(0xFF1A1208)
private val DefaultTextColor = Color.White
private const val PREVIEW_BACKGROUND_COLOR = 0xFF071B12L

private const val BUTTON_ENABLED_ALPHA = 1f
private const val BUTTON_DISABLED_ALPHA = 0.35f
private val BUTTON_SHADOW_ELEVATION = 5.dp
private val BUTTON_DISABLED_SHADOW = 0.dp

private val ACTION_BAR_HEIGHT = 64.dp
private val ACTION_BAR_VERTICAL_PADDING = 8.dp
private val BUTTON_SPACING = 8.dp

private val BUTTON_HEIGHT = 46.dp
private val BUTTON_CORNER_RADIUS = 12.dp
private val BUTTON_TEXT_HORIZONTAL_PADDING = 4.dp

private const val WEIGHT_NORMAL = 1f
private const val WEIGHT_DEAL = 1.4f

private const val CHIP_VALUE_LOW = 5L
private const val CHIP_VALUE_MEDIUM = 25L
private const val CHIP_VALUE_HIGH = 100L

private val BUTTON_FONT_SIZE = 13.sp
private val BUTTON_MIN_FONT_SIZE = 8.sp
private val BUTTON_LETTER_SPACING = 2.sp
private val FONT_SIZE_STEP = 0.5.sp

private const val PREVIEW_WIDTH_DP = 360

@Composable
fun GameActionBar(
    modifier: Modifier = Modifier,
    isBetting: Boolean = false,
    canBet: Boolean = true,
    availableChips: Long = 0L,
    legalActions: Set<Action> = emptySet(),
    onAction: (Action) -> Unit = {},
    onChip: (Long) -> Unit = {},
    onDeal: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(ACTION_BAR_HEIGHT)
            .padding(vertical = ACTION_BAR_VERTICAL_PADDING),
        horizontalArrangement = Arrangement.spacedBy(BUTTON_SPACING),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isBetting) {
            GameButton(
                label = CHIP_VALUE_LOW.toString(),
                topColor = Gold,
                bottomColor = GoldDark,
                modifier = Modifier.weight(WEIGHT_NORMAL),
                enabled = availableChips >= CHIP_VALUE_LOW,
                onClick = { onChip(CHIP_VALUE_LOW) },
            )
            GameButton(
                label = CHIP_VALUE_MEDIUM.toString(),
                topColor = Gold,
                bottomColor = GoldDark,
                modifier = Modifier.weight(WEIGHT_NORMAL),
                enabled = availableChips >= CHIP_VALUE_MEDIUM,
                onClick = { onChip(CHIP_VALUE_MEDIUM) },
            )
            GameButton(
                label = CHIP_VALUE_HIGH.toString(),
                topColor = Gold,
                bottomColor = GoldDark,
                modifier = Modifier.weight(WEIGHT_NORMAL),
                enabled = availableChips >= CHIP_VALUE_HIGH,
                onClick = { onChip(CHIP_VALUE_HIGH) },
            )
            GameButton(
                label = "DEAL",
                topColor = Gold,
                bottomColor = GoldDark,
                modifier = Modifier.weight(WEIGHT_DEAL),
                textColor = DealTextColor,
                enabled = canBet,
                onClick = onDeal,
            )
        } else {
            GameButton(
                label = "HIT",
                topColor = Gold,
                bottomColor = GoldDark,
                modifier = Modifier.weight(WEIGHT_NORMAL),
                enabled = Action.HIT in legalActions,
                onClick = { onAction(Action.HIT) },
            )
            GameButton(
                label = "STAND",
                topColor = Gold,
                bottomColor = GoldDark,
                modifier = Modifier.weight(WEIGHT_NORMAL),
                enabled = Action.STAND in legalActions,
                onClick = { onAction(Action.STAND) },
            )
            if (Action.DOUBLE in legalActions) {
                GameButton(
                    label = "DOUBLE",
                    topColor = Gold,
                    bottomColor = GoldDark,
                    modifier = Modifier.weight(WEIGHT_NORMAL),
                    enabled = availableChips >= 0L,
                    onClick = { onAction(Action.DOUBLE) },
                )
            }
            if (Action.SURRENDER in legalActions) {
                GameButton(
                    label = "SURRENDER",
                    topColor = Gold,
                    bottomColor = GoldDark,
                    modifier = Modifier.weight(WEIGHT_NORMAL),
                    onClick = { onAction(Action.SURRENDER) },
                )
            }
        }
    }
}

@Composable
private fun GameButton(
    label: String,
    topColor: Color,
    bottomColor: Color,
    modifier: Modifier = Modifier,
    textColor: Color = DefaultTextColor,
    height: Dp = BUTTON_HEIGHT,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(BUTTON_CORNER_RADIUS)
    val buttonAlpha = if (enabled) BUTTON_ENABLED_ALPHA else BUTTON_DISABLED_ALPHA
    val shadowElevation = if (enabled) BUTTON_SHADOW_ELEVATION else BUTTON_DISABLED_SHADOW
    Box(
        modifier = modifier
            .height(height)
            .alpha(buttonAlpha)
            .shadow(shadowElevation, shape)
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(listOf(topColor, bottomColor)),
                shape = shape,
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        AutoResizedText(
            text = label,
            color = textColor,
            modifier = Modifier.padding(horizontal = BUTTON_TEXT_HORIZONTAL_PADDING),
        )
    }
}

@Composable
private fun AutoResizedText(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    targetFontSize: TextUnit = BUTTON_FONT_SIZE,
    minFontSize: TextUnit = BUTTON_MIN_FONT_SIZE,
    letterSpacing: TextUnit = BUTTON_LETTER_SPACING,
    fontWeight: FontWeight = FontWeight.Bold,
) {
    var resizedFontSize by remember(text, targetFontSize) { mutableStateOf(targetFontSize) }
    var shouldDraw by remember(text, targetFontSize) { mutableStateOf(false) }

    Text(
        text = text,
        color = color,
        fontSize = resizedFontSize,
        fontWeight = fontWeight,
        letterSpacing = letterSpacing,
        maxLines = 1,
        softWrap = false,
        modifier = modifier.drawWithContent {
            if (shouldDraw) {
                drawContent()
            }
        },
        onTextLayout = { result ->
            if (result.didOverflowWidth && resizedFontSize > minFontSize) {
                val nextSize = (resizedFontSize.value - FONT_SIZE_STEP.value).sp
                resizedFontSize = if (nextSize < minFontSize) minFontSize else nextSize
            } else {
                shouldDraw = true
            }
        },
    )
}

@Preview(showBackground = true, backgroundColor = PREVIEW_BACKGROUND_COLOR, widthDp = PREVIEW_WIDTH_DP)
@Composable
private fun ActionBarBettingPreview() {
    GameActionBar(
        isBetting = true,
        canBet = true,
        availableChips = 100L,
    )
}

@Preview(showBackground = true, backgroundColor = PREVIEW_BACKGROUND_COLOR, widthDp = PREVIEW_WIDTH_DP)
@Composable
private fun ActionBarPlayingPreview() {
    GameActionBar(
        isBetting = false,
        legalActions = setOf(Action.HIT, Action.STAND, Action.DOUBLE, Action.SURRENDER),
    )
}

