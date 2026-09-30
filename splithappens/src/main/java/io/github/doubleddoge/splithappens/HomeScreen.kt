package io.github.doubleddoge.splithappens

import android.text.Layout
import android.widget.GridLayout
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.doubleddoge.splithappens.ui.theme.SplitHappensTheme

val DarkBackgroun = Color(0xFF0B1B15)
val CardBackground = Color(0xFF0F3A2E)
val BorderGold = Color(0xFFD4A311)
val ButtonBackground = Color(0xFFD8D8D8)
val ButtonTextColor = Color(0xFF1E1E1E)

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onPlayClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    HomeScreenContent(
        uiState = uiState,
        onPlayClick = onPlayClick
    )
}
@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    onPlayClick: () -> Unit ={}

){

    var showStatsDialog by remember{ mutableStateOf(false) }
    var showRulesDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackgroun)
            .padding(16.dp)

    ){
        if (uiState.isLoading){
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = BorderGold
            )
        }else{
            Column(modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally

            ) {
                Spacer(modifier = Modifier.height(24.dp))

                Surface(modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = CardBackground,
                    border = BorderStroke(1.dp, Color(0xFF1A5243))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically

                    ) {
                        //Avatar Initial
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color.Transparent, CircleShape)
                                .border(2.dp, BorderGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ){
                            Text(
                                text = uiState.displayName.take(1).uppercase(),
                                }
                    }

                }


            }
        }
    }
}
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    SplitHappensTheme {
        HomeScreenContent(
            uiState = HomeUiState(
                displayName = "Player 1",
                chipsOwned = 2500L,
                isLoading = false
            )
        )
    }
}