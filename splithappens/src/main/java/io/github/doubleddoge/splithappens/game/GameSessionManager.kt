package io.github.doubleddoge.splithappens.game

import io.github.doubleddoge.splithappens.data.dao.RoundDao
import io.github.doubleddoge.splithappens.data.dao.SessionDao
import io.github.doubleddoge.splithappens.data.entity.SessionEntity
import io.github.doubleddoge.splithappens.data.enum.GameMode
import io.github.doubleddoge.splithappens.data.enum.SessionStatus

//Mini repo to keep track of round/hand progress. Ends when the hand does.
class GameSessionManager(
    private val sessionDao: SessionDao,
    private val roundDao: RoundDao,
    private val userId: String,
    private val stakes: Long,
    private val mode: GameMode = GameMode.WILDCARD
) {
    var sessionId: Long = 0
        private set
    var gameNum: Int = 1
        private set
    var isSessionActive: Boolean = false
        private set

    suspend fun startSession(): Long {
        //new id is automatically inserted by Room.
        val session = SessionEntity(
            userId = userId,
            stakes = stakes,
            mode = mode,
            status = SessionStatus.ONGOING
        )
        sessionId = sessionDao.insert(session)
        gameNum = 1
        isSessionActive = true
        return sessionId
    }

    suspend fun saveAndNextRound(round: Round, userChips: Long) {
        check(isSessionActive) { "Session is not active" }
        check(round.phase == Phase.FINISHED) { "Round is not finished" }

        val record = round.toRecord(sessionId, gameNum)
        roundDao.saveRound(record, userId, userChips)
        //next hand that player gets, gets another game started.
        gameNum++
    }

    suspend fun endSession(status: SessionStatus = SessionStatus.COMPLETED) {
        if (isSessionActive) {
            sessionDao.setStatus(sessionId, status, System.currentTimeMillis())
            isSessionActive = false
        }
    }
}
