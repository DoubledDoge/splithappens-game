package io.github.doubleddoge.splithappens.components

import android.R
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
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
    val rightLapel = Path().apply {
        moveTo(w * 0.61f, h * 0.48f)
        lineTo(w * 0.53f, h * 0.78f)
        lineTo(w * 0.51f, h)
        lineTo(w * 0.66f, h * 0.62f)
        close()
    }

    drawPath(leftLapel, color = Color(0xFF202722))
    drawPath(rightLapel, color = Color(0xFF202722))

    drawPath(
        leftLapel,
        color = Gold.copy(alpha = 0.8f),
        style = Stroke(width = 1.4f)
    )
    drawPath(
        rightLapel,
        color = Gold.copy( alpha = 0.8f),
        style = Stroke(width = 1.4f)
    )
    // Small gold pocket detail
    drawLine(
        color = Gold,
        start = Offset(w * 0.58f, h * 0.76f),
        end = Offset(w * 0.68f, h * 0.76f),
        strokeWidth = 2f
    )
    // Gold lapel pin
    drawCircle(
        color = Gold,
        radius = w * 0.012f,
        center = Offset(w * 0.62f, h * 0.68f)
    )
}

private fun DrawScope.drawDealerShirt(w: Float, h: Float){
    //White Shirt
    val shirt = Path().apply {
        moveTo(w * 0.41f, h * 0.48f)
        lineTo(w* 0.50f, h * 0.76f)
        lineTo(w * 0.59f, h * 0.48f)
        close()
    }
    drawPath(
        shirt,
        brush = Brush.verticalGradient(
            colors = listOf(Color.White,ShirtWhite, Color(0xFFD8D5CC)
            ),
            startY = h * 0.45f,
            endY = h
        )
    )
    val leftCollar = Path().apply {
        moveTo(w * 0.40f, h * 0.45f)
        lineTo(w * 0.49f, h * 0.55f)
        lineTo(w * 0.46f, h * 0.63f)
        lineTo(w * 0.38f, h * 0.49f)
        close()
    }
    
    val rightCollar = Path().apply {
        moveTo(w * 0.60f, h * 0.45f)
        lineTo(w * 0.51f, h * 0.55f)
        lineTo(w * 0.54f, h * 0.63f)
        lineTo(w * 0.62f, h * 0.49f)
        close()
    }

    drawPath(leftCollar, color = Color.White)
    drawPath(rightCollar, color = Color(0xFFE5E1D7))

    //Bow tie
    val bowLeft = Path().apply {
        moveTo(w * 0.50f, h * 0.56f)
        lineTo(w * 0.455f, h * 0.525f)
        lineTo(w * 0.455f, h * 0.60f)
        close()
    }

    val bowRight = Path().apply {
        moveTo(w * 0.50f, h * 0.56f)
        lineTo(w * 0.545f, h * 0.525f)
        lineTo(w * 0.545f, h * 0.60f)
        close()
    }
    drawPath(bowRight, color = Color(0xFF171A18))
    drawPath(bowRight, color = Color(0xFF171A18))
    drawCircle(
        color = Gold,
        radius = w * 0.008f,
        center = Offset(w * 0.50f, h * 0.56f)
    )


    //Shirt Buttons
    for (i in 0..2){
        drawCircle(
            color = Color(0xFFB6B1A5),
            radius = 1.5f,
            center = Offset(w * 0.50f, h * (0.68f + i * 0.075f))
        )
    }


}
private fun DrawScope.drawDealerHead(w: Float, h: Float){
    val cx = w * 0.50f
    val cy = h * 0.315f
    val rx = w * 0.092f
    val ry = h * 0.205f

    //Neck behinde the face
    drawRect(
        color = SkinShadow,
        topLeft = Offset(cx - rx * 0.42f, cy + ry * 0.52f),
        size = Size(rx * 0.84f, ry * 0.83f)
    )

    //Ears
    drawOval(
        color = SkinShadow,
        topLeft = Offset(cx - rx * 1.08f, cy - ry * 0.02f),
        size = Size(rx * 0.28f, ry * 0.42f)
    )

    //Face shape
    val face = Path().apply {
        moveTo(cx - rx * 0.90f, cy - ry * 0.70f)
        cubicTo(
            cx - rx * 1.03f, cy + ry * 0.20f,
            cx - rx * 0.55f, cy + ry * 0.94f,
            cx, cy + ry
        )
        cubicTo(
            cx + rx * 0.55f, cy + ry * 0.94f,
            cx + rx * 1.03f, cy + ry * 0.20f,
            cx + rx * 0.90f, cy - ry * 0.70f
        )
        close()
    }
    drawPath(
        face,
        brush = Brush.horizontalGradient(
            colors = listOf(
                SkinShadow,
                Skin,
                Color(0xFFF1CBAA)
            ),
            startX = cx - rx,
            endX = cx + rx
        )
    )

    val hair = Path().apply {
        moveTo(cx - rx * 1.02f, cy - ry * 0.36f)
        cubicTo(
            cx - rx * 1.12f, cy - ry * 1.12f,
            cx - rx * 0.25f, cy - ry * 1.25f,
            cx + rx * 0.15f, cy - ry * 1.14f
        )
        cubicTo(
            cx + rx * 0.90f, cy - ry * 1.15f,
            cx + rx * 1.10f, cy - ry * 0.68f,
            cx + rx * 0.98f, cy - ry * 0.24f
        )
        cubicTo(
            cx + rx * 0.62f, cy - ry * 0.63f,
            cx + rx * 0.10f, cy - ry * 0.68f,
            cx - rx * 0.20f, cy - ry * 0.46f
        )
        cubicTo(
            cx - rx * 0.55f, cy - ry * 0.25f,
            cx - rx * 0.78f, cy - ry * 0.32f,
            cx - rx * 1.02f, cy - ry * 0.36f
        )
        close()
    }

    drawPath(
        hair,
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF4A3023),
                Hair,
                Color(0xFF17110D)
            ),
            startY = cy - ry * 1.25f,
            endY = cy
        )
    )

    //Eyebrows
    drawLine(
        color = Hair,
        start = Offset(cx - rx * 0.62f, cy - ry * 0.04f),
        end = Offset(cx - rx * 0.16f, cy - ry * 0.10f),
        strokeWidth = 2.3f
    )
    drawLine(
        color = Hair,
        start = Offset(cx + rx * 0.16f, cy - ry * 0.10f),
        end = Offset(cx + rx * 0.62f, cy - ry * 0.04f),
        strokeWidth = 2.3f
    )

    //Eyes
    drawOval(
        color = Color(0xFFF8F2E9),
        topLeft = Offset(cx - rx * 0.58f, cy + ry * 0.04f),
        size = Size(rx * 0.38f, ry * 0.14f)
    )
    drawOval(
        color = Color(0xFFF8F2E9),
        topLeft = Offset(cx + rx * 0.20f, cy + ry * 0.04f),
        size = Size(rx * 0.38f, ry * 0.14f)
    )
    drawCircle(
        color = Color(0xFF39291F),
        radius = rx * 0.075f,
        center = Offset(cx - rx * 0.39f, cy + ry * 0.105f)
    )
    drawCircle(
        color = Color(0xFF39291F),
        radius = rx * 0.075f,
        center = Offset(cx + rx * 0.39f, cy + ry * 0.105f)
    )
}
@Preview(showBackground = true, backgroundColor = 0xFF071B12)
@Composable
private fun DealerBustPreview() {
    DealerBust()
}