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
private val RedChipColor = Color(0xFFB83A3A)
private val GreenChipColor = Color(0xFF3A9A63)
private val BlueChipColor = Color(0xFF4A62A0)
private val DefaultChipColor = Color(0xFF8A6A1F)

private const val FIVE_CHIP = 5
private const val TWENTY_FIVE_CHIP = 25
private const val HUNDRED_CHIP = 100

// Chip dimensions and stroke styling
private val DefaultChipSize = 44.dp
private val DefaultChipElevation = 3.dp
private val RimRingMargin = 3.dp
private val OuterRingStrokeWidth = 5.dp
private val InnerRingStrokeWidth = 1.dp
private const val EDGE_SPOT_SEGMENT_COUNT = 24f
private const val INNER_FACE_RADIUS_RATIO = 0.68f
private const val OUTER_RING_ALPHA = 0.9f
private const val INNER_RING_ALPHA = 0.7f
private val ChipTextFontSize = 11.sp

// Animation constants
private val ChipDropStartOffsetY = (-140).dp

// Bet container and chip stack layout
private val BetContainerHeight = 84.dp
private val StackSpacing = 8.dp
private val ChipStackOffsetStep = 4.dp
private const val MAX_STACKED_CHIPS_SHOWN = 10

// Placeholder and Total text styling
private const val PLACEHOLDER_TEXT_ALPHA = 0.45f
private val PlaceholderTextFontSize = 10.sp
private val PlaceholderLetterSpacing = 1.5.sp
private val PlaceholderBottomPadding = 16.dp
private val TotalTextFontSize = 14.sp
private val TotalTextTopPadding = 4.dp

private fun chipColor(value: Int): Color =
    when (value) {
      FIVE_CHIP -> RedChipColor
      TWENTY_FIVE_CHIP -> GreenChipColor
      HUNDRED_CHIP -> BlueChipColor
      else -> DefaultChipColor
    }

// A single casino chip
@Composable
fun ChipView(value: Int, modifier: Modifier = Modifier, chipSize: Dp = DefaultChipSize) {
  val color = chipColor(value)
  Box(
      modifier = modifier.size(chipSize).shadow(DefaultChipElevation, CircleShape),
      contentAlignment = Alignment.Center,
  ) {
    Canvas(Modifier.fillMaxSize()) {
      val outer = size.minDimension / 2f
      drawCircle(color)

      // 12 white edge spots around the rim
      val ringRadius = outer - RimRingMargin.toPx()
      val seg = (2f * PI.toFloat() * ringRadius) / EDGE_SPOT_SEGMENT_COUNT
      drawCircle(
          color = Color.White.copy(alpha = OUTER_RING_ALPHA),
          radius = ringRadius,
          style =
              Stroke(
                  width = OuterRingStrokeWidth.toPx(),
                  pathEffect = PathEffect.dashPathEffect(floatArrayOf(seg, seg)),
              ),
      )

      // Inner Face
      drawCircle(color, radius = outer * INNER_FACE_RADIUS_RATIO)
      drawCircle(
          color = Color.White.copy(alpha = INNER_RING_ALPHA),
          radius = outer * INNER_FACE_RADIUS_RATIO,
          style = Stroke(InnerRingStrokeWidth.toPx()),
      )
    }
    Text(
        text = value.toString(),
        color = Color.White,
        fontSize = ChipTextFontSize,
        fontWeight = FontWeight.Bold,
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
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
    )
  }
  ChipView(
      value = value,
      modifier =
          modifier.graphicsLayer {
            translationY = (1f - progress.value) * ChipDropStartOffsetY.toPx()
            alpha = progress.value.coerceIn(0f, 1f)
          },
  )
}

// All chips currently bet, stacked by value, with the total underneath
@Composable
fun BetChips(
    chips: List<Int>,
    modifier: Modifier = Modifier,
    onUndo: () -> Unit = {},
) {
  val stacks = chips.groupingBy { it }.eachCount().toSortedMap()
  val total = chips.sum()

  Column(
      modifier =
          modifier.clickable(
              enabled = chips.isNotEmpty(),
              indication = null,
              interactionSource = remember { MutableInteractionSource() },
              onClick = onUndo,
          ),
      horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Box(Modifier.height(BetContainerHeight), contentAlignment = Alignment.BottomCenter) {
      if (chips.isEmpty()) {
        Text(
            "TAP A CHIP TO BET",
            color = ChipGold.copy(alpha = PLACEHOLDER_TEXT_ALPHA),
            fontSize = PlaceholderTextFontSize,
            fontWeight = FontWeight.Bold,
            letterSpacing = PlaceholderLetterSpacing,
            modifier = Modifier.padding(bottom = PlaceholderBottomPadding),
        )
      } else {
        Row(
            horizontalArrangement = Arrangement.spacedBy(StackSpacing),
            verticalAlignment = Alignment.Bottom,
        ) {
          stacks.forEach { (value, count) ->
            val shown = minOf(count, MAX_STACKED_CHIPS_SHOWN)
            val stackHeight = DefaultChipSize + (ChipStackOffsetStep * (shown - 1))

            Box(Modifier.size(width = DefaultChipSize, height = stackHeight)) {
              for (i in 0 until shown) {
                key(value, i) {
                  DroppingChip(
                      value = value,
                      modifier = Modifier
                          .align(Alignment.BottomCenter)
                          .offset(y = -ChipStackOffsetStep * i),
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
          fontSize = TotalTextFontSize,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(top = TotalTextTopPadding),
      )
    }
  }
}
