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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoundDaoTest : DatabaseTest() {
    private val dao get() = db.roundDao()

    private fun card(order: Int, s: Suit, r: Rank) =
        HandCardEntity(handId = 0, dealtOrder = order, cardId = CardEntity.idOf(s, r))

    private suspend fun seed(chips: Long = 1000): Pair<String, Long> {
        val user = newUser(chips = chips)
        db.cardDao().insertAll(CardEntity.standardDeck())
        val sessionId = db.sessionDao().insert(SessionEntity(userId = user.userId, stakes = 10))
        return user.userId to sessionId
    }

    private fun simpleRound(sessionId: Long, gameNum: Int = 1) = RoundRecord(
        game = GameEntity.create(sessionId, gameNum, dealerTotal = 19),
        hands = listOf(
            HandRecord(
                HandEntity.player(10, HandStatus.STOOD, HandResult.WIN),
                cards = listOf(card(0, Suit.HEARTS, Rank.TEN), card(1, Suit.CLUBS, Rank.NINE)),
            ),
            HandRecord(
                HandEntity.dealer(HandStatus.STOOD),
                cards = listOf(card(0, Suit.SPADES, Rank.TEN), card(1, Suit.SPADES, Rank.NINE)),
            ),
        ),
    )

    private suspend fun assertNothingPersisted() {
        assertEquals(0, dao.countGames())
        assertEquals(0, dao.countHands())
        assertEquals(0, dao.countHandCards())
    }

    @Test
    fun saveRound_updatesChipsAndHistory() = runBlocking {
        val (userId, sessionId) = seed()

        dao.saveRound(simpleRound(sessionId), userId, newChipsOwned = 0)

        assertEquals(1010L, db.userDao().getById(userId)!!.chipsOwned)
        assertEquals(10L, db.gameHistoryDao().observeNetChips(userId).first())
        assertEquals(1, db.gameHistoryDao().observeForUser(userId).first().single().playerHands)
    }


    //Beginning of splitting tests
    @Test
    fun saveRound_withSplit_linksChildToParent_andSumsBothHands() = runBlocking {
        val (userId, sessionId) = seed()

        // 8-8 split: parent finishes on 18 (WIN), split hand finishes on 17 (PUSH), dealer stands on 17.
        val round = RoundRecord(
            game = GameEntity.create(sessionId, gameNum = 1, dealerTotal = 17),
            hands = listOf(
                HandRecord( // index 0: parent
                    HandEntity.player(10, HandStatus.STOOD, HandResult.WIN),
                    cards = listOf(card(0, Suit.HEARTS, Rank.EIGHT), card(1, Suit.CLUBS, Rank.TEN)),
                ),
                HandRecord( // index 1: split child of index 0
                    HandEntity.player(10, HandStatus.STOOD, HandResult.PUSH),
                    parentIndex = 0,
                    cards = listOf(card(0, Suit.SPADES, Rank.EIGHT), card(1, Suit.DIAMONDS, Rank.NINE)),
                ),
                HandRecord(
                    HandEntity.dealer(HandStatus.STOOD),
                    cards = listOf(card(0, Suit.SPADES, Rank.TEN), card(1, Suit.HEARTS, Rank.SEVEN)),
                ),
            ),
        )

        val gameId = dao.saveRound(round, userId, newChipsOwned = 0)

        val hands = dao.getHands(gameId)
        assertEquals(3, hands.size)
        val parent = hands[0]
        val child = hands[1]
        assertNull(parent.parentHandId)
        assertEquals(parent.handId, child.parentHandId)
        assertEquals(HandOwner.DEALER, hands[2].owner)
        assertNull(hands[2].parentHandId)

        assertEquals(6, dao.countHandCards())
        // +10 (win) + 0 (push) = +10
        assertEquals(1010L, db.userDao().getById(userId)!!.chipsOwned)
        val history = db.gameHistoryDao().observeForUser(userId).first().single()
        assertEquals(2, history.playerHands)
        assertEquals(10L, history.netChips)
    }

    @Test
    fun saveRound_splitChildBeforeParent_throwsAndPersistsNothing() = runBlocking {
        val (userId, sessionId) = seed()
        val round = RoundRecord(
            game = GameEntity.create(sessionId, 1, 17),
            hands = listOf(
                HandRecord(
                    HandEntity.player(10, HandStatus.STOOD, HandResult.WIN),
                    parentIndex = 1, // parent not inserted yet
                    cards = listOf(card(0, Suit.HEARTS, Rank.TEN)),
                ),
            ),
        )

        assertTrue(runCatching { dao.saveRound(round, userId, 0) }.isFailure)
        assertNothingPersisted()
        assertEquals(1000L, db.userDao().getById(userId)!!.chipsOwned)
    }

    @Test
    fun saveRound_invalidCardOnSplitChild_rollsBackGameHandsCardsAndChips() = runBlocking {
        val (userId, sessionId) = seed()
        val unknownCard = HandCardEntity(handId = 0, dealtOrder = 1, cardId = 999) // not in Cards

        val round = RoundRecord(
            game = GameEntity.create(sessionId, 1, 17),
            hands = listOf(
                HandRecord( // parent + its cards are written successfully first
                    HandEntity.player(10, HandStatus.STOOD, HandResult.WIN),
                    cards = listOf(card(0, Suit.HEARTS, Rank.EIGHT), card(1, Suit.CLUBS, Rank.TEN)),
                ),
                HandRecord( // fails here, mid-round
                    HandEntity.player(10, HandStatus.STOOD, HandResult.WIN),
                    parentIndex = 0,
                    cards = listOf(card(0, Suit.SPADES, Rank.EIGHT), unknownCard),
                ),
            ),
        )

        assertTrue(runCatching { dao.saveRound(round, userId, 0) }.isFailure)

        assertNothingPersisted() // no orphaned parent hand, no game row
        assertEquals(1000L, db.userDao().getById(userId)!!.chipsOwned)
        assertEquals(0, db.gameHistoryDao().observeForUser(userId).first().size)
        assertEquals(0L, db.gameHistoryDao().observeNetChips(userId).first())
    }
    @Test
    fun saveRound_split_eachHandKeepsItsOwnCards() = runBlocking {
        val (userId, sessionId) = seed()
        val round = RoundRecord(
            game = GameEntity.create(sessionId, 1, 17),
            hands = listOf(
                HandRecord(
                    HandEntity.player(10, HandStatus.STOOD, HandResult.WIN),
                    cards = listOf(card(0, Suit.HEARTS, Rank.EIGHT), card(1, Suit.CLUBS, Rank.TEN)),
                ),
                HandRecord(
                    HandEntity.player(10, HandStatus.STOOD, HandResult.PUSH),
                    parentIndex = 0,
                    cards = listOf(
                        card(0, Suit.SPADES, Rank.EIGHT),
                        card(1, Suit.DIAMONDS, Rank.FIVE),
                        card(2, Suit.CLUBS, Rank.FOUR),
                    ),
                ),
            ),
        )

        val (parent, child) = dao.getHands(dao.saveRound(round, userId, 0))

        assertEquals(
            listOf(CardEntity.idOf(Suit.HEARTS, Rank.EIGHT), CardEntity.idOf(Suit.CLUBS, Rank.TEN)),
            dao.getHandCards(parent.handId).map { it.cardId },
        )
        assertEquals(
            listOf(
                CardEntity.idOf(Suit.SPADES, Rank.EIGHT),
                CardEntity.idOf(Suit.DIAMONDS, Rank.FIVE),
                CardEntity.idOf(Suit.CLUBS, Rank.FOUR),
            ),
            dao.getHandCards(child.handId).map { it.cardId },
        )
    }

    @Test
    fun saveRound_resplit_siblingsAndGrandchildPointAtCorrectParents() = runBlocking {
        val (userId, sessionId) = seed()
        fun hand(result: HandResult, parent: Int?, rank: Rank) = HandRecord(
            HandEntity.player(10, HandStatus.STOOD, result),
            parentIndex = parent,
            cards = listOf(card(0, Suit.HEARTS, rank)),
        )
        val round = RoundRecord(
            game = GameEntity.create(sessionId, 1, 18),
            hands = listOf(
                hand(HandResult.WIN, null, Rank.EIGHT), // 0 root
                hand(HandResult.WIN, 0, Rank.NINE),     // 1 child of 0
                hand(HandResult.LOSS, 0, Rank.TWO),     // 2 child of 0 (sibling)
                hand(HandResult.PUSH, 1, Rank.THREE),   // 3 re-split: child of 1
            ),
        )

        val h = dao.getHands(dao.saveRound(round, userId, 0))

        assertNull(h[0].parentHandId)
        assertEquals(h[0].handId, h[1].parentHandId)
        assertEquals(h[0].handId, h[2].parentHandId)
        assertEquals(h[1].handId, h[3].parentHandId)
        assertEquals(4, dao.countHandCards())
        assertEquals(1010L, db.userDao().getById(userId)!!.chipsOwned) // +10 +10 -10 +0
    }

    @Test
    fun saveRound_split_doubledChildAndLosingParent_netsCorrectly() = runBlocking {
        val (userId, sessionId) = seed()
        val round = RoundRecord(
            game = GameEntity.create(sessionId, 1, 19),
            hands = listOf(
                HandRecord(
                    HandEntity.player(10, HandStatus.STOOD, HandResult.LOSS),
                    cards = listOf(card(0, Suit.HEARTS, Rank.EIGHT)),
                ),
                HandRecord(
                    HandEntity.player(20, HandStatus.STOOD, HandResult.WIN, isDoubled = true),
                    parentIndex = 0,
                    cards = listOf(card(0, Suit.SPADES, Rank.EIGHT)),
                ),
            ),
        )

        val (_, child) = dao.getHands(dao.saveRound(round, userId, 0))

        assertTrue(child.isDoubled)
        assertEquals(20L, child.betAmount)
        assertEquals(40L, child.payout)
        assertEquals(1010L, db.userDao().getById(userId)!!.chipsOwned) // -10 + 20
        assertEquals(10L, db.gameHistoryDao().observeNetChips(userId).first())
    }

    @Test
    fun saveRound_selfReferencingParentIndex_rollsBack() = runBlocking {
        val (userId, sessionId) = seed()
        val round = RoundRecord(
            game = GameEntity.create(sessionId, 1, 17),
            hands = listOf(
                HandRecord(
                    HandEntity.player(10, HandStatus.STOOD, HandResult.WIN),
                    parentIndex = 0, // points at itself
                    cards = listOf(card(0, Suit.HEARTS, Rank.TEN)),
                ),
            ),
        )

        assertTrue(runCatching { dao.saveRound(round, userId, 0) }.isFailure)
        assertNothingPersisted()
        assertEquals(1000L, db.userDao().getById(userId)!!.chipsOwned)
    }
    //End of splitting tests


    @Test
    fun saveRound_duplicateDealtOrder_rollsBack() = runBlocking {
        val (userId, sessionId) = seed()
        val round = RoundRecord(
            game = GameEntity.create(sessionId, 1, 19),
            hands = listOf(
                HandRecord(
                    HandEntity.player(10, HandStatus.STOOD, HandResult.WIN),
                    // same (handId, dealtOrder) twice -> primary key violation
                    cards = listOf(card(0, Suit.HEARTS, Rank.TEN), card(0, Suit.CLUBS, Rank.NINE)),
                ),
            ),
        )

        assertTrue(runCatching { dao.saveRound(round, userId, 0) }.isFailure)
        assertNothingPersisted()
        assertEquals(1000L, db.userDao().getById(userId)!!.chipsOwned)
    }

    @Test
    fun saveRound_duplicateGameNum_failsAtGameInsert_andKeepsEarlierRoundIntact() = runBlocking {
        val (userId, sessionId) = seed()
        dao.saveRound(simpleRound(sessionId, gameNum = 1), userId, 0)

        // Same (sessionId, gameNum) -> unique index violation on the very first write
        assertTrue(runCatching { dao.saveRound(simpleRound(sessionId, gameNum = 1), userId, 0) }.isFailure)

        assertEquals(1, dao.countGames())
        assertEquals(2, dao.countHands())
        assertEquals(4, dao.countHandCards())
        assertEquals(1010L, db.userDao().getById(userId)!!.chipsOwned) // only the first round counted
    }

    @Test
    fun saveRound_sameCardTwiceInOneHand_isAllowed_forMultiDeckShoes() = runBlocking {
        val (userId, sessionId) = seed()
        val round = RoundRecord(
            game = GameEntity.create(sessionId, 1, 18),
            hands = listOf(
                HandRecord(
                    HandEntity.player(10, HandStatus.STOOD, HandResult.WIN),
                    cards = listOf(card(0, Suit.HEARTS, Rank.NINE), card(1, Suit.HEARTS, Rank.NINE)),
                ),
            ),
        )

        dao.saveRound(round, userId, 0)

        assertEquals(2, dao.countHandCards())
        assertEquals(1010L, db.userDao().getById(userId)!!.chipsOwned)
    }
}