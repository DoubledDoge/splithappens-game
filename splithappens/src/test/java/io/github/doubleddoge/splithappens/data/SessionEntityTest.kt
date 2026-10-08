package io.github.doubleddoge.splithappens.data

import io.github.doubleddoge.splithappens.data.entity.SessionEntity
import io.github.doubleddoge.splithappens.data.enum.GameMode
import io.github.doubleddoge.splithappens.data.enum.SessionStatus
import org.junit.Assert
import org.junit.Test

class SessionEntityTest {
    @Test
    fun defaults_areWildcardOngoingAndNotEnded() {
        val before = System.currentTimeMillis()
        val s = SessionEntity(userId = "u", stakes = 100)
        Assert.assertEquals(0L, s.sessionId)
        Assert.assertEquals(GameMode.WILDCARD, s.mode)
        Assert.assertEquals(SessionStatus.ONGOING, s.status)
        Assert.assertNull(s.endedAt)
        Assert.assertTrue(s.startedAt >= before)
    }
}