package io.github.doubleddoge.splithappens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import io.github.doubleddoge.splithappens.components.AppBottomBar
import java.io.ByteArrayOutputStream
import java.util.Locale

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = true,
    onNavigateBack: () -> Unit = {},
    onNavigateHome: () -> Unit = {},
    onNavigateGame: () -> Unit = {},
    onNavigateMenu: () -> Unit = onNavigateBack
) {
    val uiState by viewModel.uiState.collectAsState()

    ProfileScreenContent(
        modifier = modifier,
        uiState = uiState,
        isDarkTheme = isDarkTheme,
        onNavigateBack = onNavigateBack,
        onUpdateDisplayName = { newName ->
            viewModel.updateDisplayName(newName)
        },
        onUpdateProfilePicture = { bytes ->
            viewModel.updateProfilePicture(bytes)
        },
        onNavigateHome = onNavigateHome,
        onNavigateGame = onNavigateGame,
        onNavigateMenu = onNavigateMenu
    )
}

@Composable
fun ProfileScreenContent(
    uiState: ProfileUiState,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = true,
    onNavigateBack: () -> Unit = {},
    onUpdateDisplayName: (String) -> Unit = {},
    onUpdateProfilePicture: (ByteArray) -> Unit = {},
    onNavigateHome: () -> Unit = {},
    onNavigateGame: () -> Unit = {},
    onNavigateMenu: () -> Unit = onNavigateBack
) {
    val context = LocalContext.current

    val currentBackground = if (isDarkTheme) DarkBackground else Color(0xFF144D38)
    val currentCardBg = if (isDarkTheme) CardBackground else Color(0xFF1C6349)
    val currentCardBorder = if (isDarkTheme) Color(0xFF134530) else Color(0xFF288160)

    var isEditingName by remember { mutableStateOf(false) }
    var tempName by remember { mutableStateOf(uiState.displayName) }

    LaunchedEffect(uiState.displayName) {
        if (!isEditingName) {
            tempName = uiState.displayName
        }
    }

    val imagePickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri ?: return@rememberLauncherForActivityResult

            try {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                val bitmap: Bitmap = ImageDecoder.decodeBitmap(source)

                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                onUpdateProfilePicture(outputStream.toByteArray())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            AppBottomBar(
                currentRoute = "profile",
                isDarkTheme = isDarkTheme,
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Centered Header (Top-left back button completely removed)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "PROFILE",
                    color = BorderGold,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp
                )
            }

            if (uiState.isLoading) {
                Spacer(modifier = Modifier.height(40.dp))
                CircularProgressIndicator(
                    color = BorderGold,
                    modifier = Modifier.size(32.dp)
                )
            } else {
                // Profile Avatar
                Box(
                    modifier = Modifier
                        .size(105.dp)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color(0xFF0F4D35))
                            .border(
                                width = 2.dp,
                                color = BorderGold,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.profilePictureBytes != null &&
                            uiState.profilePictureBytes.isNotEmpty()
                        ) {
                            AsyncImage(
                                model = uiState.profilePictureBytes,
                                contentDescription = "Profile Picture",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(
                                text = uiState.displayName.take(1).uppercase(),
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.Bold,
                                fontSize = 42.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(BorderGold)
                            .border(1.dp, Color.Black, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Display Name
                if (isEditingName) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        OutlinedTextField(
                            value = tempName,
                            onValueChange = { tempName = it },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BorderGold,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.35f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = BorderGold
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                val cleaned = tempName.trim()
                                if (cleaned.isNotEmpty()) {
                                    onUpdateDisplayName(cleaned)
                                    isEditingName = false
                                }
                            },
                            modifier = Modifier.height(50.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BorderGold,
                                contentColor = Color.Black
                            )
                        ) {
                            Text(
                                text = "SAVE",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                tempName = uiState.displayName
                                isEditingName = true
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = uiState.displayName,
                            color = Color.White,
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "✏️", fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Chip Balance
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = Color(0xFF132A21),
                    border = BorderStroke(1.dp, BorderGold.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🪙", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "R${uiState.chipsOwned} CHIPS",
                            color = BorderGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Career Overview Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = currentCardBorder,
                            shape = RoundedCornerShape(14.dp)
                        ),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = currentCardBg)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "CAREER OVERVIEW",
                                    color = BorderGold,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "YOUR BLACKJACK PERFORMANCE",
                                    color = Color.White.copy(alpha = 0.45f),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium,
                                    letterSpacing = 1.2.sp
                                )
                            }
                            Text(
                                text = "♛",
                                color = BorderGold.copy(alpha = 0.7f),
                                fontSize = 24.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(BorderGold.copy(alpha = 0.25f))
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ProfileStatCard(
                                title = "GAMES",
                                value = uiState.gamesPlayed.toString(),
                                modifier = Modifier.weight(1f)
                            )
                            ProfileStatCard(
                                title = "WINS",
                                value = uiState.wins.toString(),
                                modifier = Modifier.weight(1f)
                            )
                            ProfileStatCard(
                                title = "LOSSES",
                                value = uiState.losses.toString(),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black.copy(alpha = 0.18f))
                                .border(
                                    width = 1.dp,
                                    color = BorderGold.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "WIN RATE",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = String.format(Locale.US, "%.1f%%", uiState.winRate),
                                    color = BorderGold,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            val performanceText = when {
                                uiState.gamesPlayed == 0 -> "NO GAMES"
                                uiState.winRate >= 60 -> "EXCELLENT"
                                uiState.winRate >= 50 -> "GOOD FORM"
                                else -> "KEEP PLAYING"
                            }

                            Text(
                                text = performanceText,
                                color = BorderGold,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileStatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.18f))
            .border(
                width = 1.dp,
                color = BorderGold.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(vertical = 12.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = value,
            color = BorderGold,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}