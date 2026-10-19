package io.github.doubleddoge.splithappens.game

import io.github.doubleddoge.splithappens.data.enum.Rank
import io.github.doubleddoge.splithappens.data.enum.Suit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HandValueTest {

    private val aceClubs = Card(Suit.CLUBS, Rank.ACE)
    private val aceSpades = Card(Suit.SPADES, Rank.ACE)
    private val aceHearts = Card(Suit.HEARTS, Rank.ACE)
    private val kingDiamonds = Card(Suit.DIAMONDS, Rank.KING)
    private val queenHearts = Card(Suit.HEARTS, Rank.QUEEN)
    private val jackSpades = Card(Suit.SPADES, Rank.JACK)
    private val tenClubs = Card(Suit.CLUBS, Rank.TEN)
    private val nineHearts = Card(Suit.HEARTS, Rank.NINE)
    private val sevenClubs = Card(Suit.CLUBS, Rank.SEVEN)
    private val sevenDiamonds = Card(Suit.DIAMONDS, Rank.SEVEN)
    private val sevenHearts = Card(Suit.HEARTS, Rank.SEVEN)
    private val sixDiamonds = Card(Suit.DIAMONDS, Rank.SIX)
    private val twoClubs = Card(Suit.CLUBS, Rank.TWO)
    private val twoSpades = Card(Suit.SPADES, Rank.TWO)
    private val twoHearts = Card(Suit.HEARTS, Rank.TWO)
    private val twoDiamonds = Card(Suit.DIAMONDS, Rank.TWO)
    private val threeClubs = Card(Suit.CLUBS, Rank.THREE)
    private val threeSpades = Card(Suit.SPADES, Rank.THREE)

    @Test
    fun testEmptyHand() {
        val hand = HandValue()
        assertEquals(0, hand.total)
        assertEquals(0, hand.cardCount)
        assertFalse(hand.isSoft)
        assertTrue(hand.isHard)
        assertFalse(hand.isBust)
        assertFalse(hand.isBlackjack)
        assertEquals(HandType.HARD, hand.type)
        assertEquals("Hard 0", hand.displayString)
    }

    @Test
    fun testNaturalBlackjack() {
        val hand = HandValue.of(aceClubs, kingDiamonds)
        assertEquals(21, hand.total)
        assertEquals(2, hand.cardCount)
        assertTrue(hand.isSoft)
        assertFalse(hand.isBust)
        assertTrue(hand.isBlackjack)
        assertEquals(HandType.BLACKJACK, hand.type)
        assertEquals("Blackjack", hand.displayString)
    }

    @Test
    fun testQueenAceAndJackAce() {
        val queenAceHand = HandValue.of(queenHearts, aceClubs)
        assertTrue(queenAceHand.isQueenAce)
        assertFalse(queenAceHand.isJackAce)

        val jackAceHand = HandValue.of(jackSpades, aceClubs)
        assertTrue(jackAceHand.isJackAce)
        assertFalse(jackAceHand.isQueenAce)
    }

    @Test
    fun testSplitHandIs21NotBlackjack() {
        val hand = HandValue.of(aceClubs, tenClubs, isSplit = true)
        assertEquals(21, hand.total)
        assertTrue(hand.isSoft)
        assertFalse(hand.isBlackjack)
        assertEquals(HandType.SOFT, hand.type)
        assertEquals("Soft 21", hand.displayString)
    }

    @Test
    fun testThreeCard21AndTripleSeven() {
        val hand = HandValue.of(sevenClubs, sevenDiamonds, sevenHearts)
        assertEquals(21, hand.total)
        assertEquals(3, hand.cardCount)
        assertTrue(hand.isTripleSeven)
        assertFalse(hand.isBlackjack)
        assertEquals("Triple 7s (21)", hand.displayString)
    }

    @Test
    fun testSoftHand() {
        val hand = HandValue.of(aceClubs, sixDiamonds)
        assertEquals(17, hand.total)
        assertTrue(hand.isSoft)
        assertFalse(hand.isHard)
        assertFalse(hand.isBust)
        assertEquals(HandType.SOFT, hand.type)
        assertEquals("Soft 17", hand.displayString)
    }

    @Test
    fun testHardHandWhenAceConvertedToOne() {
        val hand = HandValue.of(aceClubs, sixDiamonds, tenClubs)
        assertEquals(17, hand.total)
        assertFalse(hand.isSoft)
        assertTrue(hand.isHard)
        assertFalse(hand.isBust)
        assertEquals(HandType.HARD, hand.type)
        assertEquals("Hard 17", hand.displayString)
    }

    @Test
    fun testMultipleAcesSoftHand() {
        val hand = HandValue.of(aceClubs, aceSpades)
        assertEquals(12, hand.total)
        assertTrue(hand.isSoft)
        assertEquals(HandType.SOFT, hand.type)
        assertEquals("Soft 12", hand.displayString)
    }

    @Test
    fun testMultipleAcesWithNine() {
        val hand = HandValue.of(aceClubs, aceSpades, nineHearts)
        assertEquals(21, hand.total)
        assertTrue(hand.isSoft)
        assertFalse(hand.isBlackjack)
        assertEquals(HandType.SOFT, hand.type)
        assertEquals("Soft 21", hand.displayString)
    }

    @Test
    fun testThreeAcesAndTen() {
        val hand = HandValue.of(aceClubs, aceSpades, aceHearts, tenClubs)
        assertEquals(13, hand.total)
        assertFalse(hand.isSoft)
        assertTrue(hand.isHard)
        assertEquals(HandType.HARD, hand.type)
        assertEquals("Hard 13", hand.displayString)
    }

    @Test
    fun testSixCardUnbustedHand() {
        // 2 + 2 + 2 + 2 + 3 + 3 = 14 (6 cards without busting)
        val hand = HandValue.of(twoClubs, twoSpades, twoHearts, twoDiamonds, threeClubs, threeSpades)
        assertEquals(14, hand.total)
        assertEquals(6, hand.cardCount)
        assertTrue(hand.isSixCard)
        assertFalse(hand.isSevenCard)
        assertFalse(hand.isBust)
        assertEquals("6-Card Hand (14)", hand.displayString)
    }

    @Test
    fun testSevenCardUnbustedHand() {
        // 2 + 2 + 2 + 2 + 3 + 3 + Ace = 15 (7 cards without busting)
        val hand = HandValue.of(twoClubs, twoSpades, twoHearts, twoDiamonds, threeClubs, threeSpades, aceClubs)
        assertEquals(15, hand.total)
        assertEquals(7, hand.cardCount)
        assertTrue(hand.isSevenCard)
        assertFalse(hand.isBust)
        assertEquals("7-Card Hand (15)", hand.displayString)
    }

    @Test
    fun testBustHand() {
        val hand = HandValue.of(tenClubs, sixDiamonds, queenHearts)
        assertEquals(26, hand.total)
        assertFalse(hand.isSoft)
        assertFalse(hand.isHard)
        assertTrue(hand.isBust)
        assertEquals(HandType.BUST, hand.type)
        assertEquals("Bust (26)", hand.displayString)
    }

    @Test
    fun testExtensionFunctionEvaluateHand() {
        val cards = listOf(aceClubs, queenHearts)
        val hand = cards.evaluateHand()
        assertEquals(21, hand.total)
        assertTrue(hand.isBlackjack)
    }
}
