package io.github.doubleddoge.splithappens.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.doubleddoge.splithappens.TableCard
import io.github.doubleddoge.splithappens.isRed
import io.github.doubleddoge.splithappens.label
import io.github.doubleddoge.splithappens.symbol

private val GoldFoil = Color(0xFFE5B036)

@Composable
fun PlayingCard(
    card: TableCard,
    modifier: Modifier = Modifier,
    dealFrom: DpOffset = DpOffset(140.dp, (-80).dp)
) {
    val fly = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        fly.animateTo(1f, tween(450, easing = FastOutSlowInEasing))
    }
    val flip by animateFloatAsState(
        targetValue = if (card.isFaceUp) 0f else 180f,
        animationSpec = tween(400),
        label = "cardFlip"
    )

    Box(
        modifier
            .size(width = 64.dp, height = 92.dp)
            .graphicsLayer {
                val t = fly.value
                translationX = (1f - t) * dealFrom.x.toPx()
                translationY = (1f - t) * dealFrom.y.toPx()
                rotationZ = (1f - t) * -25f
                alpha = (t * 4f).coerceAtMost(1f)
                rotationY = flip
                cameraDistance = 12f * density
            }
            .shadow(4.dp, RoundedCornerShape(8.dp))
    ) {
        if (flip <= 90f) {
            CardFace(card)
        } else {
            CardBack(Modifier.graphicsLayer { rotationY = 180f })
        }
    }
}

@Composable
private fun CardFace(card: TableCard) {
    val ink = if (card.suit.isRed) Color(0xFFC62828) else Color(0xFF1A1A1A)
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xFFFAF7F0), RoundedCornerShape(8.dp))
            .border(0.5.dp, Color.Black.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
            .padding(5.dp)
    ) {
        Column(
            Modifier.align(Alignment.TopStart),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(card.rank.label, color = ink, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(card.suit.symbol, color = ink, fontSize = 13.sp)
        }
        Text(
            card.suit.symbol,
            color = ink,
            fontSize = 30.sp,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun CardBack(modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(listOf(Color(0xFF7A1424), Color(0xFF4A0B16))),
                RoundedCornerShape(8.dp)
            )
            .border(3.dp, Color(0xFFFAF7F0), RoundedCornerShape(8.dp))
            .padding(6.dp)
            .border(1.dp, GoldFoil.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
    )
}

@Composable
fun HandRow(
    hand: List<TableCard>,
    modifier: Modifier = Modifier,
    overlap: Dp = 38.dp,
    dealFrom: DpOffset = DpOffset(140.dp, (-80).dp)
) {
    Box(modifier.width(64.dp + overlap * (hand.size - 1).coerceAtLeast(0)).height(92.dp)) {
        hand.forEachIndexed { i, card ->
            key(card.uid) {
                PlayingCard(
                    card = card,
                    modifier = Modifier.offset(x = overlap * i),
                    dealFrom = dealFrom
                )
            }
        }
    }
}

@Composable
fun SectionLabel(title: String, total: Int?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .width(28.dp)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, GoldFoil.copy(alpha = 0.55f))
                    )
                )
        )
        Text(
            text = if (total != null) "$title · $total" else title,
            color = GoldFoil.copy(alpha = 0.75f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(horizontal = 10.dp)
        )
        Box(
            Modifier
                .width(28.dp)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(GoldFoil.copy(alpha = 0.55f), Color.Transparent)
                    )
                )
        )
    }
}