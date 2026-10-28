package io.github.doubleddoge.splithappens.data

import io.github.doubleddoge.splithappens.data.entity.SessionEntity
import io.github.doubleddoge.splithappens.data.enum.GameMode
import io.github.doubleddoge.splithappens.data.enum.HandResult
import io.github.doubleddoge.splithappens.data.enum.SessionStatus
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionEntityTest {
    @Test
    fun defaults_areWildcardOngoingAndNotEnded() {
        val before = System.currentTimeMillis()
        val s = SessionEntity(userId = "u", stakes = 100)
        Assert.assertEquals(0L, s.sessionId)
        assertEquals(GameMode.WILDCARD, s.mode)
        assertEquals(SessionStatus.ONGOING, s.status)
        Assert.assertNull(s.endedAt)
        assertTrue(s.startedAt >= before)
    }

    @Test
    fun testWildcardSpecialPayoutsAndModeRestriction() {
        val bet = 100L

        val wildcardAllowed = GameMode.WILDCARD.allowedResults
        assertTrue(HandResult.QUEEN_JACK in wildcardAllowed)
        assertTrue(HandResult.TRIPLE_SEVEN in wildcardAllowed)
        assertTrue(HandResult.NATURAL_BLACKJACK in wildcardAllowed)

        assertEquals(1000L, HandResult.QUEEN_JACK.payoutFor(bet))
        assertEquals(10000L, HandResult.TRIPLE_SEVEN.payoutFor(bet))

        val traditionalAllowed = GameMode.TRADITIONAL.allowedResults
        assertTrue(HandResult.BLACKJACK in traditionalAllowed)
        assertFalse(HandResult.QUEEN_JACK in traditionalAllowed)
        assertFalse(HandResult.TRIPLE_SEVEN in traditionalAllowed)
        assertFalse(HandResult.NATURAL_BLACKJACK in traditionalAllowed)
    }
}