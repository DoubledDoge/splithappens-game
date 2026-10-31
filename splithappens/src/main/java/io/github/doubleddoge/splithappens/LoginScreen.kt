package io.github.doubleddoge.splithappens

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import io.github.doubleddoge.splithappens.data.entity.UserEntity
import io.github.doubleddoge.splithappens.ui.theme.SplitHappensTheme

// Dark Theme Palette
private val LoginBgDarkCenter = Color(0xFF0F3022)
private val LoginBgDarkOuter = Color(0xFF071B12)
private val LoginCardSurfaceDark = Color(0xFF133827)

// Light Theme Palette
private val LoginBgLightCenter = Color(0xFF1B634A)
private val LoginBgLightOuter = Color(0xFF144D38)
private val LoginCardSurfaceLight = Color(0xFF1D5A43)

private val GoldFoil = Color(0xFFE5B036)
private val DangerRed = Color(0xFFFF5252)
private val PlayingCardBg = Color(0xFFF9F9F9)
private val CheckMark = Color(0xFF140E06)
private val LoginButton = Color(0xFF1A1208)
private val ItemCard = Color(0xFF0F4D35)

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    isDarkTheme: Boolean = true,
    onLoginSuccess: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsState()

  LoginScreenContent(
      uiState = uiState,
      isDarkTheme = isDarkTheme,
      onUsernameChange = viewModel::onUsernameChange,
      onRememberMeChange = viewModel::onRememberMeChange,
      onLogin = { viewModel.login { onLoginSuccess() } },
      onOpenProfilesDialog = viewModel::openSwitchProfileDialog,
      onCloseProfilesDialog = viewModel::closeSwitchProfileDialog,
      onSelectProfile = viewModel::selectProfile,
      onPromptDeleteUser = viewModel::promptDeleteUser,
      onConfirmDeleteUser = viewModel::confirmDeleteUser,
      onCancelDeleteUser = viewModel::cancelDeleteUser,
  )
}

