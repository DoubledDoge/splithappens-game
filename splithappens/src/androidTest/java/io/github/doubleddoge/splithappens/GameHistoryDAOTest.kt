package io.github.doubleddoge.splithappens.data.dao

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.doubleddoge.splithappens.data.entity.GameEntity
import io.github.doubleddoge.splithappens.data.entity.HandEntity
import io.github.doubleddoge.splithappens.data.entity.SessionEntity
import io.github.doubleddoge.splithappens.data.enum.GameMode
import io.github.doubleddoge.splithappens.data.enum.HandResult
import io.github.doubleddoge.splithappens.data.enum.HandStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameHistoryDaoTest : DatabaseTest() {
    private val dao get() = db.gameHistoryDao()

    private suspend fun session(userId: String, mode: GameMode = GameMode.WILDCARD, startedAt: Long = 1) =
        db.sessionDao().insert(SessionEntity(userId = userId, stakes = 100, mode = mode, startedAt = startedAt))

    // hands: (bet, result) pairs for the player; a dealer hand is always added
    private suspend fun game(
        sessionId: Long,
        gameNum: Int,
        hands: List<Pair<Long, HandResult>>,
        insuranceBet: Long = 0,
        dealerBlackjack: Boolean = false,
    ): Long {
        val round = db.roundDao()
        val gameId =
            round.insertGame(GameEntity.create(sessionId, gameNum, 20, insuranceBet, dealerBlackjack))
        hands.forEach { (bet, result) ->
            val status = if (result == HandResult.SURRENDER) HandStatus.SURRENDERED else HandStatus.STOOD
            round.insertHand(HandEntity.player(bet, status, result).copy(gameId = gameId))
        }
        round.insertHand(HandEntity.dealer(HandStatus.STOOD).copy(gameId = gameId))
        return gameId
    }

    @Test
    fun netChips_sumsPayoutMinusBet_ignoringDealerHand() = runBlocking {
        val user = newUser()
        val s = session(user.userId)
        game(s, 1, listOf(100L to HandResult.WIN, 50L to HandResult.LOSS))

        val row = dao.observeForUser(user.userId).first().single()
        assertEquals(2, row.playerHands) // dealer hand not counted
        assertEquals(50L, row.netChips) // +100 - 50
        assertEquals(20, row.dealerTotal)
    }

    @Test
    fun netChips_includesInsurance() = runBlocking {
        val user = newUser()
        val s = session(user.userId)
        // lose 100, insurance 50 pays 150 -> -100 + 150 - 50 = 0
        game(s, 1, listOf(100L to HandResult.LOSS), insuranceBet = 50, dealerBlackjack = true)
        // lose 100, insurance 50 lost -> -150
        game(s, 2, listOf(100L to HandResult.LOSS), insuranceBet = 50)

        val nets = dao.observeForUser(user.userId).first().associate { it.gameNum to it.netChips }
        assertEquals(0L, nets[1])
        assertEquals(-150L, nets[2])
    }

    @Test
    fun netChips_pushAndSurrender() = runBlocking {
        val user = newUser()
        val s = session(user.userId)
        game(s, 1, listOf(100L to HandResult.PUSH))
        game(s, 2, listOf(100L to HandResult.SURRENDER))

        val nets = dao.observeForUser(user.userId).first().associate { it.gameNum to it.netChips }
        assertEquals(0L, nets[1])
        assertEquals(-50L, nets[2])
    }

    @Test
    fun observeForUser_newestSessionFirst_thenHighestGameNum() = runBlocking {
        val user = newUser()
        val old = session(user.userId, startedAt = 100)
        val recent = session(user.userId, startedAt = 200)
        game(old, 1, listOf(10L to HandResult.WIN))
        game(recent, 1, listOf(10L to HandResult.WIN))
        game(recent, 2, listOf(10L to HandResult.WIN))

        val order = dao.observeForUser(user.userId).first().map { it.sessionId to it.gameNum }
        assertEquals(listOf(recent to 2, recent to 1, old to 1), order)
    }

    @Test
    fun observeForUser_filtersByMode() = runBlocking {
        val user = newUser()
        val trad = session(user.userId, GameMode.TRADITIONAL)
        val wild = session(user.userId, GameMode.WILDCARD)
        game(trad, 1, listOf(10L to HandResult.WIN))
        game(wild, 1, listOf(10L to HandResult.WIN))

        val rows = dao.observeForUser(user.userId, GameMode.TRADITIONAL).first()
        assertEquals(1, rows.size)
        assertEquals(GameMode.TRADITIONAL, rows.single().mode)
    }

    @Test
    fun observeForUser_isolatesUsers() = runBlocking {
        val u1 = newUser("One")
        val u2 = newUser("Two")
        game(session(u1.userId), 1, listOf(10L to HandResult.WIN))
        game(session(u2.userId), 1, listOf(10L to HandResult.WIN))

        assertEquals(1, dao.observeForUser(u1.userId).first().size)
        assertEquals(0, dao.observeForUser("nobody").first().size)
    }

    @Test
    fun observeNetChips_zeroWhenNoGames() = runBlocking {
        assertEquals(0L, dao.observeNetChips(newUser().userId).first())
    }

    @Test
    fun observeNetChips_sumsAcrossSessions() = runBlocking {
        val user = newUser()
        game(session(user.userId), 1, listOf(100L to HandResult.WIN)) // +100
        game(session(user.userId), 1, listOf(100L to HandResult.LOSS)) // -100
        game(session(user.userId), 1, listOf(100L to HandResult.BLACKJACK)) // +200
        assertEquals(200L, dao.observeNetChips(user.userId).first())
    }

    @Test
    fun observeNetChipsByMode_groupsPerMode() = runBlocking {
        val user = newUser()
        val trad = session(user.userId, GameMode.TRADITIONAL)
        val wild = session(user.userId, GameMode.WILDCARD)
        game(trad, 1, listOf(100L to HandResult.WIN)) // +100
        game(trad, 2, listOf(100L to HandResult.LOSS)) // -100 -> 0
        game(wild, 1, listOf(100L to HandResult.QUEEN_JACK)) // +900

        val byMode = dao.observeNetChipsByMode(user.userId).first().associate { it.mode to it.netChips }
        assertEquals(0L, byMode[GameMode.TRADITIONAL])
        assertEquals(900L, byMode[GameMode.WILDCARD])
    }

    @Test
    fun history_disappearsWhenSessionDeleted_viaUserCascade() = runBlocking {
        val user = newUser()
        game(session(user.userId), 1, listOf(10L to HandResult.WIN))
        db.userDao().delete(user)
        assertEquals(0, dao.observeForUser(user.userId).first().size)
    }
}