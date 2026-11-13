package io.github.doubleddoge.splithappens.game

import io.github.doubleddoge.splithappens.game.model.GameMode
import io.github.doubleddoge.splithappens.game.model.HandResult
import io.github.doubleddoge.splithappens.game.model.Rank
import io.github.doubleddoge.splithappens.game.model.Suit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HandEvaluatorTest {
	private fun hand(vararg ranks: Rank) = ranks.map { Card(Suit.SPADES, it) }

	private fun settle(
		player: List<Card>,
		dealer: List<Card>,
		mode: GameMode = GameMode.WILDCARD,
		afterSplit: Boolean = false,
		surrendered: Boolean = false,
	) = HandEvaluator.settle(player, afterSplit, surrendered, dealer, mode)

	private fun premium(cards: List<Card>, mode: GameMode, afterSplit: Boolean = false) =
		HandEvaluator.premium(cards, afterSplit, mode)

	@Test
	fun premium_bustHasNone() {
		assertNull(premium(hand(Rank.TEN, Rank.SIX, Rank.QUEEN), GameMode.WILDCARD))
	}

	@Test
	fun premium_queenAce_wildcardPaysQueenJack_traditionalPaysBlackjack() {
		val cards = hand(Rank.QUEEN, Rank.ACE)
		assertEquals(HandResult.QUEEN_JACK, premium(cards, GameMode.WILDCARD))
		assertEquals(HandResult.BLACKJACK, premium(cards, GameMode.TRADITIONAL))
	}

	@Test
	fun premium_jackAce_wildcardPaysNaturalBlackjack() {
		assertEquals(
			HandResult.NATURAL_BLACKJACK,
			premium(hand(Rank.JACK, Rank.ACE), GameMode.WILDCARD),
		)
	}

	@Test
	fun premium_splitAceTen_isNotPremium() {
		assertNull(premium(hand(Rank.ACE, Rank.TEN), GameMode.WILDCARD, afterSplit = true))
	}

	@Test
	fun premium_tripleSeven_wildcardOnly() {
		val cards = hand(Rank.SEVEN, Rank.SEVEN, Rank.SEVEN)
		assertEquals(HandResult.TRIPLE_SEVEN, premium(cards, GameMode.WILDCARD))
		assertNull(premium(cards, GameMode.TRADITIONAL))
	}

	@Test
	fun premium_sixAndSevenCardHands_wildcardOnly() {
		val six = hand(Rank.TWO, Rank.TWO, Rank.TWO, Rank.TWO, Rank.THREE, Rank.THREE)
		val seven = six + hand(Rank.ACE)
		assertEquals(HandResult.SIX_CARD, premium(six, GameMode.WILDCARD))
		assertEquals(HandResult.SEVEN_CARD, premium(seven, GameMode.WILDCARD))
		assertNull(premium(six, GameMode.TRADITIONAL))
	}

	@Test
	fun settle_surrenderOverridesEverything() {
		val result = settle(hand(Rank.TEN, Rank.SEVEN), hand(Rank.TEN, Rank.NINE), surrendered = true)
		assertEquals(HandResult.SURRENDER, result)
	}

	@Test
	fun settle_playerBustLosesEvenIfDealerBusts() {
		val result = settle(
			hand(Rank.TEN, Rank.SIX, Rank.QUEEN),
			hand(Rank.TEN, Rank.SIX, Rank.KING),
		)
		assertEquals(HandResult.LOSS, result)
	}

	@Test
	fun settle_comparesTotals() {
		assertEquals(HandResult.WIN, settle(hand(Rank.TEN, Rank.KING), hand(Rank.TEN, Rank.EIGHT)))
		assertEquals(HandResult.PUSH, settle(hand(Rank.TEN, Rank.EIGHT), hand(Rank.NINE, Rank.NINE)))
		assertEquals(HandResult.LOSS, settle(hand(Rank.TEN, Rank.SEVEN), hand(Rank.TEN, Rank.NINE)))
		assertEquals(
			HandResult.WIN,
			settle(hand(Rank.TEN, Rank.TWO), hand(Rank.TEN, Rank.SIX, Rank.KING)),
		)
	}

	@Test
	fun settle_dealerBlackjack_pushesAgainstNaturalsOnly() {
		val dealer = hand(Rank.ACE, Rank.KING)
		assertEquals(HandResult.PUSH, settle(hand(Rank.QUEEN, Rank.ACE), dealer))
		assertEquals(HandResult.PUSH, settle(hand(Rank.TEN, Rank.ACE), dealer, GameMode.TRADITIONAL))
		assertEquals(HandResult.LOSS, settle(hand(Rank.SEVEN, Rank.SEVEN, Rank.SEVEN), dealer))
	}

	@Test
	fun settle_premiumBeatsHigherDealerTotal() {
		val result = settle(hand(Rank.QUEEN, Rank.ACE), hand(Rank.TEN, Rank.KING))
		assertEquals(HandResult.QUEEN_JACK, result)
	}
}