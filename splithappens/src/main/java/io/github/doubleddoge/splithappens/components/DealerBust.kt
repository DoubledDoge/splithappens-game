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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ---------- Palette ----------
private val SuitBlack = Color(0xFF101513)
private val SuitHighlight = Color(0xFF343D38)
private val ShirtWhite = Color(0xFFF5F1E8)
private val ShirtShade = Color(0xFFD9D5CB)
private val Gold = Color(0xFFD4A64A)
private val Skin = Color(0xFFE8BC9C)
private val SkinLight = Color(0xFFF3CFB0)
private val SkinShadow = Color(0xFFBF8468)
private val SkinDeep = Color(0xFF9A6450)
private val Hair = Color(0xFF2E1E16)
private val HairLight = Color(0xFF5A3D2E)

private val sides = floatArrayOf(-1f, 1f)

// Layout helpers
// so left/right features are mirrored exactly.
private class Frame(val cx: Float, val u: Float) {
    fun x(dx: Float) = cx + dx * u
    fun y(fy: Float) = fy * u
    fun o(dx: Float, fy: Float) = Offset(x(dx), y(fy))
}

private class FramePath(private val f: Frame) {
    val path = Path()
    fun move(dx: Float, fy: Float) = path.moveTo(f.x(dx), f.y(fy))
    fun line(dx: Float, fy: Float) = path.lineTo(f.x(dx), f.y(fy))
    fun quad(cdx: Float, cfy: Float, dx: Float, fy: Float) =
        path.quadraticBezierTo(f.x(cdx), f.y(cfy), f.x(dx), f.y(fy))

    fun curve(
        dx1: Float, fy1: Float,
        dx2: Float, fy2: Float,
        dx3: Float, fy3: Float
    ) = path.cubicTo(f.x(dx1), f.y(fy1), f.x(dx2), f.y(fy2), f.x(dx3), f.y(fy3))

    fun close() = path.close()
}

private fun Frame.path(block: FramePath.() -> Unit): Path = FramePath(this).apply(block).path

// ---------- Composable ----------
@Composable
fun DealerBust(modifier: Modifier = Modifier, height: Dp = 110.dp) {
    val transition = rememberInfiniteTransition(label = "dealerMotion")

    val breath by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dealerBreathing"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        contentAlignment = Alignment.BottomCenter
    ) {
        Canvas(
            modifier = Modifier
                .width(height *260f/150f)
                .height(height)
                .offset(y = breath.dp)
        ) {
            val f = Frame(cx = size.width / 2f, u = size.height)

            drawDealerBody(f)
            val neck = drawDealerNeck(f)
            drawDealerShirt(f)
            drawDealerLapels(f)
            drawDealerCollarAndTie(f)
            drawDealerHead(f)
        }
    }
}

// ---------- Jacket ----------
private fun DrawScope.drawDealerBody(f: Frame) {
    val body = f.path {
        move(-0.84f, 1f)
        curve(-0.82f, 0.82f, -0.66f, 0.69f, -0.30f, 0.63f)
        line(-0.11f, 0.585f)
        line(0.11f, 0.585f)
        line(0.30f, 0.63f)
        curve(0.66f, 0.69f, 0.82f, 0.82f, 0.84f, 1f)
        close()
    }

    drawPath(
        body,
        brush = Brush.verticalGradient(
            colors = listOf(SuitHighlight, SuitBlack, Color(0xFF070A09)),
            startY = f.y(0.58f),
            endY = f.y(1f)
        )
    )
    // Soft rim light on both shoulders
    drawPath(
        body,
        brush = Brush.horizontalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.07f),
                Color.Transparent,
                Color.Transparent,
                Color.White.copy(alpha = 0.07f)
            ),
            startX = f.x(-0.84f),
            endX = f.x(0.84f)
        )
    )

    // Pocket (viewer's right) with welt, gold piping and pocket square
    drawLine(
        color = Color.Black.copy(alpha = 0.55f),
        start = f.o(0.34f, 0.87f),
        end = f.o(0.52f, 0.87f),
        strokeWidth = 1.5.dp.toPx()
    )
    drawLine(
        color = Gold.copy(alpha = 0.8f),
        start = f.o(0.34f, 0.882f),
        end = f.o(0.52f, 0.882f),
        strokeWidth = 1.dp.toPx()
    )
    val square = f.path {
        move(0.355f, 0.87f)
        line(0.370f, 0.830f)
        line(0.400f, 0.855f)
        line(0.430f, 0.820f)
        line(0.460f, 0.855f)
        line(0.505f, 0.870f)
        close()
    }
    drawPath(
        square,
        brush = Brush.verticalGradient(
            colors = listOf(Color.White, ShirtShade),
            startY = f.y(0.82f),
            endY = f.y(0.87f)
        )
    )
}

