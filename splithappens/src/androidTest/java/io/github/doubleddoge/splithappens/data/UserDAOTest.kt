package io.github.doubleddoge.splithappens.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.doubleddoge.splithappens.data.entity.ProfilePictureEntity
import io.github.doubleddoge.splithappens.data.entity.SessionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserDaoTest : DatabaseTest() {
    private val dao get() = db.userDao()

    @Test
    fun insert_thenGetById() = runBlocking {
        val user = newUser("Neo", 500)
        assertEquals(user, dao.getById(user.userId))
    }

    @Test
    fun getById_unknownReturnsNull() = runBlocking {
        assertNull(dao.getById("missing"))
    }

    @Test
    fun insert_duplicateId_fails() {
        assertFailsWithException {
            val user = newUser()
            dao.insert(user)
        }
    }

    @Test
    fun update_changesRow() = runBlocking {
        val user = newUser("Old")
        dao.update(user.copy(displayName = "New"))
        assertEquals("New", dao.getById(user.userId)?.displayName)
    }

    @Test
    fun observeAll_isOrderedByDisplayName() = runBlocking {
        newUser("Charlie"); newUser("Alice"); newUser("Bob")
        assertEquals(listOf("Alice", "Bob", "Charlie"), dao.observeAll().first().map { it.displayName })
    }

    @Test
    fun observeById_emitsUserOrNull() = runBlocking {
        val user = newUser()
        assertEquals(user, dao.observeById(user.userId).first())
        assertNull(dao.observeById("missing").first())
    }

    @Test
    fun setChips_overwritesBalance() = runBlocking {
        val user = newUser(chips = 100)
        dao.setChips(user.userId, 999)
        assertEquals(999L, dao.getById(user.userId)?.chipsOwned)
    }

    @Test
    fun delete_removesUser() = runBlocking {
        val user = newUser()
        dao.delete(user)
        assertNull(dao.getById(user.userId))
    }

    @Test
    fun delete_cascadesToSessionsAndPicture() = runBlocking {
        val user = newUser()
        val sessionId = db.sessionDao().insert(SessionEntity(userId = user.userId, stakes = 100))
        dao.savePicture(ProfilePictureEntity(user.userId, byteArrayOf(1, 2, 3)))

        dao.delete(user)

        assertNull(db.sessionDao().getById(sessionId))
        assertNull(dao.getPicture(user.userId))
    }

    @Test
    fun picture_saveAndRead() = runBlocking {
        val user = newUser()
        val bytes = byteArrayOf(9, 8, 7)
        dao.savePicture(ProfilePictureEntity(user.userId, bytes))
        assertArrayEquals(bytes, dao.getPicture(user.userId))
    }

    @Test
    fun picture_saveAgainReplacesPrevious() = runBlocking {
        val user = newUser()
        dao.savePicture(ProfilePictureEntity(user.userId, byteArrayOf(1)))
        dao.savePicture(ProfilePictureEntity(user.userId, byteArrayOf(2, 2)))
        assertArrayEquals(byteArrayOf(2, 2), dao.getPicture(user.userId))
    }

    @Test
    fun picture_missingReturnsNull() = runBlocking {
        assertNull(dao.getPicture(newUser().userId))
    }

    @Test
    fun picture_withoutUser_violatesForeignKey() {
        assertFailsWithException {
            dao.savePicture(ProfilePictureEntity("ghost", byteArrayOf(1)))
        }
    }
}