package io.github.doubleddoge.splithappens.game

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.doubleddoge.splithappens.data.DatabaseTest
import io.github.doubleddoge.splithappens.data.entity.CardEntity
import io.github.doubleddoge.splithappens.data.enum.Action
import io.github.doubleddoge.splithappens.game.enum.GameMode
import io.github.doubleddoge.splithappens.game.enum.Rank
import io.github.doubleddoge.splithappens.data.enum.SessionStatus
import io.github.doubleddoge.splithappens.game.enum.Suit
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameSessionManagerTest : DatabaseTest() {

	@Before
	fun seedCards() = runBlocking {
		db.cardDao().insertAll(CardEntity.standardDeck())
	}

	@Test
	fun testSessionLifecycleAndRoundPersistence() = runBlocking {
		val user = newUser(chips = 1000)
		val manager = GameSessionManager(
			sessionDao = db.sessionDao(),
			roundDao = db.roundDao(),
			userId = user.userId,
			stakes = 100,
			mode = GameMode.WILDCARD
		)

		val sessionId = manager.startSession()
		assertTrue(sessionId > 0)
		assertTrue(manager.isSessionActive)
		assertEquals(1, manager.gameNum)

		// Play a round
		val shoe = Shoe(deckCount = 1).apply {
			stackTop(listOf(
				Card(Suit.SPADES, Rank.TEN), Card(Suit.CLUBS, Rank.SIX),      // player, dealer up
				Card(Suit.HEARTS, Rank.EIGHT), Card(Suit.DIAMONDS, Rank.TEN), // player, dealer hole
				Card(Suit.SPADES, Rank.NINE),                                 // dealer's draw
			))
		}
		val round = Round(shoe = shoe, mode = GameMode.WILDCARD, bankroll = 1000, minBet = 100)
		round.placeBet(100)
		round.act(Action.STAND) // Dealer plays out

		// Save round
		manager.saveAndNextRound(round)
		assertEquals(2, manager.gameNum)
		assertEquals(1, db.roundDao().countGames())

		// End session
		manager.endSession(SessionStatus.COMPLETED)
		val session = db.sessionDao().getById(sessionId)
		assertEquals(SessionStatus.COMPLETED, session?.status)
		assertTrue(session?.endedAt != null)

		val updatedUser = db.userDao().getById(user.userId)
		val net = round.toRecord(sessionId, 1).netChips
		assertEquals(user.chipsOwned + net, updatedUser?.chipsOwned)
	}

	/**
	 * 6. Card Reshuffling Timing Test
	 * Tests that the shoe reshuffles between rounds when remaining cards drop below threshold,
	 * and never reshuffles mid-round.
	 */
	@Test
	fun testCardReshufflingTimingBetweenRounds() {
		val shoe = Shoe(deckCount = 1)
		val initialRemaining = shoe.remaining
		assertEquals(52, initialRemaining)

		// Deal some cards (mid-round simulation)
		val card1 = shoe.deal()
		val card2 = shoe.deal()
		assertNotNull(card1)
		assertNotNull(card2)
		assertEquals(50, shoe.remaining)

		// Reshuffle should not have happened mid-round automatically unless explicitly triggered or threshold reached
		assertFalse(shoe.needsReshuffle)

		// Exhaust shoe until threshold (cutCardAt = 52 / 4 = 13 remaining)
		while (shoe.remaining > 13) {
			shoe.deal()
		}

		assertTrue(shoe.needsReshuffle)

		// Reshuffle between rounds
		shoe.reshuffle()
		assertEquals(52, shoe.remaining)
		assertFalse(shoe.needsReshuffle)
	}

}
