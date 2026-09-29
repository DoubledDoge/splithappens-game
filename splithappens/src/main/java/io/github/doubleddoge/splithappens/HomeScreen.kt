package io.github.doubleddoge.splithappens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

val DarkBackgroun = Color(0xFF0B1B15)
val CardBackground = Color(0xFF0F3A2E)
val BorderGold = Color(0xFFD4A311)
val ButtonBackground = Color(0xFFD8D8D8)
val ButtonTextColor = Color(0xFF1E1E1E)

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onPlayClick: () -> Unit ={}

){
val uiState by viewModel.uiState.collectAsState()
    var showStatsDialog by remember{ mutableStateOf(false) }
    var showRulesDialog by remember { mutableStateOf(false) }

}