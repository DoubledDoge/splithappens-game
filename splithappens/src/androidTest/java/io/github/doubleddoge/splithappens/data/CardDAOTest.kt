package io.github.doubleddoge.splithappens.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.doubleddoge.splithappens.data.entity.CardEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CardDaoTest : DatabaseTest() {
    private val dao get() = db.cardDao()

    @Test
    fun count_emptyIsZero() = runBlocking {
        assertEquals(0, dao.count())
    }

    @Test
    fun insertAll_seedsFullDeck() = runBlocking {
        dao.insertAll(CardEntity.standardDeck())
        assertEquals(52, dao.count())
    }

    @Test
    fun insertAll_isIdempotent() = runBlocking {
        dao.insertAll(CardEntity.standardDeck())
        dao.insertAll(CardEntity.standardDeck())
        assertEquals(52, dao.count())
    }

    @Test
    fun insertAll_topUpsPartialDeck() = runBlocking {
        dao.insertAll(CardEntity.standardDeck().take(10))
        dao.insertAll(CardEntity.standardDeck())
        assertEquals(52, dao.count())
    }
}