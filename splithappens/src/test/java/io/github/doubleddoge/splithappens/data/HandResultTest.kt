package io.github.doubleddoge.splithappens.data

import io.github.doubleddoge.splithappens.data.enum.HandResult
import io.github.doubleddoge.splithappens.data.enum.GameMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HandResultTest {
    @Test
    fun payoutFor_standardResults() {
        assertEquals(0L, HandResult.LOSS.payoutFor(100))
        assertEquals(100L, HandResult.PUSH.payoutFor(100))
        assertEquals(200L, HandResult.WIN.payoutFor(100))
        assertEquals(300L, HandResult.BLACKJACK.payoutFor(100))
    }

    @Test
    fun payoutFor_specialResults() {
        assertEquals(1000L, HandResult.QUEEN_JACK.payoutFor(100))
        assertEquals(5000L, HandResult.NATURAL_BLACKJACK.payoutFor(100))
        assertEquals(10000L, HandResult.TRIPLE_SEVEN.payoutFor(100))
        assertEquals(12000L, HandResult.SIX_CARD.payoutFor(100))
        assertEquals(25000L, HandResult.SEVEN_CARD.payoutFor(100))
    }

    @Test
    fun payoutFor_roundsDownInFavourOfHouse() {
        assertEquals(50L, HandResult.SURRENDER.payoutFor(101)) // 50.5 -> 50
    }

    @Test
    fun payoutFor_zeroBetIsZero() {
        HandResult.entries.forEach { assertEquals(0L, it.payoutFor(0)) }
    }
}

class GameModeTest {
    @Test
    fun wildcard_allowsEveryResult() {
        assertEquals(HandResult.entries.toSet(), GameMode.WILDCARD.allowedResults)
    }

    @Test
    fun traditional_excludesSpecialPayouts() {
        val allowed = GameMode.TRADITIONAL.allowedResults
        assertTrue(HandResult.BLACKJACK in allowed)
        listOf(
            HandResult.QUEEN_JACK, HandResult.NATURAL_BLACKJACK, HandResult.TRIPLE_SEVEN,
            HandResult.SIX_CARD, HandResult.SEVEN_CARD,
        ).forEach { assertFalse(it in allowed) }
    }
}
