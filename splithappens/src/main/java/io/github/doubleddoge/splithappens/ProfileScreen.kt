package io.github.doubleddoge.splithappens

import android.R
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import java.io.ByteArrayOutputStream


@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateHome: () -> Unit = {},
    onNavigateSettings: () -> Unit = {},
    onNavigateGame: () -> Unit = {}

){
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val isEditingName by remember { mutableStateOf(false) }
    var tempName by remember { mutableStateOf(uiState.displayName) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val bitmap = if (Build.VERSION.SDK_INT < 28){
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, it)
            }else {
                val source = ImageDecoder.createSource(context.contentResolver, it )
                ImageDecoder.decodeBitmap(source)
            }
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val byteArray = outputStream.toByteArray()

            viewModel.updateProfilePicture(byteArray)

        }

    }
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
    ) {innerPadding ->
        Column (
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterVertically
        ){
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "PLAYER PROFILE",
                color = BorderGold,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )

            //Profile Picture
            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier
                    .size(110.dp)
                    .clickable{imagePickerLauncher.launch("image/*")}
            ){

            }
        }



    }
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