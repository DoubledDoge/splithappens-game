package io.github.doubleddoge.splithappens.game

import io.github.doubleddoge.splithappens.data.entity.CardEntity
import io.github.doubleddoge.splithappens.data.enum.Rank
import io.github.doubleddoge.splithappens.data.enum.Suit

data class Card(val suit: Suit, val rank: Rank) {
	val cardId: Int get() = CardEntity.idOf(suit, rank)

	val value: Int
		get() = when (rank) {
			Rank.ACE -> GameRules.ACE_HIGH_VALUE
			Rank.JACK, Rank.QUEEN, Rank.KING -> GameRules.FACE_CARD_VALUE
			else -> rank.ordinal + 1 // TWO to TEN follow ACE in declaration order
		}

	val isAce: Boolean get() = rank == Rank.ACE
}