//Neck
private fun DrawScope.drawDealerNeck(f: Frame): Path {
    val neck = f.path {
        move(-0.075f, 0.42f)
        line(0.075f, 0.42f)
        line(0.092f, 0.62f)
        line(-0.092f, 0.62f)
        close()
    }
    drawPath(
        neck,
        brush = Brush.horizontalGradient(
            0f to SkinDeep,
            0.25f to SkinShadow,
            0.5f to Color(0xFFCF9577),
            0.75f to SkinShadow,
            1f to SkinDeep,
            startX = f.x(-0.092f),
            endX = f.x(0.092f)
        )
    )
    // Shadow cast by the jaw
    clipPath(neck) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF5E3526).copy(alpha = 0.7f), Color.Transparent),
                startY = f.y(0.50f),
                endY = f.y(0.60f)
            ),
            topLeft = f.o(-0.10f, 0.48f),
            size = Size(0.20f * f.u, 0.14f * f.u)
        )
    }
    return neck
}

//Shirt
private fun DrawScope.drawDealerShirt(f: Frame) {
    val shirt = f.path {
        move(-0.12f, 0.57f)
        line(0.12f, 0.57f)
        line(0f, 1f)
        close()
    }
    drawPath(
        shirt,
        brush = Brush.verticalGradient(
            colors = listOf(Color.White, ShirtWhite, ShirtShade),
            startY = f.y(0.57f),
            endY = f.y(1f)
        )
    )
    // Gold studs
    for (i in 0..2) {
        val cy = 0.74f + i * 0.09f
        drawCircle(Color(0xFF8C6A2A), radius = 0.0085f * f.u, center = f.o(0f, cy))
        drawCircle(Gold, radius = 0.006f * f.u, center = f.o(0f, cy))
    }
}

//Lapels
private fun DrawScope.drawDealerLapels(f: Frame) {
    for (s in sides) {
        val lapel = f.path {
            move(s * 0.115f, 0.585f)
            line(s * 0.30f, 0.64f)
            line(s * 0.215f, 0.80f)
            line(s * 0.015f, 1f)
            close()
        }
        drawPath(
            lapel,
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF2E3832), Color(0xFF1A201C)),
                start = f.o(s * 0.30f, 0.64f),
                end = f.o(s * 0.05f, 1f)
            )
        )
        drawPath(
            lapel,
            color = Gold.copy(alpha = 0.65f),
            style = Stroke(width = 1.dp.toPx())
        )
    }

    // Lapel pin (viewer's right)
    drawCircle(Color(0xFF8C6A2A), radius = 0.019f * f.u, center = f.o(0.17f, 0.735f))
    drawCircle(Gold, radius = 0.015f * f.u, center = f.o(0.17f, 0.735f))
    drawCircle(
        Color.White.copy(alpha = 0.55f),
        radius = 0.0045f * f.u,
        center = f.o(0.164f, 0.728f)
    )
}

//Collar + Bow tie
private fun DrawScope.drawDealerCollarAndTie(f: Frame) {
    for (s in sides) {
        val wing = f.path {
            move(s * 0.062f, 0.535f)
            line(s * 0.135f, 0.560f)
            line(s * 0.095f, 0.645f)
            line(s * 0.012f, 0.605f)
            close()
        }
        drawPath(
            wing,
            brush = Brush.linearGradient(
                colors = listOf(Color.White, Color(0xFFE6E2D8)),
                start = f.o(s * 0.06f, 0.53f),
                end = f.o(s * 0.10f, 0.65f)
            )
        )
        drawPath(
            wing,
            color = Color.Black.copy(alpha = 0.12f),
            style = Stroke(width = 0.8.dp.toPx())
        )
    }

    // Bow tie wings (mirrored)
    for (s in sides) {
        val bow = f.path {
            move(s * 0.012f, 0.612f)
            line(s * 0.088f, 0.572f)
            quad(s * 0.100f, 0.612f, s * 0.088f, 0.652f)
            close()
        }
        drawPath(
            bow,
            brush = Brush.horizontalGradient(
                colors = if (s < 0) {
                    listOf(Color(0xFF0C0F0E), Color(0xFF2A302D))
                } else {
                    listOf(Color(0xFF2A302D), Color(0xFF0C0F0E))
                },
                startX = f.x(-0.09f),
                endX = f.x(0.09f)
            )
        )
    }
    // Knot
    drawRoundRect(
        color = Color(0xFF1F2522),
        topLeft = f.o(-0.015f, 0.597f),
        size = Size(0.030f * f.u, 0.030f * f.u),
        cornerRadius = CornerRadius(0.008f * f.u)
    )
    drawCircle(Gold, radius = 0.0055f * f.u, center = f.o(0f, 0.612f))
}

