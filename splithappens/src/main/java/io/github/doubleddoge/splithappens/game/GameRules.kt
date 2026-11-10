package io.github.doubleddoge.splithappens.game

object GameRules {
	// Hand totals
	const val BLACKJACK_TOTAL = 21
	const val DEALER_STAND_TOTAL = 17

	// Card values
	const val ACE_HIGH_VALUE = 11
	const val ACE_LOW_VALUE = 1
	const val FACE_CARD_VALUE = 10
	const val ACE_DOWNGRADE = ACE_HIGH_VALUE - ACE_LOW_VALUE

	// Hand sizes
	const val INITIAL_HAND_SIZE = 2
	const val NATURAL_CARD_COUNT = INITIAL_HAND_SIZE
	const val TRIPLE_SEVEN_CARD_COUNT = 3
	const val SIX_CARD_COUNT = 6
	const val SEVEN_CARD_COUNT = 7

	// Table rules
	const val DEFAULT_MAX_HANDS = 4
	const val DOUBLE_DOWN_FACTOR = 2
	const val INSURANCE_MAX_BET_DIVISOR = 2
}