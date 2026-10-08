package io.github.doubleddoge.splithappens.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.doubleddoge.splithappens.data.entity.CardEntity
import io.github.doubleddoge.splithappens.data.entity.GameEntity
import io.github.doubleddoge.splithappens.data.entity.HandCardEntity
import io.github.doubleddoge.splithappens.data.entity.HandEntity
import io.github.doubleddoge.splithappens.data.entity.SessionEntity
import io.github.doubleddoge.splithappens.data.enum.HandResult
import io.github.doubleddoge.splithappens.data.enum.HandStatus
import io.github.doubleddoge.splithappens.data.enum.Rank
import io.github.doubleddoge.splithappens.data.enum.Suit
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoundConstraintsTest : DatabaseTest() {
    private val dao get() = db.roundDao()

    private val tenHearts = CardEntity.idOf(Suit.HEARTS, Rank.TEN)
    private val nineClubs = CardEntity.idOf(Suit.CLUBS, Rank.NINE)

    @Before
    fun seedCards() = runBlocking { db.cardDao().insertAll(CardEntity.standardDeck()) }

    private suspend fun newSession(): Long =
        db.sessionDao().insert(SessionEntity(userId = newUser().userId, stakes = 10))

    private suspend fun newGame(sessionId: Long, gameNum: Int = 1): Long =
        dao.insertGame(GameEntity.create(sessionId, gameNum, 19))

    private suspend fun newHand(gameId: Long): Long =
        dao.insertHand(HandEntity.player(10, HandStatus.STOOD, HandResult.WIN).copy(gameId = gameId))

    private suspend fun handWithOneCard(): Long {
        val handId = newHand(newGame(newSession()))
        dao.insertHandCards(listOf(HandCardEntity(handId, 0, tenHearts)))
        return handId
    }

    // ---- HandCards: composite key (handId, dealtOrder) ----

    @Test
    fun handCard_sameHandSameDealtOrder_rejected_evenWithDifferentCard() = runBlocking {
        val handId = handWithOneCard()

        assertFailsWithException { dao.insertHandCards(listOf(HandCardEntity(handId, 0, nineClubs))) }

        assertEquals(1, dao.countHandCards())
        assertEquals(tenHearts, dao.getHandCards(handId).single().cardId) // original untouched
    }

    @Test
    fun handCard_sameHandSameCard_differentDealtOrder_allowed() = runBlocking {
        val handId = handWithOneCard()
        dao.insertHandCards(listOf(HandCardEntity(handId, 1, tenHearts))) // multi-deck shoe
        assertEquals(2, dao.countHandCards())
    }

    @Test
    fun handCard_differentHands_sameDealtOrder_andSameCard_allowed() = runBlocking {
        val gameId = newGame(newSession())
        val a = newHand(gameId)
        val b = newHand(gameId)
        dao.insertHandCards(listOf(HandCardEntity(a, 0, tenHearts), HandCardEntity(b, 0, tenHearts)))
        assertEquals(2, dao.countHandCards())
    }

    @Test
    fun handCard_unknownHand_rejected() = runBlocking {
        assertFailsWithException { dao.insertHandCards(listOf(HandCardEntity(9999, 0, tenHearts))) }
        assertEquals(0, dao.countHandCards())
    }

    @Test
    fun handCard_unknownCard_rejected() = runBlocking {
        val handId = newHand(newGame(newSession()))
        assertFailsWithException { dao.insertHandCards(listOf(HandCardEntity(handId, 0, 999))) }
        assertEquals(0, dao.countHandCards())
    }

    // ---- Games: unique index (sessionId, gameNum) ----

    @Test
    fun game_sameSessionSameGameNum_rejected() = runBlocking {
        val sessionId = newSession()
        newGame(sessionId, 1)

        assertFailsWithException { newGame(sessionId, 1) }

        assertEquals(1, dao.countGames())
    }

    @Test
    fun game_sameGameNum_differentSessions_allowed() = runBlocking {
        newGame(newSession(), 1)
        newGame(newSession(), 1)
        assertEquals(2, dao.countGames())
    }

    @Test
    fun game_sameSession_differentGameNum_allowed() = runBlocking {
        val sessionId = newSession()
        newGame(sessionId, 1)
        newGame(sessionId, 2)
        assertEquals(2, dao.countGames())
    }

    @Test
    fun game_unknownSession_rejected() = runBlocking {
        assertFailsWithException { newGame(sessionId = 9999) }
        assertEquals(0, dao.countGames())
    }

    // ---- Hands: foreign keys ----

    @Test
    fun hand_unknownGame_rejected() = runBlocking {
        assertFailsWithException { newHand(gameId = 9999) }
        assertEquals(0, dao.countHands())
    }

    @Test
    fun hand_unknownParent_rejected() = runBlocking {
        val gameId = newGame(newSession())
        assertFailsWithException {
            dao.insertHand(
                HandEntity.player(10, HandStatus.STOOD, HandResult.WIN)
                    .copy(gameId = gameId, parentHandId = 9999),
            )
        }
        assertEquals(0, dao.countHands())
    }
}