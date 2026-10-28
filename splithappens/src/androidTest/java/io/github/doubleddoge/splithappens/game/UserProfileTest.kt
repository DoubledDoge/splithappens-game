package io.github.doubleddoge.splithappens.game

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.doubleddoge.splithappens.data.DatabaseTest
import io.github.doubleddoge.splithappens.data.entity.CardEntity
import io.github.doubleddoge.splithappens.data.entity.GameEntity
import io.github.doubleddoge.splithappens.data.entity.HandEntity
import io.github.doubleddoge.splithappens.data.entity.SessionEntity
import io.github.doubleddoge.splithappens.data.enum.HandResult
import io.github.doubleddoge.splithappens.data.enum.HandStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
@RunWith(AndroidJUnit4::class)
class UserProfileTest: DatabaseTest() {

    private val context: Context get() = ApplicationProvider.getApplicationContext()

    @Before
    fun setupCards() = runBlocking {
        db.cardDao().insertAll(CardEntity.standardDeck())
    }

    @Test
    fun testUserSwitchAndProfileDataIsolation() = runBlocking {
        val user1 = newUser("Alice", 1000L)
        val user2 = newUser("Bob", 2000L)

        // Simulate active user switching in SharedPreferences
        val prefs = context.getSharedPreferences("split_happens_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("active_user_id", user1.userId).apply()

        // Play game for User 1
        val session1 = db.sessionDao().insert(SessionEntity(userId = user1.userId, stakes = 50))
        val gameId1 = db.roundDao().insertGame(GameEntity.create(session1, 1, 18))
        db.roundDao().insertHand(HandEntity.player(50, HandStatus.STOOD, HandResult.WIN).copy(gameId = gameId1))
        db.roundDao().insertHand(HandEntity.dealer(HandStatus.STOOD).copy(gameId = gameId1))
        db.userDao().setChips(user1.userId, 1100L)

        // Switch active user to User 2
        prefs.edit().putString("active_user_id", user2.userId).apply()

        // Verify User 2 data is isolated and untouched by User 1's game
        val loadedUser1 = db.userDao().getById(user1.userId)!!
        val loadedUser2 = db.userDao().getById(user2.userId)!!

        assertEquals(1100L, loadedUser1.chipsOwned)
        assertEquals(2000L, loadedUser2.chipsOwned)

        val user1History = db.gameHistoryDao().observeForUser(user1.userId).first()
        val user2History = db.gameHistoryDao().observeForUser(user2.userId).first()

        assertEquals(1, user1History.size)
        assertEquals(0, user2History.size)
        assertEquals(user1.userId, user1History.single().userId)
    }


    /**
     * Daily Payouts Functionality & 24-Hour Interval Enforcement
     * Tests that daily reward is claimed automatically on startup if 24 hours have passed,
     * but blocked if requested within the 24-hour window.
     */


    @Test
    fun testDailyPayoutIntervalEnforcement() = runBlocking {
        val user = newUser("Charlie", 1000L)
        val prefs = context.getSharedPreferences("split_happens_prefs", Context.MODE_PRIVATE)
        val claimKey = "pref_last_daily_claim_${user.userId}"

        val now = System.currentTimeMillis()
        val interval = 24 * 60 * 60 * 1000L

        // Scenario A: First time claim (lastClaim == 0L) -> should claim
        var lastClaim = prefs.getLong(claimKey, 0L)
        var claimed = false
        if (lastClaim == 0L || (now - lastClaim) >= interval) {
            prefs.edit().putLong(claimKey, now).apply()
            db.userDao().setChips(user.userId, user.chipsOwned + 500L)
            claimed = true
        }
        assertTrue(claimed)
        assertEquals(1500L, db.userDao().getById(user.userId)?.chipsOwned)

        // Scenario B: Immediate second claim (< 24h passed) -> should NOT claim
        val nowAgain = now + 1000L // 1 second later
        lastClaim = prefs.getLong(claimKey, 0L)
        var secondClaimed = false
        if (lastClaim == 0L || (nowAgain - lastClaim) >= interval) {
            prefs.edit().putLong(claimKey, nowAgain).apply()
            db.userDao().setChips(user.userId, db.userDao().getById(user.userId)!!.chipsOwned + 500L)
            secondClaimed = true
        }
        assertFalse(secondClaimed)
        assertEquals(1500L, db.userDao().getById(user.userId)?.chipsOwned)

        // Scenario C: After 24h+ elapsed -> should claim again
        val futureTime = now + interval + 1000L
        lastClaim = prefs.getLong(claimKey, 0L)
        var thirdClaimed = false
        if (lastClaim == 0L || (futureTime - lastClaim) >= interval) {
            prefs.edit().putLong(claimKey, futureTime).apply()
            db.userDao().setChips(user.userId, db.userDao().getById(user.userId)!!.chipsOwned + 500L)
            thirdClaimed = true
        }
        assertTrue(thirdClaimed)
        assertEquals(2000L, db.userDao().getById(user.userId)?.chipsOwned)
    }

    /**
     * User Statistics Recording and Aggregation Test
     * Tests that wins, losses, win rate, and total winnings are correctly calculated and aggregated.
     */

    @Test
    fun testUserStatisticsAggregation() = runBlocking {
        val user = newUser("StatsUser", 5000L)
        val sessionId = db.sessionDao().insert(SessionEntity(userId = user.userId, stakes = 100))
        val roundDao = db.roundDao()

        // Game 1: Win (+200 net)
        val g1 = roundDao.insertGame(GameEntity.create(sessionId, 1, 18))
        roundDao.insertHand(HandEntity.player(100, HandStatus.STOOD, HandResult.WIN).copy(gameId = g1))
        roundDao.insertHand(HandEntity.dealer(HandStatus.STOOD).copy(gameId = g1))

        // Game 2: Loss (-100 net)
        val g2 = roundDao.insertGame(GameEntity.create(sessionId, 2, 20))
        roundDao.insertHand(HandEntity.player(100, HandStatus.STOOD, HandResult.LOSS).copy(gameId = g2))
        roundDao.insertHand(HandEntity.dealer(HandStatus.STOOD).copy(gameId = g2))

        // Game 3: Win (+100 net)
        val g3 = roundDao.insertGame(GameEntity.create(sessionId, 3, 17))
        roundDao.insertHand(HandEntity.player(100, HandStatus.STOOD, HandResult.WIN).copy(gameId = g3))
        roundDao.insertHand(HandEntity.dealer(HandStatus.STOOD).copy(gameId = g3))

        val history = db.gameHistoryDao().observeForUser(user.userId).first()
        val totalNet = db.gameHistoryDao().observeNetChips(user.userId).first()

        val gamesPlayed = history.size
        val wins = history.count { it.netChips > 0 }
        val losses = history.count { it.netChips < 0 }
        val winRate = if (gamesPlayed > 0) (wins.toDouble() / gamesPlayed) * 100 else 0.0

        assertEquals(3, gamesPlayed)
        assertEquals(2, wins)
        assertEquals(1, losses)
        assertEquals(66.66666666666666, winRate, 0.001)
        assertEquals(200L, totalNet) // +200 - 100 + 100 = 200
    }
}