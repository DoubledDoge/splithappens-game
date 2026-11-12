package io.github.doubleddoge.splithappens.game

import io.github.doubleddoge.splithappens.data.dao.RoundDao
import io.github.doubleddoge.splithappens.data.dao.SessionDao
import io.github.doubleddoge.splithappens.data.entity.SessionEntity
import io.github.doubleddoge.splithappens.game.model.GameMode
import io.github.doubleddoge.splithappens.data.enum.Phase
import io.github.doubleddoge.splithappens.data.enum.SessionStatus

//Mini repo to keep track of round/hand progress
class GameSessionManager(
	private val sessionDao: SessionDao,
	private val roundDao: RoundDao,
	private val userId: String,
	private val stakes: Long,
	private val mode: GameMode = GameMode.WILDCARD,
) {
	var sessionId: Long = NO_SESSION_ID
		private set
	var gameNum: Int = FIRST_GAME_NUMBER
		private set
	var isSessionActive: Boolean = false
		private set

	suspend fun startSession(): Long {
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

	suspend fun saveAndNextRound(round: Round) {
		check(isSessionActive) { "Session is not active" }
		check(round.phase == Phase.FINISHED) { "Round is not finished" }

		val record = round.toRecord(sessionId, gameNum)
		roundDao.saveRound(record, userId)
		//next hand that player gets, gets another game started.
		gameNum++
	}

	suspend fun endSession(status: SessionStatus = SessionStatus.COMPLETED) {
		if (isSessionActive) {
			sessionDao.setStatus(sessionId, status, System.currentTimeMillis())
			isSessionActive = false
		}
	}

	private companion object {
		const val NO_SESSION_ID = 0L
		const val FIRST_GAME_NUMBER = 1
	}
}
