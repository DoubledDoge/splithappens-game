package io.github.doubleddoge.splithappens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.doubleddoge.splithappens.BorderGold
import io.github.doubleddoge.splithappens.ui.theme.SplitHappensTheme

@Composable
fun UserHeaderCard(
    displayName: String,
    chipsOwned: Long,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .border(2.dp, BorderGold, RoundedCornerShape(16.dp))
            .background(
                color = Color(0xFF072C1E), // Dark green background
                shape = RoundedCornerShape(16.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile Picture / Avatar Icon Placeholder
        Box(
            modifier = Modifier
                .size(44.dp)
                .border(1.dp, BorderGold, RoundedCornerShape(24.dp))
                .background(Color(0xFF0F4D35), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = displayName.take(1).uppercase(),
                color = Color(0xFFFFD700),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Player details
        Column {
            Text(
                text = displayName,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
            Row(
                modifier = modifier
                    .background(
                        color = Color(0xFF041910),
                        shape = RoundedCornerShape(50.dp)

                    )
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            )
            {
                Text(
                    text = "🪙 R$chipsOwned",
                    color = Color(0xFFFFD700), // Gold chips color
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun UserHeaderCardPreview() {
    SplitHappensTheme(){
        Box(
            modifier = Modifier
                .background(Color(0xFF0B1B15)) // Dark background to match app theme
                .padding(16.dp)
        ) {
            UserHeaderCard(
                displayName = "Player 1",
                chipsOwned = 2500L
            )
        }
    }
}