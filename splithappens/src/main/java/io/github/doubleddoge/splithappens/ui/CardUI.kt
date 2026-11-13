package io.github.doubleddoge.splithappens.ui

import io.github.doubleddoge.splithappens.data.entity.CardEntity
import io.github.doubleddoge.splithappens.game.GameRules
import io.github.doubleddoge.splithappens.game.GameRules.ACE_HIGH_VALUE
import io.github.doubleddoge.splithappens.game.GameRules.CUT_CARD_FRACTION
import io.github.doubleddoge.splithappens.game.GameRules.DEFAULT_NUM_DECKS
import io.github.doubleddoge.splithappens.game.GameRules.FACE_CARD_VALUE
import io.github.doubleddoge.splithappens.game.GameRules.NUM_CARDS
import io.github.doubleddoge.splithappens.game.GameRules.RANK_ORDINAL_OFFSET
import io.github.doubleddoge.splithappens.game.model.Rank
import io.github.doubleddoge.splithappens.game.model.Suit

val Suit.symbol: String
	get() =
		when (this) {
			Suit.CLUBS -> "♣"
			Suit.DIAMONDS -> "♦"
			Suit.HEARTS -> "♥"
			Suit.SPADES -> "♠"
		}

val Suit.isRed: Boolean
	get() = (this == Suit.DIAMONDS) || (this == Suit.HEARTS)

// ACE is ordinal 0, so TWO..TEN are ordinal + 1
val Rank.label: String
	get() =
		when (this) {
			Rank.ACE -> "A"
			Rank.JACK -> "J"
			Rank.QUEEN -> "Q"
			Rank.KING -> "K"
			else -> (ordinal + RANK_ORDINAL_OFFSET).toString()
		}

val Rank.blackjackValue: Int
	get() =
		when (this) {
			Rank.ACE -> ACE_HIGH_VALUE
			Rank.JACK,
			Rank.QUEEN,
			Rank.KING,
				-> FACE_CARD_VALUE

			else -> ordinal + RANK_ORDINAL_OFFSET
		}

/**
 * A card on the table. [uid] is unique per physical card in the shoe, so it is a stable Compose
 * key.
 */
data class TableCard(
	val uid: Long,
	val suit: Suit,
	val rank: Rank,
	val isFaceUp: Boolean = true,
) {
	val value: Int
		get() = rank.blackjackValue

	val cardId: Int
		get() = CardEntity.idOf(suit, rank) // maps straight to HandCardEntity.cardId
}

class Shoe(private val decks: Int = DEFAULT_NUM_DECKS) {

	private val cards = ArrayDeque<TableCard>()
	private var nextUid = 0L
	private var cutCardAt = 0

	init {
		shuffle()
	}

	fun shuffle() {
		cards.clear()
		repeat(decks) {
			for (suit in Suit.entries) for (rank in Rank.entries) {
				cards.add(TableCard(nextUid++, suit, rank))
			}
		}
		cards.shuffle()
		cutCardAt =
			(decks * NUM_CARDS) / CUT_CARD_FRACTION // reshuffle when a quarter of the shoe is left
	}

	fun draw(faceUp: Boolean = true): TableCard {
		if (cards.isEmpty()) shuffle()
		return cards.removeFirst().copy(isFaceUp = faceUp)
	}
}
