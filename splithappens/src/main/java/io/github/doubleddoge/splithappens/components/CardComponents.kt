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

// Colors
private val GoldFoil = Color(0xFFE5B036)
private val RedSuitColor = Color(0xFFC62828)
private val BlackSuitColor = Color(0xFF1A1A1A)
private val CardFaceBackgroundColor = Color(0xFFFAF7F0)
private val CardBackPrimaryColor = Color(0xFF7A1424)
private val CardBackSecondaryColor = Color(0xFF4A0B16)

// Animation parameters
private const val CARD_FLY_DURATION_MS = 450
private const val CARD_FLIP_DURATION_MS = 400
private const val FLY_ROTATION_Z_DEG = -25f
private const val FLY_ALPHA_MULTIPLIER = 4f
private const val CARD_CAMERA_DISTANCE = 12f
private const val FLIP_MIDPOINT_DEG = 90f
private const val FACE_DOWN_FLIP_DEG = 180f
private const val FACE_UP_FLIP_DEG = 0f

// Card layout & shape dimensions
private val DefaultCardWidth = 64.dp
private val DefaultCardHeight = 92.dp
private val DefaultHandOverlap = 38.dp
private val DefaultDealFromOffset = DpOffset(140.dp, (-80).dp)

private val CardCornerRadius = 8.dp
private val CardInnerCornerRadius = 4.dp
private val CardShadowElevation = 4.dp

// Borders & Padding
private val CardFaceBorderWidth = 0.5.dp
private val CardBackOuterBorderWidth = 3.dp
private val CardBackInnerBorderWidth = 1.dp
private val CardFacePadding = 5.dp
private val CardBackPadding = 6.dp

private const val CARD_FACE_BORDER_ALPHA = 0.25f
private const val CARD_BACK_INNER_BORDER_ALPHA = 0.6f

// Typography
private val CornerRankFontSize = 15.sp
private val CenterSuitFontSize = 30.sp

// Section label styling
private val SectionLabelLineLength = 28.dp
private val SectionLabelLineWidth = 1.dp
private val SectionLabelTextPadding = 10.dp
private val SectionLabelFontSize = 12.sp
private val SectionLabelLetterSpacing = 2.sp
private const val SECTION_LINE_ALPHA = 0.55f
private const val SECTION_TEXT_ALPHA = 0.75f

@Composable
fun PlayingCard(
    card: TableCard,
    modifier: Modifier = Modifier,
    dealFrom: DpOffset = DefaultDealFromOffset,
) {
    val fly = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        fly.animateTo(1f, tween(CARD_FLY_DURATION_MS, easing = FastOutSlowInEasing))
    }
    val flip by animateFloatAsState(
        targetValue = if (card.isFaceUp) FACE_UP_FLIP_DEG else FACE_DOWN_FLIP_DEG,
        animationSpec = tween(CARD_FLIP_DURATION_MS),
        label = "cardFlip",
    )

    Box(
        modifier
            .size(width = DefaultCardWidth, height = DefaultCardHeight)
            .graphicsLayer {
                val t = fly.value
                translationX = (1f - t) * dealFrom.x.toPx()
                translationY = (1f - t) * dealFrom.y.toPx()
                rotationZ = (1f - t) * FLY_ROTATION_Z_DEG
                alpha = (t * FLY_ALPHA_MULTIPLIER).coerceAtMost(1f)
                rotationY = flip
                cameraDistance = CARD_CAMERA_DISTANCE * density
            }
            .shadow(CardShadowElevation, RoundedCornerShape(CardCornerRadius)),
    ) {
        if (flip <= FLIP_MIDPOINT_DEG) {
            CardFace(card)
        } else {
            CardBack(Modifier.graphicsLayer { rotationY = FACE_DOWN_FLIP_DEG })
        }
    }
}

@Composable
private fun CardFace(card: TableCard) {
    val ink = if (card.suit.isRed) RedSuitColor else BlackSuitColor
    Box(
        Modifier
            .fillMaxSize()
            .background(CardFaceBackgroundColor, RoundedCornerShape(CardCornerRadius))
            .border(
                CardFaceBorderWidth,
                Color.Black.copy(alpha = CARD_FACE_BORDER_ALPHA),
                RoundedCornerShape(CardCornerRadius),
            )
            .padding(CardFacePadding),
    ) {
        Column(
            Modifier.align(Alignment.TopStart),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                card.rank.label,
                color = ink,
                fontSize = CornerRankFontSize,
                fontWeight = FontWeight.Bold,
            )
        }
        Text(
            card.suit.symbol,
            color = ink,
            fontSize = CenterSuitFontSize,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

@Composable
private fun CardBack(modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(listOf(CardBackPrimaryColor, CardBackSecondaryColor)),
                RoundedCornerShape(CardCornerRadius),
            )
            .border(
                CardBackOuterBorderWidth,
                CardFaceBackgroundColor,
                RoundedCornerShape(CardCornerRadius),
            )
            .padding(CardBackPadding)
            .border(
                CardBackInnerBorderWidth,
                GoldFoil.copy(alpha = CARD_BACK_INNER_BORDER_ALPHA),
                RoundedCornerShape(CardInnerCornerRadius),
            ),
    )
}

@Composable
fun HandRow(
    hand: List<TableCard>,
    modifier: Modifier = Modifier,
    overlap: Dp = DefaultHandOverlap,
    dealFrom: DpOffset = DefaultDealFromOffset,
) {
    val totalWidth = DefaultCardWidth + (overlap * (hand.size - 1).coerceAtLeast(0))
    Box(modifier.width(totalWidth).height(DefaultCardHeight)) {
        hand.forEachIndexed { i, card ->
            key(card.uid) {
                PlayingCard(
                    card = card,
                    modifier = Modifier.offset(x = overlap * i),
                    dealFrom = dealFrom,
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
                .width(SectionLabelLineLength)
                .height(SectionLabelLineWidth)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, GoldFoil.copy(alpha = SECTION_LINE_ALPHA)),
                    ),
                ),
        )
        Text(
            text = if (total != null) "$title · $total" else title,
            color = GoldFoil.copy(alpha = SECTION_TEXT_ALPHA),
            fontSize = SectionLabelFontSize,
            fontWeight = FontWeight.Bold,
            letterSpacing = SectionLabelLetterSpacing,
            modifier = Modifier.padding(horizontal = SectionLabelTextPadding),
        )
        Box(
            Modifier
                .width(SectionLabelLineLength)
                .height(SectionLabelLineWidth)
                .background(
                    Brush.horizontalGradient(
                        listOf(GoldFoil.copy(alpha = SECTION_LINE_ALPHA), Color.Transparent),
                    ),
                ),
        )
    }
}
