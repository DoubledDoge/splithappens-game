package io.github.doubleddoge.splithappens
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.doubleddoge.splithappens.data.SplitHappensDatabase
import org.junit.runner.RunWith
import org.junit.Before
import androidx.room3.Room
import io.github.doubleddoge.splithappens.data.dao.HandRecord
import io.github.doubleddoge.splithappens.data.dao.RoundRecord
import io.github.doubleddoge.splithappens.data.entity.CardEntity
import io.github.doubleddoge.splithappens.data.entity.GameEntity
import io.github.doubleddoge.splithappens.data.entity.HandCardEntity
import io.github.doubleddoge.splithappens.data.entity.HandEntity
import io.github.doubleddoge.splithappens.data.entity.SessionEntity
import io.github.doubleddoge.splithappens.data.entity.UserEntity
import io.github.doubleddoge.splithappens.data.enum.HandResult
import io.github.doubleddoge.splithappens.data.enum.HandStatus
import io.github.doubleddoge.splithappens.data.enum.Rank
import io.github.doubleddoge.splithappens.data.enum.Suit
import kotlinx.coroutines.flow.first


import android.content.Context
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test

@RunWith(AndroidJUnit4::class)
class RoundDaoTest {
    private lateinit var db: SplitHappensDatabase

    @Before fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder<SplitHappensDatabase>(context)
            .setDriver(AndroidSQLiteDriver())
            .build()
    }

    @After fun tearDown() = db.close()

    @Test fun saveRound_updatesChipsAndHistory() = runBlocking {
        val user = UserEntity(displayName = "Tester", chipsOwned = 1000)
        db.userDao().insert(user)
        db.cardDao().insertAll(CardEntity.standardDeck())
        val sessionId = db.sessionDao().insert(SessionEntity(userId = user.userId, stakes = 10))

        fun card(order: Int, s: Suit, r: Rank) =
            HandCardEntity(handId = 0, dealtOrder = order, cardId = CardEntity.idOf(s, r))

        val round = RoundRecord(
            game = GameEntity.create(sessionId, gameNum = 1, dealerTotal = 19),
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

        db.roundDao().saveRound(round, user.userId, newChipsOwned = 0)

        assertEquals(1010L, db.userDao().getById(user.userId)!!.chipsOwned)
        assertEquals(10L, db.gameHistoryDao().observeNetChips(user.userId).first())
        assertEquals(1, db.gameHistoryDao().observeForUser(user.userId).first().single().playerHands)
    }
}