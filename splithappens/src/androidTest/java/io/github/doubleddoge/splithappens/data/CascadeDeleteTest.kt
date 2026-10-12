package io.github.doubleddoge.splithappens.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.doubleddoge.splithappens.data.dao.HandRecord
import io.github.doubleddoge.splithappens.data.dao.RoundRecord
import io.github.doubleddoge.splithappens.data.entity.CardEntity
import io.github.doubleddoge.splithappens.data.entity.GameEntity
import io.github.doubleddoge.splithappens.data.entity.HandCardEntity
import io.github.doubleddoge.splithappens.data.entity.HandEntity
import io.github.doubleddoge.splithappens.data.entity.SessionEntity
import io.github.doubleddoge.splithappens.data.enum.HandOwner
import io.github.doubleddoge.splithappens.data.enum.HandResult
import io.github.doubleddoge.splithappens.data.enum.HandStatus
import io.github.doubleddoge.splithappens.data.enum.Rank
import io.github.doubleddoge.splithappens.data.enum.Suit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

//Note: Cascade Deletions do NOT reverse chips owned. Deletion assumes that the user survives, but not the chip balance.

@RunWith(AndroidJUnit4::class)
class CascadeDeleteTest : DatabaseTest() {
    private val round get() = db.roundDao()

    @Before
    fun seedCards() = runBlocking { db.cardDao().insertAll(CardEntity.standardDeck()) }

    private fun card(order: Int, s: Suit, r: Rank) =
        HandCardEntity(handId = 0, dealtOrder = order, cardId = CardEntity.idOf(s, r))

    private suspend fun newSession(userId: String) =
        db.sessionDao().insert(SessionEntity(userId = userId, stakes = 10))

    /** Saves one game with 3 hands (parent, split child, dealer) and 6 cards. Returns the gameId. */
    private suspend fun saveSplitGame(userId: String, sessionId: Long, gameNum: Int): Long =
        round.saveRound(
            RoundRecord(
                game = GameEntity.create(sessionId, gameNum, 17),
                hands = listOf(
                    HandRecord(
                        HandEntity.player(10, HandStatus.STOOD, HandResult.WIN),
                        cards = listOf(
                            card(0, Suit.HEARTS, Rank.EIGHT),
                            card(1, Suit.CLUBS, Rank.TEN)
                        ),
                    ),
                    HandRecord(
                        HandEntity.player(10, HandStatus.STOOD, HandResult.PUSH),
                        parentIndex = 0,
                        cards = listOf(
                            card(0, Suit.SPADES, Rank.EIGHT),
                            card(1, Suit.DIAMONDS, Rank.NINE)
                        ),
                    ),
                    HandRecord(
                        HandEntity.dealer(HandStatus.STOOD),
                        cards = listOf(
                            card(0, Suit.SPADES, Rank.TEN),
                            card(1, Suit.HEARTS, Rank.SEVEN)
                        ),
                    ),
                ),
            ),
            userId, 0,
        )

    @Test
    fun deleteSession_cascadesGamesHandsAndCards() = runBlocking {
        val user = newUser()
        val sessionId = newSession(user.userId)
        saveSplitGame(user.userId, sessionId, 1)
        saveSplitGame(user.userId, sessionId, 2)
        Assert.assertEquals(2, round.countGames())
        Assert.assertEquals(6, round.countHands())
        Assert.assertEquals(12, round.countHandCards())

        db.sessionDao().deleteById(sessionId)

        Assert.assertNull(db.sessionDao().getById(sessionId))
        Assert.assertEquals(0, round.countGames())
        Assert.assertEquals(0, round.countHands())
        Assert.assertEquals(0, round.countHandCards())
        Assert.assertEquals(0, db.gameHistoryDao().observeForUser(user.userId).first().size)
    }

    @Test
    fun deleteSession_leavesOtherSessionsUsersAndCardsUntouched() = runBlocking {
        val user = newUser()
        val doomed = newSession(user.userId)
        val kept = newSession(user.userId)
        saveSplitGame(user.userId, doomed, 1)
        val keptGame = saveSplitGame(user.userId, kept, 1)

        db.sessionDao().deleteById(doomed)

        Assert.assertEquals(1, round.countGames())
        Assert.assertEquals(3, round.getHands(keptGame).size)
        Assert.assertEquals(6, round.countHandCards())
        Assert.assertNotNull(db.sessionDao().getById(kept))
        Assert.assertNotNull(db.userDao().getById(user.userId))
        Assert.assertEquals(52, db.cardDao().count()) // reference data is never cascaded
    }

    @Test
    fun deleteGame_cascadesHandsAndCards_butKeepsSession() = runBlocking {
        val user = newUser()
        val sessionId = newSession(user.userId)
        val gameId = saveSplitGame(user.userId, sessionId, 1)

        round.deleteGame(gameId)

        Assert.assertEquals(0, round.countHands())
        Assert.assertEquals(0, round.countHandCards())
        Assert.assertNotNull(db.sessionDao().getById(sessionId))
    }

    @Test
    fun deleteParentHand_cascadesSplitChild_andTheirCards_butNotDealer() = runBlocking {
        val user = newUser()
        val gameId = saveSplitGame(user.userId, newSession(user.userId), 1)
        val parent = round.getHands(gameId).first()

        round.deleteHand(parent.handId)

        val remaining = round.getHands(gameId)
        Assert.assertEquals(listOf(HandOwner.DEALER), remaining.map { it.owner })
        Assert.assertEquals(2, round.countHandCards()) // only the dealer's cards remain
    }

    @Test
    fun deleteSplitChild_keepsParent() = runBlocking {
        val user = newUser()
        val gameId = saveSplitGame(user.userId, newSession(user.userId), 1)
        val (parent, child) = round.getHands(gameId)

        round.deleteHand(child.handId)

        Assert.assertEquals(
            listOf(parent.handId),
            round.getHands(gameId).filter { it.owner == HandOwner.PLAYER }.map { it.handId })
        Assert.assertEquals(4, round.countHandCards())
    }

    @Test
    fun deleteUser_cascadesWholeChain() = runBlocking {
        val user = newUser()
        saveSplitGame(user.userId, newSession(user.userId), 1)

        db.userDao().delete(user)

        Assert.assertEquals(0, round.countGames())
        Assert.assertEquals(0, round.countHands())
        Assert.assertEquals(0, round.countHandCards())
        Assert.assertEquals(52, db.cardDao().count())
    }
}