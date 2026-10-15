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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Gold = Color(0xFFD4A64A)
private val GoldDark = Color(0xFF9C7425)

@Composable
fun GameActionBar(
    modifier: Modifier = Modifier,
    isBetting: Boolean = false,
    canBet: Boolean = true,
    onChip: (Long) -> Unit = {},
    onClear: () -> Unit = {},
    onDeal: () -> Unit = {},
    onHit: () -> Unit = {},
    onStand: () -> Unit = {},
    onDouble: () -> Unit = {},
    doubleEnabled: Boolean = true //
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isBetting) {
            GameButton(
                label = "5",
                topColor = Gold,
                bottomColor = GoldDark,
                modifier = Modifier.weight(1f),
                onClick = { onChip(5) }
            )
            GameButton(
                label = "25",
                topColor = Gold,
                bottomColor = GoldDark,
                modifier = Modifier.weight(1f),
                onClick = { onChip(25) }
            )
            GameButton(
                label = "100",
                topColor = Gold,
                bottomColor = GoldDark,
                modifier = Modifier.weight(1f),
                onClick = { onChip(100) }
            )
            GameButton(
                label = "DEAL",
                topColor = Gold,
                bottomColor = GoldDark,
                modifier = Modifier.weight(1.4f),
                textColor = Color(0xFF1A1208),
                enabled = canBet,
                onClick = onDeal
            )
        } else {
            GameButton(
                label = "HIT",
                topColor = Gold,
                bottomColor = GoldDark,
                modifier = Modifier.weight(1f),
                onClick = onHit
            )
            GameButton(
                label = "STAND",
                topColor = Gold,
                bottomColor = GoldDark,
                modifier = Modifier.weight(1f),
                onClick = onStand
            )
            GameButton(
                label = "DOUBLE",
                topColor = Gold,
                bottomColor = GoldDark,
                modifier = Modifier.weight(1f),
                enabled = doubleEnabled,
                onClick = onDouble
            )
        }
    }
}

@Composable
private fun GameButton(
    label: String,
    topColor: Color,
    bottomColor: Color,
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
    height: Dp = 46.dp,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .height(height)
            .alpha(if (enabled) 1f else 0.35f)
            .shadow(if (enabled) 5.dp else 0.dp, shape)
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(listOf(topColor, bottomColor)),
                shape = shape
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF071B12, widthDp = 360)
@Composable
private fun ActionBarPreview() {
    GameActionBar()
}