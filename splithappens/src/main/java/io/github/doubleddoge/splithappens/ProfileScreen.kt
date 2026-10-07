package io.github.doubleddoge.splithappens

import android.R
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.media.quality.PictureProfile
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.doubleddoge.splithappens.components.AppBottomBar
import io.github.doubleddoge.splithappens.ui.theme.SplitHappensTheme
import org.jetbrains.annotations.Async
import java.io.ByteArrayOutputStream
import java.util.Locale
import kotlin.contracts.contract

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateHome: () -> Unit = {},
    onNavigateGame: () -> Unit = {},
    onNavigateSettings: () -> Unit = {}
) {

    val uiState by viewModel.uiState.collectAsState()

    ProfileScreenContent(
        uiState = uiState,

        onUpdateDisplayName = { newName ->
            viewModel.updateDisplayName(newName)
        },

        onUpdateProfilePicture = { bytes ->
            viewModel.updateProfilePicture(bytes)
        },

        onNavigateHome = onNavigateHome,
        onNavigateGame = onNavigateGame,
        onNavigateSettings = onNavigateSettings
    )
}
@Composable
fun ProfileScreenContent(
    uiState: ProfileUiState,

    onUpdateDisplayName: (String) -> Unit = {},
    onUpdateProfilePicture: (ByteArray) -> Unit = {},

    onNavigateHome: () -> Unit ={},
    onNavigateGame: () -> Unit ={},
    onNavigateSettings: () -> Unit
){
    val context = LocalContext.current

    var isEditingName by remember {
        mutableStateOf(false)
    }

    var tempName by remember {
        mutableStateOf(uiState.displayName)
    }
    LaunchedEffect(uiState.displayName) {
        if (!isEditingName){
            tempName = uiState.displayName
        }
    }
    // IMAGE PICKER

    val imagePickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->

            uri ?: return@rememberLauncherForActivityResult

            try {
                val bitmap: Bitmap =

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        val source =
                            ImageDecoder.createSource(
                                context.contentResolver,
                                uri
                            )
                        ImageDecoder.decodeBitmap(source)
                    } else {
                        @Suppress("DEPRECATION")
                        MediaStore.Images.Media.getBitmap(
                            context.contentResolver,
                            uri
                        )
                    }
                 val outputStream =
                     ByteArrayOutputStream()

                bitmap.compress(
                    Bitmap.CompressFormat.JPEG,
                    80,
                    outputStream
                )
                onUpdateProfilePicture(
                    outputStream.toByteArray()
                )
            }catch (e: Exception) {
                e.printStackTrace()
            }
        }

    // SCREEN
    Scaffold(
        bottomBar = {
            AppBottomBar(
                currentRoute = "profile",
                onNavigateHome = onNavigateHome,
                onNavigateGame = onNavigateGame,
                onNavigateSettings = onNavigateSettings
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            //Title

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            Text(
                text = "Player Profile",
                color = BorderGold,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            //Loading

            if (uiState.isLoading){
                Spacer(
                    modifier = Modifier.height(30.dp)
                )

                CircularProgressIndicator(
                    color = BorderGold,
                    modifier = Modifier.size(32.dp)
                )
            }else{
                // PROFILE PICTURE

                Box(
                    modifier = Modifier
                        .size(105.dp)
                        .clickable{
                            imagePickerLauncher.launch("image/*")
                        },

                    contentAlignment = Alignment.BottomEnd
                ){
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(
                                Color(0xFF0F4D35)
                            )
                            .border(
                                width = 2.dp,
                                color = BorderGold,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ){
                        if(
                            uiState.profilePictureBytes != null &&
                            uiState.profilePictureBytes.isNotEmpty()
                        ){
                            AsyncImage(
                                model = uiState.profilePictureBytes,

                                contentDescription = "Profile Picture",

                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }else {
                            Text(
                                text = uiState.displayName.take(1).uppercase(),
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.Bold,
                                fontSize = 40.sp
                            )
                        }
                    }
                    //Camera Button

                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(BorderGold)
                            .border(1.dp,Color.Black,CircleShape),
                        contentAlignment = Alignment.Center
                    ){
                        Text( text = "+", fontSize = 13.sp)

                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // DISPLAY NAME

                if(isEditingName){
                    Row(
                        modifier = Modifier.fillMaxSize(),

                        verticalAlignment = Alignment.CenterVertically,

                        horizontalArrangement = Arrangement.Center
                    ) {
                        OutlinedTextField(
                            value = tempName,

                            onValueChange = {
                                tempName = it
                            },

                            singleLine = true,
                            
                            modifier = Modifier.weight(1f),

                            shape = RoundedCornerShape(10.dp),

                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BorderGold,
                                unfocusedBorderColor = Color.White.copy(
                                    alpha = 0.35f
                                ),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,

                                cursorColor = BorderGold,

                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                val cleanedName = tempName.trim()

                                if(cleanedName.isNotEmpty()){
                                    onUpdateDisplayName(
                                        cleanedName
                                    )
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

                }else{
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable{
                                tempName = uiState.displayName
                                isEditingName = true
                            }
                            .padding(
                                horizontal = 10.dp,
                                vertical = 5.dp
                            ),

                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = uiState.displayName,

                            color = Color.White,

                            fontSize = 21.sp,

                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.width(7.dp))

                        Text(text = "✏\uFE0F", fontSize = 14.sp)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Chip Balance
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = Color(0xFF132A21),
                    border = BorderStroke(
                        width = 1.dp,
                        color = BorderGold.copy(alpha = 0.6f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = 18.dp,
                            vertical = 8.dp
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🪙",
                            fontSize = 18.sp
                        )

                        Spacer(
                            modifier = Modifier.width(8.dp)
                        )

                        Text(
                            text = "R${uiState.chipsOwned} CHIPS",
                            color = BorderGold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                //Career Overview

                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(width = 1.dp, color = BorderGold.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = CardBackground
                    )

                ){
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "CAREER OVERVIEW",
                            color = BorderGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        ProfileStatRow(
                            title = "Total Game Played",
                            value = uiState.gamesPlayed.toString()
                        )
                        ProfileStatRow(
                            title = "Total Losses",
                            value = uiState.losses.toString()
                        )
                        ProfileStatRow(
                            title = "Win Rate",
                            value = String.format(Locale.US, "%.1f%%", uiState.winRate)
                        )

                    }
                }


            }
        }

    }
}

@Composable
private fun ProfileStatRow(
    title: String,
    value: String
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),

        verticalAlignment = Alignment.CenterVertically,

        horizontalArrangement = Arrangement.SpaceBetween
    ){
        Text(
            text = title,

            color = Color.White.copy(alpha = 0.72f),
            fontSize = 13.sp
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 14.sp,



            fontWeight = FontWeight.Bold
            )
    }
}
@Composable
fun AsyncImage(
    model: ByteArray?,
    contentDescription: String,
    contentScale: ContentScale,
    modifier: Modifier
) {
    TODO("Not yet implemented")
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfileScreenPreview() {
    // Dummy ProfileViewModel instance or Mock State
    val mockUiState = ProfileUiState(
        userId = "user_001",
        displayName = "Andriaan",
        chipsOwned = 5000L,
        profilePictureBytes = null, // Set to null to preview initial letter fallback
        gamesPlayed = 24,
        wins = 15,
        losses = 9,
        winRate = 62.5,
        isLoading = false
    )

    SplitHappensTheme() {
        ProfileScreenContent(
            uiState = mockUiState,
            onUpdateDisplayName = {},
            onUpdateProfilePicture = {},
            onNavigateHome = {},
            onNavigateGame = {},
            onNavigateSettings = {}
        )
    }
}