@Composable
fun LoginScreenContent(
    uiState: LoginUiState,
    isDarkTheme: Boolean = true,
    onUsernameChange: (String) -> Unit,
    onRememberMeChange: (Boolean) -> Unit,
    onLogin: () -> Unit,
    onOpenProfilesDialog: () -> Unit,
    onCloseProfilesDialog: () -> Unit,
    onSelectProfile: (UserEntity) -> Unit,
    onPromptDeleteUser: (UserEntity) -> Unit,
    onConfirmDeleteUser: () -> Unit,
    onCancelDeleteUser: () -> Unit,
) {
  val bgCenter = if (isDarkTheme) LoginBgDarkCenter else LoginBgLightCenter
  val bgOuter = if (isDarkTheme) LoginBgDarkOuter else LoginBgLightOuter
  val cardSurface = if (isDarkTheme) LoginCardSurfaceDark else LoginCardSurfaceLight

  Box(
      modifier =
          Modifier.fillMaxSize()
              .background(
                  brush =
                      Brush.radialGradient(
                          colors = listOf(bgCenter, bgOuter),
                          radius = 1200f,
                      )
              )
              .statusBarsPadding()
              .padding(horizontal = 24.dp),
      contentAlignment = Alignment.Center,
  ) {
    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
      // Ace of Spades Card Emblem
      Surface(
          modifier = Modifier.size(width = 90.dp, height = 124.dp),
          shape = RoundedCornerShape(12.dp),
          color = PlayingCardBg,
          shadowElevation = 8.dp,
          border = BorderStroke(1.dp, GoldFoil.copy(alpha = 0.6f)),
      ) {
        Box(modifier = Modifier.fillMaxSize().padding(8.dp)) {
          Text(
              text = "A",
              color = Color.Black,
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.align(Alignment.TopStart),
          )
          Text(
              text = "♠",
              color = Color.Black,
              fontSize = 44.sp,
              modifier = Modifier.align(Alignment.Center),
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      Text(
          text = "SPLIT HAPPENS",
          color = GoldFoil,
          fontSize = 26.sp,
          fontWeight = FontWeight.Black,
          letterSpacing = 3.sp,
      )

      Text(
          text = "ENTER THE CASINO",
          color = Color.White.copy(alpha = 0.65f),
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          letterSpacing = 2.sp,
          modifier = Modifier.padding(top = 4.dp, bottom = 24.dp),
      )

      // Login Card Form
      Card(
          modifier = Modifier.fillMaxWidth().shadow(12.dp, RoundedCornerShape(20.dp)),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = cardSurface),
          border = BorderStroke(1.dp, GoldFoil.copy(alpha = 0.45f)),
      ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Text(
              text = "PLAYER SIGN IN",
              color = Color.White,
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.2.sp,
          )

          Spacer(modifier = Modifier.height(20.dp))

          OutlinedTextField(
              value = uiState.username,
              onValueChange = { if (it.length <= 20) onUsernameChange(it) },
              label = {
                Text("Player Handle / Name (max 20)", color = Color.White.copy(alpha = 0.7f))
              },
              singleLine = true,
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp),
              colors =
                  OutlinedTextFieldDefaults.colors(
                      focusedBorderColor = GoldFoil,
                      unfocusedBorderColor = Color.White.copy(alpha = 0.25f),
                      focusedTextColor = Color.White,
                      unfocusedTextColor = Color.White,
                      cursorColor = GoldFoil,
                      focusedLabelColor = GoldFoil,
                      unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                  ),
          )

          if (uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage,
                color = DangerRed,
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, start = 4.dp),
                textAlign = TextAlign.Start,
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Remember Me Toggle
          Row(
              modifier =
                  Modifier.fillMaxWidth().clickable { onRememberMeChange(!uiState.rememberMe) },
              verticalAlignment = Alignment.CenterVertically,
          ) {
            Checkbox(
                checked = uiState.rememberMe,
                onCheckedChange = onRememberMeChange,
                colors =
                    CheckboxDefaults.colors(
                        checkedColor = GoldFoil,
                        checkmarkColor = CheckMark,
                        uncheckedColor = Color.White.copy(alpha = 0.5f),
                    ),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Remember Me",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )
          }

          Spacer(modifier = Modifier.height(22.dp))

          // Primary Login Button
          Button(
              onClick = onLogin,
              enabled = !uiState.isLoading,
              modifier = Modifier.fillMaxWidth().height(50.dp),
              shape = RoundedCornerShape(12.dp),
              colors =
                  ButtonDefaults.buttonColors(
                      containerColor = GoldFoil,
                      contentColor = LoginButton,
                      disabledContainerColor = GoldFoil.copy(alpha = 0.4f),
                      disabledContentColor = LoginButton.copy(alpha = 0.4f),
                  ),
          ) {
            if (uiState.isLoading) {
              CircularProgressIndicator(
                  modifier = Modifier.size(24.dp),
                  color = LoginButton,
                  strokeWidth = 2.5.dp,
              )
            } else {
              Text(
                  text = "LOGIN",
                  fontWeight = FontWeight.Black,
                  fontSize = 15.sp,
                  letterSpacing = 2.sp,
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Load Existing Profile Button
          OutlinedButton(
              onClick = onOpenProfilesDialog,
              modifier = Modifier.fillMaxWidth().height(48.dp),
              shape = RoundedCornerShape(12.dp),
              border = BorderStroke(1.dp, GoldFoil.copy(alpha = 0.7f)),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldFoil),
          ) {
            Text(
                text = "LOAD EXISTING PROFILE",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                letterSpacing = 1.2.sp,
            )
          }
        }
      }
    }
  }

  // Existing Profiles Selection Dialog
  if (uiState.isSwitchProfileDialogOpen) {
    Dialog(onDismissRequest = onCloseProfilesDialog) {
      Surface(
          modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
          shape = RoundedCornerShape(20.dp),
          color = cardSurface,
          border = BorderStroke(1.dp, GoldFoil),
      ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
          Text(
              text = "SAVED PROFILES",
              color = GoldFoil,
              fontSize = 18.sp,
              fontWeight = FontWeight.Black,
              letterSpacing = 2.sp,
              modifier = Modifier.align(Alignment.CenterHorizontally),
          )

          Spacer(modifier = Modifier.height(6.dp))

          Text(
              text = "Select a profile to populate username",
              color = Color.White.copy(alpha = 0.7f),
              fontSize = 12.sp,
              modifier = Modifier.align(Alignment.CenterHorizontally),
          )

          Spacer(modifier = Modifier.height(16.dp))
          HorizontalDivider(color = GoldFoil.copy(alpha = 0.3f))
          Spacer(modifier = Modifier.height(12.dp))

          if (uiState.existingProfiles.isEmpty()) {
            Text(
                text = "No saved profiles found.",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 13.sp,
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                textAlign = TextAlign.Center,
            )
          } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              items(uiState.existingProfiles) { item ->
                ProfileItemCard(
                    item = item,
                    onSelect = { onSelectProfile(item.user) },
                    onDelete = { onPromptDeleteUser(item.user) },
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          Button(
              onClick = onCloseProfilesDialog,
              modifier = Modifier.fillMaxWidth().height(44.dp),
              shape = RoundedCornerShape(12.dp),
              colors =
                  ButtonDefaults.buttonColors(
                      containerColor = Color.White.copy(alpha = 0.12f),
                      contentColor = Color.White,
                  ),
          ) {
            Text("CANCEL", fontWeight = FontWeight.Bold, fontSize = 13.sp)
          }
        }
      }
    }
  }

  // Delete Confirmation Dialog
  if (uiState.userPendingDelete != null) {
    val userName = uiState.userPendingDelete.displayName
    AlertDialog(
        onDismissRequest = onCancelDeleteUser,
        containerColor = cardSurface,
        title = {
          Text(
              text = "Delete Profile?",
              color = Color.White,
              fontWeight = FontWeight.Bold,
          )
        },
        text = {
          Text(
              text =
                  "Are you sure you want to delete \"$userName\"? All chip balances and progress for this account will be permanently removed.",
              color = Color.White.copy(alpha = 0.8f),
              fontSize = 13.sp,
          )
        },
        confirmButton = {
          TextButton(onClick = onConfirmDeleteUser) {
            Text("DELETE", color = DangerRed, fontWeight = FontWeight.Bold)
          }
        },
        dismissButton = {
          TextButton(onClick = onCancelDeleteUser) {
            Text("CANCEL", color = Color.White)
          }
        },
    )
  }
}

@Composable
private fun ProfileItemCard(
    item: UserProfileItem,
    onSelect: () -> Unit,
    onDelete: () -> Unit,
) {
  Card(
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.25f)),
      border = BorderStroke(1.dp, GoldFoil.copy(alpha = 0.35f)),
      modifier = Modifier.fillMaxWidth().clickable { onSelect() },
  ) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
          modifier =
              Modifier.size(40.dp)
                  .clip(CircleShape)
                  .background(ItemCard)
                  .border(1.dp, GoldFoil, CircleShape),
          contentAlignment = Alignment.Center,
      ) {
        if (item.profilePictureBytes != null && item.profilePictureBytes.isNotEmpty()) {
          AsyncImage(
              model = item.profilePictureBytes,
              contentDescription = "Profile Picture",
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize(),
          )
        } else {
          Text(
              text = item.user.displayName.take(1).uppercase(),
              color = GoldFoil,
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp,
          )
        }
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
            text = item.user.displayName,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
        )
        Text(
            text = "🪙 R${item.user.chipsOwned}  •  Last used: ${item.lastUsedFormatted}",
            color = Color.White.copy(alpha = 0.65f),
            fontSize = 11.sp,
        )
      }

      Box(
          modifier =
              Modifier.size(36.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(DangerRed.copy(alpha = 0.15f))
                  .clickable { onDelete() },
          contentAlignment = Alignment.Center,
      ) {
        Text(
            text = "🗑",
            color = DangerRed,
            fontSize = 16.sp,
        )
      }
    }
  }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
  SplitHappensTheme {
    LoginScreenContent(
        uiState = LoginUiState(username = "Player 1", rememberMe = true),
        isDarkTheme = true,
        onUsernameChange = {},
        onRememberMeChange = {},
        onLogin = {},
        onOpenProfilesDialog = {},
        onCloseProfilesDialog = {},
        onSelectProfile = {},
        onPromptDeleteUser = {},
        onConfirmDeleteUser = {},
        onCancelDeleteUser = {},
    )
  }
}
