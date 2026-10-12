package io.github.doubleddoge.splithappens.data

import io.github.doubleddoge.splithappens.data.entity.CardEntity
import io.github.doubleddoge.splithappens.data.enum.Rank
import io.github.doubleddoge.splithappens.data.enum.Suit
import org.junit.Assert
import org.junit.Test

class CardEntityTest {
    @Test
    fun idOf_firstAndLastCard() {
        Assert.assertEquals(0, CardEntity.idOf(Suit.CLUBS, Rank.ACE))
        Assert.assertEquals(51, CardEntity.idOf(Suit.SPADES, Rank.KING))
    }

    @Test
    fun idOf_followsSuitTimes13PlusRank() {
        Assert.assertEquals(13, CardEntity.idOf(Suit.DIAMONDS, Rank.ACE))
        Assert.assertEquals(2 * 13 + Rank.SEVEN.ordinal, CardEntity.idOf(Suit.HEARTS, Rank.SEVEN))
    }

    @Test
    fun standardDeck_has52Cards() {
        Assert.assertEquals(52, CardEntity.standardDeck().size)
    }

    @Test
    fun standardDeck_idsAreContiguous0To51() {
        Assert.assertEquals((0..51).toList(), CardEntity.standardDeck().map { it.cardId }.sorted())
    }

    @Test
    fun standardDeck_everySuitRankPairAppearsOnce() {
        val pairs = CardEntity.standardDeck().map { it.suit to it.rank }
        Assert.assertEquals(52, pairs.toSet().size)
    }

    @Test
    fun standardDeck_idMatchesIdOf() {
        CardEntity.standardDeck().forEach {
            Assert.assertEquals(CardEntity.idOf(it.suit, it.rank), it.cardId)
        }
    }
}