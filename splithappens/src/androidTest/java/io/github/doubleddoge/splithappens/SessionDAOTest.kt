package io.github.doubleddoge.splithappens.data.dao

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.doubleddoge.splithappens.data.entity.SessionEntity
import io.github.doubleddoge.splithappens.data.enum.GameMode
import io.github.doubleddoge.splithappens.data.enum.SessionStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SessionDaoTest : DatabaseTest() {
    private val dao get() = db.sessionDao()

    @Test
    fun insert_returnsGeneratedIds_andGetByIdRoundTrips() = runBlocking {
        val user = newUser()
        val a = dao.insert(SessionEntity(userId = user.userId, stakes = 100, mode = GameMode.TRADITIONAL))
        val b = dao.insert(SessionEntity(userId = user.userId, stakes = 200))
        assertNotEquals(a, b)
        val loaded = dao.getById(a)!!
        assertEquals(a, loaded.sessionId)
        assertEquals(100L, loaded.stakes)
        assertEquals(GameMode.TRADITIONAL, loaded.mode)
        assertEquals(SessionStatus.ONGOING, loaded.status)
    }

    @Test
    fun getById_unknownReturnsNull() = runBlocking {
        assertNull(dao.getById(12345))
    }

    @Test
    fun insert_withoutUser_violatesForeignKey() {
        assertFailsWithException { dao.insert(SessionEntity(userId = "ghost", stakes = 1)) }
    }

    @Test
    fun observeForUser_newestFirst_andOnlyThatUser() = runBlocking {
        val u1 = newUser("One")
        val u2 = newUser("Two")
        dao.insert(SessionEntity(userId = u1.userId, stakes = 1, startedAt = 100))
        dao.insert(SessionEntity(userId = u1.userId, stakes = 2, startedAt = 300))
        dao.insert(SessionEntity(userId = u1.userId, stakes = 3, startedAt = 200))
        dao.insert(SessionEntity(userId = u2.userId, stakes = 9, startedAt = 999))

        assertEquals(listOf(2L, 3L, 1L), dao.observeForUser(u1.userId).first().map { it.stakes })
    }

    @Test
    fun setStatus_updatesStatusAndEndedAt() = runBlocking {
        val user = newUser()
        val id = dao.insert(SessionEntity(userId = user.userId, stakes = 100))
        dao.setStatus(id, SessionStatus.COMPLETED, endedAt = 5555)
        val s = dao.getById(id)!!
        assertEquals(SessionStatus.COMPLETED, s.status)
        assertEquals(5555L, s.endedAt)
    }

    @Test
    fun setStatus_canClearEndedAt() = runBlocking {
        val user = newUser()
        val id = dao.insert(SessionEntity(userId = user.userId, stakes = 100, endedAt = 1))
        dao.setStatus(id, SessionStatus.ONGOING, endedAt = null)
        assertNull(dao.getById(id)!!.endedAt)
    }
}
