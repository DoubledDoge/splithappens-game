package io.github.doubleddoge.splithappens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.doubleddoge.splithappens.BorderGold
import io.github.doubleddoge.splithappens.R
import io.github.doubleddoge.splithappens.ui.theme.SplitHappensTheme

private val ActiveGold = Color(0xFFFFD700)

@Composable
fun AppBottomBar(
    currentRoute: String,
    isDarkTheme: Boolean = true,
    onNavigateHome: () -> Unit,
    onNavigateGame: () -> Unit,
    onNavigateMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Dynamic BottomBar colors based on theme
    val barBackground = if (isDarkTheme) Color(0xFF072C1E) else Color(0xFF144D38)
    val barBorder = if (isDarkTheme) BorderGold else Color(0xFFE5B82C)
    val circleBackground = if (isDarkTheme) Color(0xFF041A12) else Color(0xFF0D3828)
    val circleBorder = if (isDarkTheme) Color(0xFF0D3D2B) else Color(0xFF23654B)
    val spadeColor = circleBackground

    Row(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .border(1.dp, barBorder, RoundedCornerShape(16.dp))
            .background(
                color = barBackground,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Home Button
        BottomBarCircleButton(
            onClick = onNavigateHome,
            backgroundColor = circleBackground,
            borderColor = circleBorder
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_home),
                contentDescription = "Home",
                tint = ActiveGold,
                modifier = Modifier.size(20.dp)
            )
        }

        // 2. Game Button (card with a spade cut out in the middle)
        BottomBarCircleButton(
            onClick = onNavigateGame,
            backgroundColor = circleBackground,
            borderColor = circleBorder
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_blackjackicon),
                    contentDescription = "Game",
                    tint = ActiveGold,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "♠",
                    color = spadeColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.offset(x = 0.dp, y = 1.dp)
                )
            }
        }

        // 3. Menu Button
        BottomBarCircleButton(
            onClick = onNavigateMenu,
            backgroundColor = circleBackground,
            borderColor = circleBorder
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu",
                tint = ActiveGold,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun BottomBarCircleButton(
    onClick: () -> Unit,
    backgroundColor: Color,
    borderColor: Color,
    content: @Composable () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(1.dp, borderColor, CircleShape)
    ) {
        content()
    }
}

@Preview(showBackground = true)
@Composable
fun AppBottomBarPreview() {
    SplitHappensTheme {
        Box(
            modifier = Modifier
                .background(Color(0xFF0B1B15))
                .padding(16.dp)
        ) {
            AppBottomBar(
                currentRoute = "settings",
                isDarkTheme = true,
                onNavigateHome = {},
                onNavigateGame = {},
                onNavigateMenu = {}
            )
        }
    }
}