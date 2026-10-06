package io.github.doubleddoge.splithappens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.doubleddoge.splithappens.BorderGold
import io.github.doubleddoge.splithappens.R
import io.github.doubleddoge.splithappens.ui.theme.SplitHappensTheme

@Composable
fun AppBottomBar(
    currentRoute: String,
    onNavigateHome: () -> Unit,
    onNavigateStats: () -> Unit,
    onNavigateProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .border(1.dp, BorderGold , RoundedCornerShape(16.dp))
            .background(
                color = Color(0xFF072C1E),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(vertical = 4.dp),

        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateHome) {
            Icon(
                painter = painterResource(id = R.drawable.ic_home),
                contentDescription = "Home",
                tint = if (currentRoute == "home") Color(0xFFFFD700) else Color.Gray
            )
        }

        IconButton(onClick = onNavigateStats) {
            Icon(
                painter = painterResource(id = R.drawable.ic_blackjackicon),
                contentDescription = "Game",
                tint = Color.Unspecified
            )
        }

        IconButton(onClick = onNavigateProfile) {
            Icon(
              painter = painterResource(id = R.drawable.ic_settings),
                contentDescription = "Settings",
                tint = if (currentRoute == "settings") Color(0xFFFFD700) else Color.Gray,

            )
        }
    }
}
@Preview(showBackground = true)
@Composable
fun AppBottomBarPreview() {
    SplitHappensTheme() {
        Box(
            modifier = Modifier
                .background(Color(0xFF0B1B15)) // Dark background to match app theme
                .padding(16.dp)
        ) {
            AppBottomBar(
                currentRoute = "home",
                onNavigateHome = {},
                onNavigateStats = {},
                onNavigateProfile = {}
            )
        }
    }
}