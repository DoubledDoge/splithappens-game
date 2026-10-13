package io.github.doubleddoge.splithappens.components

import androidx.compose.animation.core.EaseInOutSine
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

@Composable
fun DealerBust(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dealerBreathing")
    val breatheY by infiniteTransition.animateFloat(
        initialValue = -2.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "torsoFloat"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Canvas(
            modifier = Modifier
                .width(260.dp)
                .height(175.dp)
                .offset { IntOffset(0, breatheY.toInt()) }
        ) {
            val w = size.width
            val h = size.height

            // 1. Shoulders, Neck & Torso
            drawTorsoAndWaistcoat(w, h)

            // 2. Collar & Black Bow Tie
            drawShirtAndTie(w, h)

            // 3. Head & Face
            drawHeadAndFeatures(w, h)
        }
    }
}

private fun DrawScope.drawTorsoAndWaistcoat(w: Float, h: Float) {
    // Shoulders base shape
    val bodyPath = Path().apply {
        moveTo(w * 0.04f, h)
        cubicTo(
            w * 0.12f, h * 0.65f,
            w * 0.28f, h * 0.48f,
            w * 0.40f, h * 0.44f
        )
        lineTo(w * 0.60f, h * 0.44f)
        cubicTo(
            w * 0.72f, h * 0.48f,
            w * 0.88f, h * 0.65f,
            w * 0.96f, h
        )
        close()
    }

    drawPath(
        path = bodyPath,
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF222624), Color(0xFF101412), Color(0xFF090C0B))
        )
    )

    // Gold lapel piping
    val leftLapel = Path().apply {
        moveTo(w * 0.38f, h * 0.45f)
        lineTo(w * 0.43f, h * 0.82f)
        lineTo(w * 0.48f, h)
    }
    val rightLapel = Path().apply {
        moveTo(w * 0.62f, h * 0.45f)
        lineTo(w * 0.57f, h * 0.82f)
        lineTo(w * 0.52f, h)
    }
    drawPath(leftLapel, color = Color(0xFFD4A311).copy(alpha = 0.55f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.5f))
    drawPath(rightLapel, color = Color(0xFFD4A311).copy(alpha = 0.55f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.5f))

    // Waistcoat buttons
    drawCircle(Color(0xFFD4A311), radius = 3f, center = Offset(w * 0.50f, h * 0.72f))
    drawCircle(Color(0xFFD4A311), radius = 3f, center = Offset(w * 0.50f, h * 0.84f))
    drawCircle(Color(0xFFD4A311), radius = 3f, center = Offset(w * 0.50f, h * 0.96f))
}

private fun DrawScope.drawShirtAndTie(w: Float, h: Float) {
    // White dress shirt V-neck
    val shirtV = Path().apply {
        moveTo(w * 0.42f, h * 0.44f)
        lineTo(w * 0.50f, h * 0.68f)
        lineTo(w * 0.58f, h * 0.44f)
        close()
    }
    drawPath(shirtV, color = Color(0xFFF7F9F8))

    // Crisp shirt collar points
    val leftCollar = Path().apply {
        moveTo(w * 0.42f, h * 0.43f)
        lineTo(w * 0.47f, h * 0.51f)
        lineTo(w * 0.48f, h * 0.45f)
        close()
    }
    val rightCollar = Path().apply {
        moveTo(w * 0.58f, h * 0.43f)
        lineTo(w * 0.53f, h * 0.51f)
        lineTo(w * 0.52f, h * 0.45f)
        close()
    }
    drawPath(leftCollar, color = Color(0xFFFFFFFF))
    drawPath(rightCollar, color = Color(0xFFFFFFFF))

    // Bow tie wings
    val leftWing = Path().apply {
        moveTo(w * 0.50f, h * 0.48f)
        lineTo(w * 0.45f, h * 0.44f)
        lineTo(w * 0.45f, h * 0.52f)
        close()
    }
    val rightWing = Path().apply {
        moveTo(w * 0.50f, h * 0.48f)
        lineTo(w * 0.55f, h * 0.44f)
        lineTo(w * 0.55f, h * 0.52f)
        close()
    }
    drawPath(leftWing, color = Color(0xFF151515))
    drawPath(rightWing, color = Color(0xFF151515))
    drawCircle(Color(0xFF1E1E1E), radius = 4f, center = Offset(w * 0.50f, h * 0.48f))
}

