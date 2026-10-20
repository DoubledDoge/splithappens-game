package io.github.doubleddoge.splithappens.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI

private val ChipGold = Color(0xFFE5B036)

private fun chipColor(value: Int): Color = when (value) {
    5 -> Color(0xFFB83A3A)
    25 -> Color(0xFF3A9A63)
    100 -> Color(0xFF4A62A0)
    else -> Color(0xFF8A6A1F)
}

// A single casino chip
@Composable
fun ChipView(value: Int, modifier: Modifier = Modifier, chipSize: Dp = 44.dp) {
    val color = chipColor(value)
    Box(
        modifier = modifier
            .size(chipSize)
            .shadow(3.dp, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val outer = size.minDimension / 2f
            drawCircle(color)

            // 12 white edge spots around the rim
            val ringRadius = outer - 3.dp.toPx()
            val seg = (2f * PI.toFloat() * ringRadius) / 24f
            drawCircle(
                color = Color.White.copy(alpha = 0.9f),
                radius = ringRadius,
                style = Stroke(
                    width = 5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(seg, seg))
                )
            )

            // Inner Face
            drawCircle(color, radius = outer * 0.68f)
            drawCircle(
                color = Color.White.copy(alpha = 0.7f),
                radius = outer * 0.68f,
                style = Stroke(1.dp.toPx())
            )
        }
        Text(
            text = "$value",
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// A chip that drops onto the table when it first appears
@Composable
private fun DroppingChip(value: Int, modifier: Modifier = Modifier) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(
            1f,
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
        )
    }
    ChipView(
        value = value,
        modifier = modifier.graphicsLayer {
            translationY = (1f - progress.value) * -140.dp.toPx()
            alpha = progress.value.coerceIn(0f, 1f)
        }
    )
}

// All chips currently bet, stacked by value, with the total underneath
@Composable
fun BetChips(
    chips: List<Int>,
    modifier: Modifier = Modifier,
    onUndo: () -> Unit = {}
) {
    val stacks = chips.groupingBy { it }.eachCount().toSortedMap()
    val total = chips.sum()

    Column(
        modifier = modifier.clickable(
            enabled = chips.isNotEmpty(),
            indication = null,
            interactionSource = remember { MutableInteractionSource() },
            onClick = onUndo
        ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(Modifier.height(84.dp), contentAlignment = Alignment.BottomCenter) {
            if (chips.isEmpty()) {
                Text(
                    "TAP A CHIP TO BET",
                    color = ChipGold.copy(alpha = 0.45f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    stacks.forEach { (value, count) ->
                        val shown = minOf(count, 10)

                        Box(Modifier.size(width = 44.dp, height = 44.dp + (4 * (shown - 1)).dp)) {
                            for (i in 0 until shown) {
                                key(value, i) {
                                    DroppingChip(
                                        value = value,
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .offset(y = (-4 * i).dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        if (total > 0) {
            Text(
                text = "R$total",
                color = ChipGold,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}