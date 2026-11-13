package io.github.doubleddoge.splithappens.game

import io.github.doubleddoge.splithappens.game.model.HandResult
import kotlin.collections.firstOrNull

fun outcomeMessage(results: List<HandResult>, firstHandBusted: Boolean): String {
	if (results.size > 1) return splitOutcomeMessage(results)
	return when (results.firstOrNull()) {
		HandResult.BLACKJACK, HandResult.NATURAL_BLACKJACK -> "BLACKJACK! YOU WIN!"
		HandResult.QUEEN_JACK -> "QUEEN JACK! YOU WIN!"
		HandResult.TRIPLE_SEVEN -> "7-7-7! YOU WIN!"
		HandResult.SIX_CARD -> "6-CARD CHARLIE! YOU WIN!"
		HandResult.SEVEN_CARD -> "7-CARD CHARLIE! YOU WIN!"
		HandResult.WIN -> "YOU WIN!"
		HandResult.PUSH -> "PUSH - BET RETURNED"
		HandResult.LOSS -> if (firstHandBusted) "BUSTED!" else "DEALER WINS"
		HandResult.SURRENDER -> "SURRENDERED"
		null -> "ROUND OVER"
	}
}

private fun splitOutcomeMessage(results: List<HandResult>): String {
	val wins = results.count { it.isWin }
	val pushes = results.count { it == HandResult.PUSH }
	val losses = results.count { it.isLoss }
	return when {
		wins == results.size -> "ALL HANDS WON!"
		losses == results.size -> "ALL HANDS LOST"
		pushes == results.size -> "ALL HANDS PUSHED"
		else -> "SPLIT: " + buildList {
			if (wins > 0) add("$wins WON")
			if (losses > 0) add("$losses LOST")
			if (pushes > 0) add("$pushes PUSH")
		}.joinToString(", ")
	}
}