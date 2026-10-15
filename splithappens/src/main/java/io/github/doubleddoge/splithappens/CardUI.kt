package io.github.doubleddoge.splithappens

import io.github.doubleddoge.splithappens.data.entity.CardEntity
import io.github.doubleddoge.splithappens.data.enum.Rank
import io.github.doubleddoge.splithappens.data.enum.Suit

val Suit.symbol: String
    get() = when (this) {
        Suit.CLUBS -> "♣"
        Suit.DIAMONDS -> "♦"
        Suit.HEARTS -> "♥"
        Suit.SPADES -> "♠"
    }

val Suit.isRed: Boolean get() = this == Suit.DIAMONDS || this == Suit.HEARTS

// ACE is ordinal 0, so TWO..TEN are ordinal + 1
val Rank.label: String
    get() = when (this) {
        Rank.ACE -> "A"
        Rank.JACK -> "J"
        Rank.QUEEN -> "Q"
        Rank.KING -> "K"
        else -> (ordinal + 1).toString()
    }

val Rank.blackjackValue: Int
    get() = when (this) {
        Rank.ACE -> 11
        Rank.JACK, Rank.QUEEN, Rank.KING -> 10
        else -> ordinal + 1
    }

/** A card on the table. [uid] is unique per physical card in the shoe, so it is a stable Compose key. */
data class TableCard(
    val uid: Long,
    val suit: Suit,
    val rank: Rank,
    val isFaceUp: Boolean = true,
) {
    val value: Int get() = rank.blackjackValue
    val cardId: Int get() = CardEntity.idOf(suit, rank) // maps straight to HandCardEntity.cardId
}

/** Returns (total, isSoft). Hidden cards are ignored unless [includeHidden] is true. */
fun calculateHandValue(cards: List<TableCard>, includeHidden: Boolean = false): Pair<Int, Boolean> {
    val counted = if (includeHidden) cards else cards.filter { it.isFaceUp }
    var sum = counted.sumOf { it.value }
    var aces = counted.count { it.rank == Rank.ACE }
    while (sum > 21 && aces > 0) {
        sum -= 10
        aces--
    }
    return sum to (aces > 0)
}

class Shoe(private val decks: Int = 6) {
    private val cards = ArrayDeque<TableCard>()
    private var nextUid = 0L
    private var cutCardAt = 0

    init { shuffle() }

    fun shuffle() {
        cards.clear()
        repeat(decks) {
            for (suit in Suit.entries) for (rank in Rank.entries) {
                cards.add(TableCard(nextUid++, suit, rank))
            }
        }
        cards.shuffle()
        cutCardAt = decks * 52 / 4 // reshuffle when a quarter of the shoe is left
    }

    /** Call between rounds only, so cards in play are never duplicated. */
    fun shuffleIfNeeded() {
        if (cards.size < cutCardAt) shuffle()
    }

    fun draw(faceUp: Boolean = true): TableCard {
        if (cards.isEmpty()) shuffle()
        return cards.removeFirst().copy(isFaceUp = faceUp)
    }
}