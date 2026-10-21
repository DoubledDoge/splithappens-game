package io.github.doubleddoge.splithappens.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.doubleddoge.splithappens.BorderGold
import io.github.doubleddoge.splithappens.ui.theme.SplitHappensTheme

// Dark Theme Colors
private val CardBgDark = Color(0xFF072C1E)
private val AvatarBgDark = Color(0xFF0F4D35)
private val ChipBadgeBgDark = Color(0xFF041910)

// Light Theme Colors
private val CardBgLight = Color(0xFF1B6349)
private val AvatarBgLight = Color(0xFF237D5D)
private val ChipBadgeBgLight = Color(0xFF124533)

private val ChipGold = Color(0xFFFFD700)

@Composable
fun UserHeaderCard(
    displayName: String,
    chipsOwned: Long,
    modifier: Modifier = Modifier,
    profilePictureBytes: ByteArray? = null,
    isDarkTheme: Boolean = true,
    onProfileClick: () -> Unit = {}
) {
    val cardBg = if (isDarkTheme) CardBgDark else CardBgLight
    val avatarBg = if (isDarkTheme) AvatarBgDark else AvatarBgLight
    val chipBadgeBg = if (isDarkTheme) ChipBadgeBgDark else ChipBadgeBgLight

    val bitmap = remember(profilePictureBytes) {
        profilePictureBytes?.let {
            BitmapFactory.decodeByteArray(it, 0, it.size)?.asImageBitmap()
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .border(2.dp, BorderGold, RoundedCornerShape(16.dp))
            .background(
                color = cardBg,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onProfileClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile Picture Avatar
        Box(
            modifier = Modifier
                .size(44.dp)
                .border(1.dp, BorderGold, CircleShape)
                .clip(CircleShape)
                .background(avatarBg),
            contentAlignment = Alignment.Center
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = "Profile Picture",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = displayName.take(1).uppercase(),
                    color = ChipGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Player details
        Column {
            Text(
                text = displayName.ifBlank { "..." },
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
            Row(
                modifier = Modifier
                    .background(
                        color = chipBadgeBg,
                        shape = RoundedCornerShape(50.dp)
                    )
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🪙 R$chipsOwned",
                    color = ChipGold,
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
    SplitHappensTheme {
        Box(
            modifier = Modifier
                .background(Color(0xFF0B1B15))
                .padding(16.dp)
        ) {
            UserHeaderCard(
                displayName = "Player 1",
                chipsOwned = 2500L,
                profilePictureBytes = null,
                isDarkTheme = true
            )
        }
    }
}
