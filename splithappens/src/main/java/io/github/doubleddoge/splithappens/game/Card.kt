package io.github.doubleddoge.splithappens.game

import io.github.doubleddoge.splithappens.data.entity.CardEntity
import io.github.doubleddoge.splithappens.data.enum.Suit
import io.github.doubleddoge.splithappens.data.enum.Rank

data class Card(val suit:Suit, val rank: Rank) {
    val cardId: Int get() = CardEntity.idOf(suit, rank)

    val cardValue : Int get () = when (rank) {
        Rank.ACE -> 11
        Rank.JACK, Rank.QUEEN, Rank.KING -> 10
        else -> rank.ordinal + 1
    }
    val isAce get() = rank == Rank.ACE

}
fun CardEntity.toCard()= Card(suit, rank)