private fun DrawScope.drawHeadAndFeatures(w: Float, h: Float) {
    val headCenterX = w * 0.50f
    val headCenterY = h * 0.26f

    // Neck
    drawRect(
        color = Color(0xFFDCA988),
        topLeft = Offset(w * 0.45f, h * 0.32f),
        size = Size(w * 0.10f, h * 0.14f)
    )

    // Neck shadow under chin
    drawArc(
        color = Color(0xFFC08C69),
        startAngle = 0f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = Offset(w * 0.45f, h * 0.33f),
        size = Size(w * 0.10f, 16f)
    )

    // Jaw / Face
    val jawPath = Path().apply {
        moveTo(headCenterX - 28f, headCenterY - 10f)
        cubicTo(
            headCenterX - 30f, headCenterY + 24f,
            headCenterX - 18f, headCenterY + 38f,
            headCenterX, headCenterY + 40f
        )
        cubicTo(
            headCenterX + 18f, headCenterY + 38f,
            headCenterX + 30f, headCenterY + 24f,
            headCenterX + 28f, headCenterY - 10f
        )
        close()
    }
    drawPath(jawPath, color = Color(0xFFE8BA9A))

    // Upper head base
    drawCircle(
        color = Color(0xFFE8BA9A),
        radius = 30f,
        center = Offset(headCenterX, headCenterY)
    )

    // Ears
    drawOval(Color(0xFFDCA988), topLeft = Offset(headCenterX - 34f, headCenterY - 6f), size = Size(8f, 16f))
    drawOval(Color(0xFFDCA988), topLeft = Offset(headCenterX + 26f, headCenterY - 6f), size = Size(8f, 16f))

    // Eyes
    drawCircle(Color(0xFF2B1D14), radius = 3.2f, center = Offset(headCenterX - 11f, headCenterY + 4f))
    drawCircle(Color(0xFF2B1D14), radius = 3.2f, center = Offset(headCenterX + 11f, headCenterY + 4f))

    // Eyebrows
    drawLine(Color(0xFF382315), start = Offset(headCenterX - 17f, headCenterY - 4f), end = Offset(headCenterX - 6f, headCenterY - 3f), strokeWidth = 2.5f)
    drawLine(Color(0xFF382315), start = Offset(headCenterX + 6f, headCenterY - 3f), end = Offset(headCenterX + 17f, headCenterY - 4f), strokeWidth = 2.5f)

    // Nose bridge & subtle smile
    drawLine(Color(0xFFC08C69), start = Offset(headCenterX, headCenterY + 3f), end = Offset(headCenterX - 1.5f, headCenterY + 16f), strokeWidth = 2f)
    drawArc(
        color = Color(0xFF9E6548),
        startAngle = 20f,
        sweepAngle = 140f,
        useCenter = false,
        topLeft = Offset(headCenterX - 9f, headCenterY + 20f),
        size = Size(18f, 9f),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.2f)
    )

    // Hair: Stylized dark side-parted croupier hair
    val hairPath = Path().apply {
        moveTo(headCenterX - 32f, headCenterY - 2f)
        cubicTo(
            headCenterX - 35f, headCenterY - 34f,
            headCenterX - 10f, headCenterY - 40f,
            headCenterX, headCenterY - 38f
        )
        cubicTo(
            headCenterX + 20f, headCenterY - 40f,
            headCenterX + 35f, headCenterY - 28f,
            headCenterX + 32f, headCenterY - 2f
        )
        cubicTo(
            headCenterX + 24f, headCenterY - 14f,
            headCenterX + 10f, headCenterY - 20f,
            headCenterX - 2f, headCenterY - 16f
        )
        cubicTo(
            headCenterX - 16f, headCenterY - 12f,
            headCenterX - 26f, headCenterY - 10f,
            headCenterX - 32f, headCenterY - 2f
        )
        close()
    }
    drawPath(hairPath, color = Color(0xFF261911))
}

@Preview(showBackground = true, backgroundColor = 0xFF071B12)
@Composable
fun DealerBustPreview() {
    DealerBust()
}