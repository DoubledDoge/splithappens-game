package io.github.doubleddoge.splithappens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.doubleddoge.splithappens.BorderGold
import io.github.doubleddoge.splithappens.R
import io.github.doubleddoge.splithappens.ui.theme.SplitHappensTheme

private val ActiveGold = Color(0xFFFFD700)
private val CasinoDarkGreen = Color(0xFF072C1E)

// Circular badge styling
private val IconCircleBackground = Color(0xFF041A12)
private val IconCircleBorder = Color(0xFF0D3D2B)

@Composable
fun AppBottomBar(
    currentRoute: String,
    onNavigateHome: () -> Unit,
    onNavigateGame: () -> Unit,
    onNavigateMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .border(1.dp, BorderGold, RoundedCornerShape(16.dp))
            .background(
                color = CasinoDarkGreen,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Home Button (Always Yellow)
        IconButton(
            onClick = onNavigateHome,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(IconCircleBackground)
                .border(1.dp, IconCircleBorder, CircleShape)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_home),
                contentDescription = "Home",
                tint = ActiveGold,
                modifier = Modifier.size(20.dp)
            )
        }

        // 2. Game Button (Always Yellow)
        IconButton(
            onClick = onNavigateGame,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(IconCircleBackground)
                .border(1.dp, IconCircleBorder, CircleShape)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_blackjackicon),
                contentDescription = "Game",
                tint = ActiveGold,
                modifier = Modifier.size(20.dp)
            )
        }

        // 3. Menu Button (Always Yellow)
        IconButton(
            onClick = onNavigateMenu,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(IconCircleBackground)
                .border(1.dp, IconCircleBorder, CircleShape)
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
                onNavigateHome = {},
                onNavigateGame = {},
                onNavigateMenu = {}
            )
        }
    }
}