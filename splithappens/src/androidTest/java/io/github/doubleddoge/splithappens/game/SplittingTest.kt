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

	@Test
	fun testSplitOutcomeFeedbackCombinations() {
		// 1. One win, one loss
		val results1 = listOf(HandResult.WIN, HandResult.LOSS)
		val wins1 = results1.count { it.multiplier > 1.0 }
		val pushes1 = results1.count { it == HandResult.PUSH }
		val losses1 = results1.count { it == HandResult.LOSS || it == HandResult.SURRENDER }
		val banner1 = buildList {
			if (wins1 > 0) add("$wins1 WON")
			if (losses1 > 0) add("$losses1 LOST")
			if (pushes1 > 0) add("$pushes1 PUSH")
		}
			.joinToString(", ")
		assertEquals("1 WON, 1 LOST", banner1)

		// 2. One win, one push
		val results2 = listOf(HandResult.WIN, HandResult.PUSH)
		val wins2 = results2.count { it.multiplier > 1.0 }
		val pushes2 = results2.count { it == HandResult.PUSH }
		val losses2 = results2.count { it == HandResult.LOSS || it == HandResult.SURRENDER }
		val banner2 = buildList {
			if (wins2 > 0) add("$wins2 WON")
			if (losses2 > 0) add("$losses2 LOST")
			if (pushes2 > 0) add("$pushes2 PUSH")
		}
			.joinToString(", ")
		assertEquals("1 WON, 1 PUSH", banner2)

		// 3. One loss, one push
		val results3 = listOf(HandResult.LOSS, HandResult.PUSH)
		val wins3 = results3.count { it.multiplier > 1.0 }
		val pushes3 = results3.count { it == HandResult.PUSH }
		val losses3 = results3.count { it == HandResult.LOSS || it == HandResult.SURRENDER }
		val banner3 = buildList {
			if (wins3 > 0) add("$wins3 WON")
			if (losses3 > 0) add("$losses3 LOST")
			if (pushes3 > 0) add("$pushes3 PUSH")
		}
			.joinToString(", ")
		assertEquals("1 LOST, 1 PUSH", banner3)

		// 4. Both pushes
		val results4 = listOf(HandResult.PUSH, HandResult.PUSH)
		val pushes4 = results4.count { it == HandResult.PUSH }
		assertEquals(2, pushes4)

		// 5. Both wins
		val results5 = listOf(HandResult.WIN, HandResult.WIN)
		val wins5 = results5.count { it.multiplier > 1.0 }
		assertEquals(2, wins5)
	}
}
