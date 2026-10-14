package io.github.doubleddoge.splithappens.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


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
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp),
        contentAlignment = Alignment.BottomCenter
    ){
        Canvas(
            modifier = Modifier
                .width(260.dp)
                .height(150.dp)
                .offset(y = breath.dp)

        ){
            val w = size.width
            val h = size.height

            drawDealerBody(w, h)
            drawDealerShirt(w,h)
            drawDealerHead(w, h)
        }
    }
}

private fun DrawScope.drawDealerBody(w: Float, h: Float){
    // Shoulders and tailored black suit

    val body = Path().apply {
        moveTo(w * 0.04f, h)
        cubicTo(
            w * 0.08f, h * 0.67f,
            w * 0.29f, h * 0.52f,
            w * 0.40f, h * 0.48f
        )
        lineTo(w * 0.60f, h * 0.48f)
        cubicTo(
            w * 0.76f, h * 0.53f,
            w * 0.92f, h * 0.70f,
            w * 0.96f, h
        )
        close()
    }
    drawPath(
        body,
        brush = Brush.verticalGradient(
            colors = listOf(
                SuitHighlight,
                SuitBlack,
                Color(0xFF070A09)
            ),
            startY = h * 0.42f,
            endY = h
        )
    )

    val leftLapel = Path().apply {
        moveTo(w * 0.39f, h * 0.48f)
        lineTo(w * 0.47f, h * 0.78f)
        lineTo(w * 0.49f, h)
        lineTo(w * 0.34f, h * 0.62f)
        close()
    }
}
@Preview(showBackground = true, backgroundColor = 0xFF071B12)
@Composable
private fun DealerBustPreview() {
    DealerBust()
}