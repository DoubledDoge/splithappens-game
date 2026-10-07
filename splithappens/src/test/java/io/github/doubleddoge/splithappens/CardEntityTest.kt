package io.github.doubleddoge.splithappens.data.entity

import io.github.doubleddoge.splithappens.data.enum.Rank
import io.github.doubleddoge.splithappens.data.enum.Suit
import org.junit.Assert.assertEquals
import org.junit.Test

class CardEntityTest {
    @Test
    fun idOf_firstAndLastCard() {
        assertEquals(0, CardEntity.idOf(Suit.CLUBS, Rank.ACE))
        assertEquals(51, CardEntity.idOf(Suit.SPADES, Rank.KING))
    }

    @Test
    fun idOf_followsSuitTimes13PlusRank() {
        assertEquals(13, CardEntity.idOf(Suit.DIAMONDS, Rank.ACE))
        assertEquals(2 * 13 + Rank.SEVEN.ordinal, CardEntity.idOf(Suit.HEARTS, Rank.SEVEN))
    }

    @Test
    fun standardDeck_has52Cards() {
        assertEquals(52, CardEntity.standardDeck().size)
    }

    @Test
    fun standardDeck_idsAreContiguous0To51() {
        assertEquals((0..51).toList(), CardEntity.standardDeck().map { it.cardId }.sorted())
    }

    @Test
    fun standardDeck_everySuitRankPairAppearsOnce() {
        val pairs = CardEntity.standardDeck().map { it.suit to it.rank }
        assertEquals(52, pairs.toSet().size)
    }

    @Test
    fun standardDeck_idMatchesIdOf() {
        CardEntity.standardDeck().forEach {
            assertEquals(CardEntity.idOf(it.suit, it.rank), it.cardId)
        }
    }
}