package io.github.doubleddoge.splithappens.ui.components

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
import io.github.doubleddoge.splithappens.game.model.HandResult

import io.github.doubleddoge.splithappens.ui.theme.DefaultBorder
import io.github.doubleddoge.splithappens.ui.theme.DefaultGradientBottom
import io.github.doubleddoge.splithappens.ui.theme.DefaultGradientTop
import io.github.doubleddoge.splithappens.ui.theme.DefaultText
import io.github.doubleddoge.splithappens.ui.theme.GoldBorder
import io.github.doubleddoge.splithappens.ui.theme.GoldGradientBottom
import io.github.doubleddoge.splithappens.ui.theme.GoldGradientTop
import io.github.doubleddoge.splithappens.ui.theme.GoldText
import io.github.doubleddoge.splithappens.ui.theme.LossBorder
import io.github.doubleddoge.splithappens.ui.theme.LossGradientBottom
import io.github.doubleddoge.splithappens.ui.theme.LossGradientTop
import io.github.doubleddoge.splithappens.ui.theme.PushBorder
import io.github.doubleddoge.splithappens.ui.theme.PushGradientBottom
import io.github.doubleddoge.splithappens.ui.theme.PushGradientTop
import io.github.doubleddoge.splithappens.ui.theme.WinBorder
import io.github.doubleddoge.splithappens.ui.theme.WinGradientBottom
import io.github.doubleddoge.splithappens.ui.theme.WinGradientTop

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
			Color.White
		)

		HandResult.PUSH -> Triple(
			listOf(PushGradientTop, PushGradientBottom),
			PushBorder,
			Color.White
		)

		HandResult.LOSS -> Triple(
			listOf(LossGradientTop, LossGradientBottom),
			LossBorder,
			Color.White
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