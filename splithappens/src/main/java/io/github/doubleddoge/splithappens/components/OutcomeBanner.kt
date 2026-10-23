package io.github.doubleddoge.splithappens.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.doubleddoge.splithappens.data.enum.HandResult

// --- Outcome Banner Color Palette ---

// Blackjack (Gold Theme)
private val GoldGradientTop = Color(0xFFD4AF37)
private val GoldGradientBottom = Color(0xFF8B6508)
private val GoldBorder = Color(0xFFFFF0A5)
private val GoldText = Color(0xFF100C08)

// Win (Green Theme)
private val WinGradientTop = Color(0xFF1E703A)
private val WinGradientBottom = Color(0xFF0D3D1E)
private val WinBorder = Color(0xFF4ADE80)
private val WinText = Color(0xFFFFFFFF)

// Push (Gray Theme)
private val PushGradientTop = Color(0xFF4A5568)
private val PushGradientBottom = Color(0xFF2D3748)
private val PushBorder = Color(0xFFA0AEC0)
private val PushText = Color(0xFFFFFFFF)

// Loss / Bust (Red Theme)
private val LossGradientTop = Color(0xFF8C1D1D)
private val LossGradientBottom = Color(0xFF4A0E0E)
private val LossBorder = Color(0xFFFCA5A5)
private val LossText = Color(0xFFFFFFFF)

// Default / In-Progress (Slate Theme)
private val DefaultGradientTop = Color(0xFF1E293B)
private val DefaultGradientBottom = Color(0xFF0F172A)
private val DefaultBorder = Color(0xFF64748B)
private val DefaultText = Color(0xFFE2E8F0)

@Composable
fun OutcomeBanner(
    text: String,
    results: List<HandResult>,
    modifier: Modifier = Modifier,
) {
    val primaryResult = results.firstOrNull()

    // Dynamic styling based on game outcome
    val (backgroundColors, borderColor, textColor) = when (primaryResult) {
        HandResult.BLACKJACK,
        HandResult.NATURAL_BLACKJACK,
        HandResult.QUEEN_JACK -> Triple(
            listOf(GoldGradientTop, GoldGradientBottom),
            GoldBorder,
            GoldText
        )

        HandResult.WIN,
        HandResult.TRIPLE_SEVEN,
        HandResult.SIX_CARD,
        HandResult.SEVEN_CARD -> Triple(
            listOf(WinGradientTop, WinGradientBottom),
            WinBorder,
            WinText
        )

        HandResult.PUSH -> Triple(
            listOf(PushGradientTop, PushGradientBottom),
            PushBorder,
            PushText
        )

        HandResult.LOSS -> Triple(
            listOf(LossGradientTop, LossGradientBottom),
            LossBorder,
            LossText
        )

        else -> Triple(
            listOf(DefaultGradientTop, DefaultGradientBottom),
            DefaultBorder,
            DefaultText
        )
    }

    AnimatedVisibility(
        visible = text.isNotEmpty(),
        enter = fadeIn() + scaleIn(initialScale = 0.8f) + slideInVertically(initialOffsetY = { -20 }),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .shadow(12.dp, shape = RoundedCornerShape(24.dp))
                .background(
                    brush = Brush.verticalGradient(backgroundColors),
                    shape = RoundedCornerShape(24.dp)
                )
                .border(
                    border = BorderStroke(1.5.dp, borderColor),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(horizontal = 24.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = textColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.5.sp
            )
        }
    }
}