package io.github.doubleddoge.splithappens.components

import android.app.ActionBar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room3.ForeignKey

private val Gold = Color(0xFFD4A64A)
private val GoldDark = Color(0xFF9C7425)

@Composable
fun GameActionBar(
    modifier: Modifier = Modifier,
    onBet: () -> Unit = {},
    onHit: () -> Unit = {},
    onStand: () -> Unit = {},
    onDouble: () -> Unit = {},
    betEnabled: Boolean = true,
    hitEnabled: Boolean = true,
    standEnabled: Boolean = true,
    doubleEnabled: Boolean = true
){
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(
                listOf(
                    Color(0xFF0A1F16), Color(0xFF050F0A)
                )
            ))
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        //Primary action: BET

        GameButton(
            label = "BET",
            topColor = Gold,
            bottomColor = GoldDark,
            textColor = Color(0xFF1A1208),
            height = 50.dp,
            enabled = betEnabled,
            onClick = onBet,
            modifier = Modifier.fillMaxWidth()
        )

        //Row actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GameButton(
                label = "HIT",
                topColor = Gold,
                bottomColor = GoldDark,
                textColor = Color(0xFF1A1208),
                enabled = hitEnabled,
                onClick = onHit,
                modifier = Modifier.weight(1f)

            )

            GameButton(
                label = "STAND",
                topColor = Gold,
                bottomColor = GoldDark,
                textColor = Color(0xFF1A1208),
                enabled = standEnabled,
                onClick = onStand,
                modifier = Modifier.weight(1f)
            )
            GameButton(
                label = "DOUBLE",
                topColor = Gold,
                bottomColor = GoldDark,
                textColor = Color(0xFF1A1208),
                enabled = doubleEnabled,
                onClick = onDouble,
                modifier = Modifier.weight(1f)
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
    height: androidx.compose.ui.unit.Dp = 46.dp,
    enabled: Boolean = true,
    onClick: () -> Unit
){
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .height(height)
            .alpha(if (enabled) 1f else 0.35f)
            .shadow(if(enabled) 5.dp else 0.dp, shape)
            .clip(shape)
            .background(Brush.verticalGradient(
                listOf(topColor,bottomColor)
            ),
                shape
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
