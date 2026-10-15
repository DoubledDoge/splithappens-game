package io.github.doubleddoge.splithappens.data

import io.github.doubleddoge.splithappens.data.entity.UserEntity
import org.junit.Assert
import org.junit.Test
import java.util.UUID

class UserEntityTest {
    @Test
    fun defaultUserId_isValidUuid() {
        val user = UserEntity(displayName = "A", chipsOwned = 0)
        Assert.assertEquals(user.userId, UUID.fromString(user.userId).toString())
    }

    @Test
    fun defaultUserIds_areUnique() {
        val a = UserEntity(displayName = "A", chipsOwned = 0)
        val b = UserEntity(displayName = "A", chipsOwned = 0)
        Assert.assertNotEquals(a.userId, b.userId)
    }

    @Test
    fun defaultCreatedAt_isNow() {
        val before = System.currentTimeMillis()
        val user = UserEntity(displayName = "A", chipsOwned = 0)
        val after = System.currentTimeMillis()
        Assert.assertTrue(user.createdAt in before..after)
    }

    @Test
    fun explicitValues_arePreserved() {
        val user = UserEntity("id-1", "Neo", 2500, 42L)
        Assert.assertEquals("id-1", user.userId)
        Assert.assertEquals("Neo", user.displayName)
        Assert.assertEquals(2500L, user.chipsOwned)
        Assert.assertEquals(42L, user.createdAt)
    }
}