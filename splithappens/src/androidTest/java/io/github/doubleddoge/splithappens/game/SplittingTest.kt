package io.github.doubleddoge.splithappens.game

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.doubleddoge.splithappens.data.DatabaseTest
import io.github.doubleddoge.splithappens.data.entity.CardEntity
import io.github.doubleddoge.splithappens.game.model.GameMode
import io.github.doubleddoge.splithappens.game.model.HandResult
import io.github.doubleddoge.splithappens.game.model.Rank
import io.github.doubleddoge.splithappens.game.model.Suit
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SplittingTest : DatabaseTest() {

	@Before
	fun setupCards() = runBlocking {
		db.cardDao().insertAll(CardEntity.standardDeck())
	}

	@Test
	fun testSplitHandIsNotNaturalBlackjack() {
		val aceCard = Card(suit = Suit.SPADES, rank = Rank.ACE)
		val tenCard = Card(suit = Suit.HEARTS, rank = Rank.TEN)

		val normalHand = HandValue(listOf(aceCard, tenCard), isSplit = false)
		assertTrue("Normal 21 with Ace+10 is natural blackjack", normalHand.isBlackjack)

		val splitHand = HandValue(listOf(aceCard, tenCard), isSplit = true)
		assertFalse("Split 21 with Ace+10 is NOT natural blackjack", splitHand.isBlackjack)
		assertEquals(21, splitHand.total)
	}

	@Test
	fun testSplitSettlementAgainstDealerBlackjack() {
		val aceCard = Card(suit = Suit.SPADES, rank = Rank.ACE)
		val tenCard = Card(suit = Suit.HEARTS, rank = Rank.TEN)
		val kingCard = Card(suit = Suit.CLUBS, rank = Rank.KING)

		val dealerCards = listOf(aceCard, kingCard)

		val splitPlayerCards = listOf(aceCard, tenCard)
		val result =
			HandEvaluator.settle(
				player = splitPlayerCards,
				afterSplit = true,
				surrendered = false,
				dealer = dealerCards,
				mode = GameMode.TRADITIONAL,
			)

		assertEquals(HandResult.LOSS, result)
	}

	@Test
	fun testMaxAllowedSplitsAndHands() = runBlocking {
		val shoe = Shoe(deckCount = 1)
		val round =
			Round(
				shoe = shoe,
				mode = GameMode.TRADITIONAL,
				bankroll = 5000L,
				minBet = 50L,
				maxHands = 4,
			)

		round.startBetting(100L)
		assertEquals(1, round.hands.size)
	}
}
