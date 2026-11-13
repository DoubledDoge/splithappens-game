package io.github.doubleddoge.splithappens.game

import io.github.doubleddoge.splithappens.game.model.HandStatus
import io.github.doubleddoge.splithappens.data.enum.HandType
import io.github.doubleddoge.splithappens.game.model.Rank

/**
 * Calculates and holds the estimated total value, card count, and status of a Blackjack hand,
 * including special hand combinations (e.g., 6/7-card Charlie, Triple 7s, Queen+Ace, Jack+Ace).
 *
 * @property cards The list of cards in the hand.
 * @property isSplit True if this hand was created as the result of a split action.
 */
data class HandValue(
	val cards: List<Card> = emptyList(),
	val isSplit: Boolean = false,
) {
	val total: Int
	val isSoft: Boolean

	val cardCount: Int get() = cards.size

	val isBust: Boolean get() = total > GameRules.BLACKJACK_TOTAL
	val isHard: Boolean get() = !isBust && !isSoft
	val isBlackjack: Boolean
		get() = cardCount == GameRules.NATURAL_CARD_COUNT &&
				total == GameRules.BLACKJACK_TOTAL && !isSplit

	// Special hand evaluations matching HandResult conditions
	val isSevenCard: Boolean get() = cardCount >= GameRules.SEVEN_CARD_COUNT && !isBust
	val isSixCard: Boolean get() = cardCount == GameRules.SIX_CARD_COUNT && !isBust
	val isTripleSeven: Boolean
		get() = cardCount == GameRules.TRIPLE_SEVEN_CARD_COUNT && cards.all { it.rank == Rank.SEVEN }
	val isQueenAce: Boolean get() = isNaturalPair(Rank.QUEEN)
	val isJackAce: Boolean get() = isNaturalPair(Rank.JACK)

	val type: HandType
		get() = when {
			isBust -> HandType.BUST
			isBlackjack -> HandType.BLACKJACK
			isSoft -> HandType.SOFT
			else -> HandType.HARD
		}

	init {
		var sum = cards.sumOf { it.value }
		var softAceCount = cards.count { it.isAce }

		while (sum > GameRules.BLACKJACK_TOTAL && softAceCount > 0) {
			sum -= GameRules.ACE_DOWNGRADE
			softAceCount--
		}

		total = sum
		isSoft = softAceCount > 0
	}

	private fun isNaturalPair(first: Rank): Boolean =
		cardCount == GameRules.NATURAL_CARD_COUNT && !isSplit &&
				cards.any { it.rank == first } && cards.any { it.rank == Rank.ACE }

	val displayString: String
		get() = when {
			isSevenCard -> "${GameRules.SEVEN_CARD_COUNT}-Card Hand ($total)"
			isSixCard -> "${GameRules.SIX_CARD_COUNT}-Card Hand ($total)"
			isTripleSeven -> "Triple 7s ($total)"
			type == HandType.BLACKJACK -> "Blackjack"
			type == HandType.BUST -> "Bust ($total)"
			type == HandType.SOFT -> "Soft $total"
			else -> "Hard $total"
		}

	fun toHandStatus(): HandStatus = when {
		isBust -> HandStatus.BUSTED
		isBlackjack -> HandStatus.BLACKJACK
		else -> HandStatus.STOOD
	}

	companion object {
		fun of(vararg cards: Card, isSplit: Boolean = false): HandValue =
			HandValue(cards.toList(), isSplit = isSplit)
	}
}

// Extension function to evaluate a list of cards as a Blackjack hand.
fun List<Card>.evaluateHand(isSplit: Boolean = false): HandValue =
	HandValue(this, isSplit = isSplit)
