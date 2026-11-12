package io.github.doubleddoge.splithappens.game

import io.github.doubleddoge.splithappens.data.enum.Action
import io.github.doubleddoge.splithappens.game.enum.GameMode
import io.github.doubleddoge.splithappens.game.enum.HandResult
import io.github.doubleddoge.splithappens.data.enum.Phase
import io.github.doubleddoge.splithappens.game.enum.Rank
import io.github.doubleddoge.splithappens.game.enum.Suit
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class RoundTest {
	// [deal] comes out in order
	private fun roundWith(
		vararg deal: Rank,
		bankroll: Long = 1000L,
		autoStandOn21: Boolean = true,
	): Round {
		val shoe = Shoe(random = Random(1)).apply {
			stackTop(deal.map { Card(Suit.SPADES, it) })
		}
		return Round(shoe, GameMode.WILDCARD, bankroll, minBet = 10L, autoStandOn21 = autoStandOn21)
	}

	@Test
	fun dealerStandsOnSoft17() {
		val round = roundWith(Rank.TEN, Rank.SIX, Rank.EIGHT, Rank.ACE) // dealer 6 up, A hole
		round.placeBet(10)
		round.act(Action.STAND)
		assertEquals(2, round.dealerCards.size)
		assertEquals(listOf(HandResult.WIN), round.results())
	}

	@Test
	fun dealerDrawsOn16() {
		val round = roundWith(Rank.TEN, Rank.SIX, Rank.EIGHT, Rank.TEN, Rank.NINE)
		round.placeBet(10)
		round.act(Action.STAND)
		assertEquals(3, round.dealerCards.size) // 16 + 9 = bust
		assertEquals(listOf(HandResult.WIN), round.results())
	}

	@Test
	fun insuranceIsOfferedOnAceUpcard() {
		val round = roundWith(Rank.TEN, Rank.ACE, Rank.NINE, Rank.FIVE)
		round.placeBet(10)
		assertEquals(Phase.INSURANCE, round.phase)
		assertTrue(round.legalActions().isEmpty())
	}

	@Test
	fun insuranceIsCappedAtHalfTheBetRoundedDown() {
		val round = roundWith(Rank.TEN, Rank.ACE, Rank.NINE, Rank.FIVE)
		round.placeBet(51)
		assertThrows(IllegalArgumentException::class.java) { round.resolveInsurance(26) }
		round.resolveInsurance(25)
		assertEquals(25L, round.insuranceBet)
	}

	@Test
	fun insuranceYouCannotAffordIsRejected() {
		val round = roundWith(Rank.TEN, Rank.ACE, Rank.NINE, Rank.FIVE, bankroll = 50L)
		round.placeBet(50) // available chips = 0
		assertThrows(IllegalArgumentException::class.java) { round.resolveInsurance(25) }
	}

	@Test
	fun split_dealsToOriginalHandBeforeNewHand() {
		val round = roundWith(Rank.EIGHT, Rank.SIX, Rank.EIGHT, Rank.TEN, Rank.THREE, Rank.FOUR)
		round.placeBet(10)
		round.act(Action.SPLIT)
		assertEquals(listOf(Rank.EIGHT, Rank.THREE), round.hands[0].cards.map { it.rank })
		assertEquals(listOf(Rank.EIGHT, Rank.FOUR), round.hands[1].cards.map { it.rank })
		assertTrue(Action.SURRENDER !in round.legalActions())
	}

	@Test
	fun autoStandOn21_endsTheTurn_whenOn() {
		val round = roundWith(Rank.TEN, Rank.SIX, Rank.FIVE, Rank.TEN, Rank.SIX)
		round.placeBet(10)
		round.act(Action.HIT) // 10 + 5 + 6 = 21
		assertEquals(Phase.FINISHED, round.phase)
	}

	@Test
	fun autoStandOn21_keepsTheTurn_whenOff() {
		val round = roundWith(
			Rank.TEN, Rank.SIX, Rank.FIVE, Rank.TEN, Rank.SIX,
			autoStandOn21 = false,
		)
		round.placeBet(10)
		round.act(Action.HIT)
		assertEquals(Phase.PLAYER_TURN, round.phase)
		assertTrue(Action.STAND in round.legalActions())
	}

	@Test
	fun startBetting_twiceIsRejected() {
		val round = roundWith(Rank.TEN, Rank.SIX, Rank.EIGHT, Rank.TEN)
		round.startBetting(10)
		assertThrows(IllegalStateException::class.java) { round.startBetting(10) }
	}

	@Test
	fun startBetting_rejectsZeroAndNegativeBets() {
		val round = Round(Shoe(random = Random(1)), GameMode.WILDCARD, bankroll = 1000L)
		assertThrows(IllegalArgumentException::class.java) { round.startBetting(0) }
		assertThrows(IllegalArgumentException::class.java) { round.startBetting(-100) }
	}
}