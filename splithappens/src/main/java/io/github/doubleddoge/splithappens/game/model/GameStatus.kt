package io.github.doubleddoge.splithappens.game.model

import io.github.doubleddoge.splithappens.util.DailyReward
const val BALANCE_THRESHOLD = 100L

internal object GameStatus {
	const val PLACE_BETS = "Place your bets"
	const val NOT_ENOUGH_CHIPS = "Not enough chips!"
	const val PLACE_A_BET = "Please place a bet"
	const val DEALING = "Dealing..."
	const val INSURANCE_OFFERED = "Insurance Offered"
	const val HIT_OR_STAND = "Hit or Stand?"
	const val INSURANCE_UNAFFORDABLE = "Not enough chips for insurance!"
	const val DEALER_DRAWING = "Dealer drawing..."
	const val LOW_BALANCE =
		"Balance below R$BALANCE_THRESHOLD minimum. Daily reward gives R${DailyReward.AMOUNT} every 24h!"
}