//Headdflkbg.
private fun DrawScope.drawDealerHead(f: Frame) {
    val px = 1.dp.toPx()

    // Ears (behind the face)
    for (s in sides) {
        drawOval(
            color = SkinShadow,
            topLeft = Offset(f.x(s * 0.183f) - 0.026f * f.u, f.y(0.265f)),
            size = Size(0.052f * f.u, 0.11f * f.u)
        )
        drawOval(
            color = SkinDeep.copy(alpha = 0.5f),
            topLeft = Offset(f.x(s * 0.192f) - 0.011f * f.u, f.y(0.288f)),
            size = Size(0.022f * f.u, 0.065f * f.u)
        )
    }

    // Face shape
    val face = f.path {
        move(-0.175f, 0.20f)
        curve(-0.185f, 0.30f, -0.165f, 0.40f, -0.115f, 0.47f)
        curve(-0.085f, 0.52f, -0.050f, 0.555f, 0f, 0.555f)
        curve(0.050f, 0.555f, 0.085f, 0.52f, 0.115f, 0.47f)
        curve(0.165f, 0.40f, 0.185f, 0.30f, 0.175f, 0.20f)
        curve(0.170f, 0.07f, -0.170f, 0.07f, -0.175f, 0.20f)
        close()
    }
    drawPath(
        face,
        brush = Brush.horizontalGradient(
            0f to SkinShadow,
            0.22f to Skin,
            0.50f to SkinLight,
            0.78f to Skin,
            1f to SkinShadow,
            startX = f.x(-0.185f),
            endX = f.x(0.185f)
        )
    )

    // Soft shading inside the face
    clipPath(face) {
        // Shadow from the hair on the forehead
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF6B4332).copy(alpha = 0.4f), Color.Transparent),
                startY = f.y(0.09f),
                endY = f.y(0.21f)
            ),
            topLeft = f.o(-0.19f, 0.06f),
            size = Size(0.38f * f.u, 0.16f * f.u)
        )
        // Jawline shade
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Transparent, SkinShadow.copy(alpha = 0.55f)),
                startY = f.y(0.42f),
                endY = f.y(0.56f)
            ),
            topLeft = f.o(-0.19f, 0.42f),
            size = Size(0.38f * f.u, 0.15f * f.u)
        )
        // Cheek blush
        for (s in sides) {
            val c = f.o(s * 0.10f, 0.38f)
            val r = 0.07f * f.u
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFD98F7A).copy(alpha = 0.30f), Color.Transparent),
                    center = c,
                    radius = r
                ),
                radius = r,
                center = c
            )
        }
    }

    // Eyes and brows (mirrored)
    for (s in sides) {
        val ex = s * 0.082f
        val ey = 0.305f

        val eye = f.path {
            move(ex - 0.040f, ey)
            quad(ex, ey - 0.050f, ex + 0.040f, ey)
            quad(ex, ey + 0.036f, ex - 0.040f, ey)
            close()
        }
        drawPath(eye, Color(0xFFF7F1E8))
        clipPath(eye) {
            drawCircle(Color(0xFF4A3426), radius = 0.021f * f.u, center = f.o(ex, ey - 0.004f))
            drawCircle(Color(0xFF1A110C), radius = 0.011f * f.u, center = f.o(ex, ey - 0.004f))
            drawCircle(
                Color.White.copy(alpha = 0.9f),
                radius = 0.0055f * f.u,
                center = f.o(ex + 0.008f, ey - 0.011f)
            )
            // Shadow of the upper lid
            drawRect(
                Color.Black.copy(alpha = 0.18f),
                topLeft = f.o(ex - 0.045f, ey - 0.04f),
                size = Size(0.09f * f.u, 0.022f * f.u)
            )
        }
        val lid = f.path {
            move(ex - 0.043f, ey + 0.002f)
            quad(ex, ey - 0.054f, ex + 0.043f, ey + 0.002f)
        }
        drawPath(lid, Hair, style = Stroke(width = 1.4f * px, cap = StrokeCap.Round))

        val brow = f.path {
            move(s * 0.036f, 0.262f)
            quad(s * 0.088f, 0.226f, s * 0.132f, 0.262f)
        }
        drawPath(brow, Hair, style = Stroke(width = 0.016f * f.u, cap = StrokeCap.Round))
    }

    // Nose
    val bridge = f.path {
        move(0.026f, 0.30f)
        quad(0.040f, 0.35f, 0.034f, 0.385f)
    }
    drawPath(
        bridge,
        SkinShadow.copy(alpha = 0.6f),
        style = Stroke(width = 1.6f * px, cap = StrokeCap.Round)
    )
    val noseBase = f.path {
        move(-0.036f, 0.392f)
        quad(0f, 0.418f, 0.036f, 0.392f)
    }
    drawPath(
        noseBase,
        SkinDeep.copy(alpha = 0.7f),
        style = Stroke(width = 1.3f * px, cap = StrokeCap.Round)
    )
    for (s in sides) {
        drawCircle(
            SkinDeep.copy(alpha = 0.55f),
            radius = 0.005f * f.u,
            center = f.o(s * 0.021f, 0.398f)
        )
    }
    run {
        val c = f.o(0f, 0.372f)
        val r = 0.028f * f.u
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White.copy(alpha = 0.20f), Color.Transparent),
                center = c,
                radius = r
            ),
            radius = r,
            center = c
        )
    }

    // Mouth: subtle confident smile
    val lowerLip = f.path {
        move(-0.038f, 0.469f)
        quad(0f, 0.496f, 0.038f, 0.469f)
        quad(0f, 0.476f, -0.038f, 0.469f)
        close()
    }
    drawPath(lowerLip, Color(0xFFC98270).copy(alpha = 0.75f))
    val upperLip = f.path {
        move(-0.05f, 0.463f)
        quad(-0.025f, 0.452f, 0f, 0.458f)
        quad(0.025f, 0.452f, 0.05f, 0.463f)
    }
    drawPath(
        upperLip,
        Color(0xFFB5705E).copy(alpha = 0.7f),
        style = Stroke(width = 1.2f * px, cap = StrokeCap.Round)
    )
    val lipLine = f.path {
        move(-0.05f, 0.463f)
        quad(0f, 0.478f, 0.05f, 0.463f)
    }
    drawPath(
        lipLine,
        Color(0xFF8C5042),
        style = Stroke(width = 1.3f * px, cap = StrokeCap.Round)
    )
    // Chin definition
    val chin = f.path {
        move(-0.025f, 0.507f)
        quad(0f, 0.518f, 0.025f, 0.507f)
    }
    drawPath(
        chin,
        SkinShadow.copy(alpha = 0.35f),
        style = Stroke(width = 1.2f * px, cap = StrokeCap.Round)
    )

    // Hair (side part, swept to the right)
    val hair = f.path {
        move(-0.182f, 0.27f)
        curve(-0.225f, 0.12f, -0.13f, 0.012f, 0f, 0.012f)
        curve(0.13f, 0.012f, 0.225f, 0.12f, 0.182f, 0.27f)
        curve(0.180f, 0.22f, 0.155f, 0.15f, 0.10f, 0.125f)
        curve(0.04f, 0.10f, -0.03f, 0.085f, -0.07f, 0.092f)
        curve(-0.12f, 0.10f, -0.16f, 0.15f, -0.166f, 0.225f)
        close()
    }
    drawPath(
        hair,
        brush = Brush.verticalGradient(
            colors = listOf(HairLight, Hair, Color(0xFF1A110C)),
            startY = f.y(0.012f),
            endY = f.y(0.27f)
        )
    )

    // Hair strands and the part line
    val strandColor = HairLight.copy(alpha = 0.55f)
    val strandStroke = Stroke(width = 0.9f * px, cap = StrokeCap.Round)
    drawPath(
        f.path {
            move(-0.06f, 0.088f)
            curve(-0.03f, 0.04f, 0.07f, 0.03f, 0.15f, 0.085f)
        },
        strandColor, style = strandStroke
    )
    drawPath(
        f.path {
            move(-0.09f, 0.11f)
            curve(-0.10f, 0.05f, -0.01f, 0.03f, 0.10f, 0.06f)
        },
        strandColor, style = strandStroke
    )
    drawPath(
        f.path {
            move(0.04f, 0.12f)
            curve(0.10f, 0.09f, 0.15f, 0.10f, 0.17f, 0.17f)
        },
        strandColor, style = strandStroke
    )
    drawLine(
        color = Color.Black.copy(alpha = 0.35f),
        start = f.o(-0.07f, 0.092f),
        end = f.o(-0.075f, 0.045f),
        strokeWidth = 1.dp.toPx(),
        cap = StrokeCap.Round
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF071B12)
@Composable
private fun DealerBustPreview() {
    DealerBust()
}