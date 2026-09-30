package io.github.doubleddoge.splithappens

import android.R
import android.text.Layout
import android.widget.GridLayout
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                                color = BorderGold,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                                )
                        }
                        Spacer(modifier = Modifier.width(16.dp))

                        Column(verticalArrangement = Arrangement.Center) {
                            Text(
                                text = uiState.displayName,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF08261E), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 2.dp)
                            ){
                                Text(
                                    text = "\uD83E\uDE99 R${uiState.chipsOwned}",
                                    color = BorderGold,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                }
                Spacer(modifier = Modifier.height(16.dp))
                //Action Buttons(STATS & RULES)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    //STATS Button
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clickable{ showStatsDialog = true},
                        shape = RoundedCornerShape(8.dp),
                        color = CardBackground,
                        border = BorderStroke(1.dp, Color(0xFF1A5243))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "\uD83D\uDCCA",fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "STATS",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clickable{ showRulesDialog = true},
                        shape = RoundedCornerShape(8.dp),
                        color = CardBackground,
                        border = BorderStroke(1.dp, Color(0xFF1A5243))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically

                        ){
                            Text(text = "📖", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "RULES",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(1.dp, BorderGold, RoundedCornerShape(16.dp)),

                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(contentColor = CardBackground)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)

                    ) {
                        Text(
                            text = "Classic Table",
                            color = BorderGold,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text="Standard Rules • Minimum Bet R100 ",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onPlayClick,
                            modifier = Modifier
                                .fillMaxSize()
                                .height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ButtonBackground,
                                contentColor = ButtonTextColor
                            )

                        ){
                            Text(
                                text = "PLAY NOW",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }


                    }
                }
            }
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .height(60.dp)

            ) { }
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