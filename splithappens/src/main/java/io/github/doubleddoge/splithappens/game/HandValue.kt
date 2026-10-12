package io.github.doubleddoge.splithappens.game

import io.github.doubleddoge.splithappens.data.enum.Rank

//
//  The classification of a Blackjack hand based on its total and composition.
//
enum class HandType {
    BLACKJACK,
    SOFT,
    HARD,
    BUST,
}

//
//  Calculates and holds the estimated total value, card count, and status of a Blackjack hand,
//  including special hand combinations (e.g., 6/7-card Charlie, Triple 7s, Queen+Ace, Jack+Ace).
//
//  @property cards The list of cards in the hand.
//  @property isSplit True if this hand was created as the result of a split action.
//
data class HandValue(
    val cards: List<Card> = emptyList(),
    val isSplit: Boolean = false,
) {
    val total: Int
    val isSoft: Boolean

    val cardCount: Int get() = cards.size

    val isBust: Boolean get() = total > 21
    val isHard: Boolean get() = total <= 21 && !isSoft
    val isBlackjack: Boolean get() = cardCount == 2 && total == 21 && !isSplit

    // Special hand evaluations matching HandResult conditions
    val isSevenCard: Boolean get() = cardCount >= 7 && !isBust
    val isSixCard: Boolean get() = cardCount == 6 && !isBust
    val isTripleSeven: Boolean get() = cardCount == 3 && total == 21 && cards.all { it.rank == Rank.SEVEN }
    val isQueenAce: Boolean get() = cardCount == 2 && !isSplit && cards.any { it.rank == Rank.QUEEN } && cards.any { it.rank == Rank.ACE }
    val isJackAce: Boolean get() = cardCount == 2 && !isSplit && cards.any { it.rank == Rank.JACK } && cards.any { it.rank == Rank.ACE }

    val type: HandType
        get() = when {
            isBust -> HandType.BUST
            isBlackjack -> HandType.BLACKJACK
            isSoft -> HandType.SOFT
            else -> HandType.HARD
        }

    init {
        var sum = 0
        var softAceCount = 0

        for (card in cards) {
            sum += card.value
            if (card.isAce) {
                softAceCount++
            }
        }

        while (sum > 21 && softAceCount > 0) {
            sum -= 10
            softAceCount--
        }

        total = sum
        isSoft = softAceCount > 0
    }

//
//      Human-readable description of the hand's status and total.
//      Examples: "Blackjack", "Soft 17", "Hard 15", "Bust (24)", "6-Card Hand"
//
    val displayString: String
        get() = when {
            isSevenCard -> "7-Card Hand ($total)"
            isSixCard -> "6-Card Hand ($total)"
            isTripleSeven -> "Triple 7s ($total)"
            type == HandType.BLACKJACK -> "Blackjack"
            type == HandType.BUST -> "Bust ($total)"
            type == HandType.SOFT -> "Soft $total"
            else -> "Hard $total"
        }

    companion object {
        fun of(vararg cards: Card, isSplit: Boolean = false): HandValue =
            HandValue(cards.toList(), isSplit = isSplit)
    }
}

//
//  Extension function to evaluate a list of cards as a Blackjack hand.
//
fun List<Card>.evaluateHand(isSplit: Boolean = false): HandValue =
    HandValue(this, isSplit = isSplit)
