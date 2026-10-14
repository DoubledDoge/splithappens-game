package io.github.doubleddoge.splithappens.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview


private val SuitBlack = Color(0xFF101513)
private val SuitHighlight = Color(0xFF303833)
private val ShirtWhite = Color(0xFFF5F1E8)
private val Gold = Color(0xFFD4A64A)
private val Skin = Color(0xFFE5B99A)
private val SkinShadow = Color(0xFFB97F62)
private val Hair = Color(0xFF302018)

@Composable
fun DealerBust(modifier: Modifier = Modifier){
    val transient = rememberInfiniteTransition(label = "dealerMotion")

    val breath by transient.animateFloat(
        initialValue = 0f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2400,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dealerBreating"

    )
    Box(
        modifier
    ){}
}
@Preview(showBackground = true, backgroundColor = 0xFF071B12)
@Composable
private fun DealerBustPreview() {
    DealerBust()
}