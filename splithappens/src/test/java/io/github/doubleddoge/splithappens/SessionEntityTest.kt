package io.github.doubleddoge.splithappens.data.entity

import io.github.doubleddoge.splithappens.data.enum.GameMode
import io.github.doubleddoge.splithappens.data.enum.SessionStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionEntityTest {
    @Test
    fun defaults_areWildcardOngoingAndNotEnded() {
        val before = System.currentTimeMillis()
        val s = SessionEntity(userId = "u", stakes = 100)
        assertEquals(0L, s.sessionId)
        assertEquals(GameMode.WILDCARD, s.mode)
        assertEquals(SessionStatus.ONGOING, s.status)
        assertNull(s.endedAt)
        assertTrue(s.startedAt >= before)
    }
}