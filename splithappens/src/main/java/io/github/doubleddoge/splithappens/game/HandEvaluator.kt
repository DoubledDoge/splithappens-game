package io.github.doubleddoge.splithappens.game

import io.github.doubleddoge.splithappens.game.enum.GameMode
import io.github.doubleddoge.splithappens.game.enum.HandResult

object HandEvaluator {
	private val naturalResults =
		setOf(HandResult.BLACKJACK, HandResult.QUEEN_JACK, HandResult.NATURAL_BLACKJACK)

	fun premium(cards: List<Card>, afterSplit: Boolean, mode: GameMode): HandResult? {
		val hand = cards.evaluateHand(afterSplit)
		if (hand.isBust) return null

		// maxByOrNull keeps the first candidate when multipliers tie.
		val candidates = buildList {
			if (hand.isBlackjack) add(HandResult.BLACKJACK)
			if (hand.isQueenAce) add(HandResult.QUEEN_JACK)
			if (hand.isJackAce) add(HandResult.NATURAL_BLACKJACK)
			if (hand.isTripleSeven) add(HandResult.TRIPLE_SEVEN)
			if (hand.isSixCard) add(HandResult.SIX_CARD)
			if (hand.isSevenCard) add(HandResult.SEVEN_CARD)
		}
		return candidates.filter { it in mode.allowedResults }.maxByOrNull { it.multiplier }
	}

	fun settle(
		player: List<Card>,
		afterSplit: Boolean,
		surrendered: Boolean,
		dealer: List<Card>,
		mode: GameMode,
	): HandResult {
		if (surrendered) return HandResult.SURRENDER

		val playerHand = player.evaluateHand(afterSplit)
		if (playerHand.isBust) return HandResult.LOSS

		val premiumResult = premium(player, afterSplit, mode)
		val dealerHand = dealer.evaluateHand()

		if (dealerHand.isBlackjack) {
			return if (premiumResult in naturalResults) HandResult.PUSH else HandResult.LOSS
		}
		if (premiumResult != null) return premiumResult

		return when {
			dealerHand.isBust || playerHand.total > dealerHand.total -> HandResult.WIN
			playerHand.total == dealerHand.total -> HandResult.PUSH
			else -> HandResult.LOSS
		}
	}
}