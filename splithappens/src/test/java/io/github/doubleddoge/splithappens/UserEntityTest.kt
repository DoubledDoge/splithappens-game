package io.github.doubleddoge.splithappens

import io.github.doubleddoge.splithappens.data.entity.UserEntity
import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UserEntityTest {
    @Test
    fun defaultUserId_isValidUuid() {
        val user = UserEntity(displayName = "A", chipsOwned = 0)
        assertEquals(user.userId, UUID.fromString(user.userId).toString())
    }

    @Test
    fun defaultUserIds_areUnique() {
        val a = UserEntity(displayName = "A", chipsOwned = 0)
        val b = UserEntity(displayName = "A", chipsOwned = 0)
        assertNotEquals(a.userId, b.userId)
    }

    @Test
    fun defaultCreatedAt_isNow() {
        val before = System.currentTimeMillis()
        val user = UserEntity(displayName = "A", chipsOwned = 0)
        val after = System.currentTimeMillis()
        assertTrue(user.createdAt in before..after)
    }

    @Test
    fun explicitValues_arePreserved() {
        val user = UserEntity("id-1", "Neo", 2500, 42L)
        assertEquals("id-1", user.userId)
        assertEquals("Neo", user.displayName)
        assertEquals(2500L, user.chipsOwned)
        assertEquals(42L, user.createdAt)
    }
}