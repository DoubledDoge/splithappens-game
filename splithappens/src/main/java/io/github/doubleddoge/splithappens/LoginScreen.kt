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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.doubleddoge.splithappens.ui.theme.SplitHappensTheme

// Dark Theme Palette
private val LoginBgDarkCenter = Color(0xFF0F3022)
private val LoginBgDarkOuter = Color(0xFF071B12)
private val LoginCardSurfaceDark = Color(0xFF133827)
private val EmblemBgDark = Color(0xFF0B2D21)

// Light Theme Palette
private val LoginBgLightCenter = Color(0xFF1B634A)
private val LoginBgLightOuter = Color(0xFF144D38)
private val LoginCardSurfaceLight = Color(0xFF1D5A43)
private val EmblemBgLight = Color(0xFF154E38)

private val GoldFoil = Color(0xFFE5B036)

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    isDarkTheme: Boolean = true,
    onLoginSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LoginScreenContent(
        uiState = uiState,
        isDarkTheme = isDarkTheme,
        onUsernameChange = viewModel::onUsernameChange,
        onRememberMeChange = viewModel::onRememberMeChange,
        onLogin = {
            viewModel.login {
                onLoginSuccess()
            }
        }
    )
}

@Composable
fun LoginScreenContent(
    uiState: LoginUiState,
    isDarkTheme: Boolean = true,
    onUsernameChange: (String) -> Unit,
    onRememberMeChange: (Boolean) -> Unit,
    onLogin: () -> Unit
) {
    val bgCenter = if (isDarkTheme) LoginBgDarkCenter else LoginBgLightCenter
    val bgOuter = if (isDarkTheme) LoginBgDarkOuter else LoginBgLightOuter
    val cardSurface = if (isDarkTheme) LoginCardSurfaceDark else LoginCardSurfaceLight
    val emblemBg = if (isDarkTheme) EmblemBgDark else EmblemBgLight

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(bgCenter, bgOuter),
                    radius = 1200f
                )
            )
            .statusBarsPadding()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Casino Emblem
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(emblemBg)
                    .border(2.dp, GoldFoil, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "♠",
                    color = GoldFoil,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "SPLIT HAPPENS",
                color = GoldFoil,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp
            )

            Text(
                text = "ENTER THE CASINO",
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 28.dp)
            )

            // Login Card Form
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardSurface),
                border = BorderStroke(1.dp, GoldFoil.copy(alpha = 0.45f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "PLAYER SIGN IN",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Player Handle / Name
                    OutlinedTextField(
                        value = uiState.username,
                        onValueChange = onUsernameChange,
                        label = { Text("Player Handle / Name", color = Color.White.copy(alpha = 0.7f)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldFoil,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.25f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = GoldFoil,
                            focusedLabelColor = GoldFoil,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.6f)
                        )
                    )

                    if (uiState.errorMessage != null) {
                        Text(
                            text = uiState.errorMessage,
                            color = Color(0xFFFF6B6B),
                            fontSize = 12.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, start = 4.dp),
                            textAlign = TextAlign.Start
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Remember Me Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onRememberMeChange(!uiState.rememberMe) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = uiState.rememberMe,
                            onCheckedChange = onRememberMeChange,
                            colors = CheckboxDefaults.colors(
                                checkedColor = GoldFoil,
                                checkmarkColor = Color(0xFF140E06),
                                uncheckedColor = Color.White.copy(alpha = 0.5f)
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Remember Me",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // Submit Button labeled "LOGIN"
                    Button(
                        onClick = onLogin,
                        enabled = !uiState.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldFoil,
                            contentColor = Color(0xFF1A1208),
                            disabledContainerColor = GoldFoil.copy(alpha = 0.4f),
                            disabledContentColor = Color(0xFF1A1208).copy(alpha = 0.4f)
                        )
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color(0xFF1A1208),
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                text = "LOGIN",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                letterSpacing = 2.sp
                            )
                        }
                    }
                }
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
            onLogin = {}
        )
    }
}