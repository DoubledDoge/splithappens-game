package io.github.doubleddoge.splithappens.data.view

import androidx.room3.DatabaseView

/*
	Made into a derived view rather than a stored table since
	the idea that stats arriving on demand rather than stored
	and also acting as a log conflicts.

	This will still have the stats be read as simple rows but nothing
	desyncs over time.

	Could be debatably be turned into a table again.
 */
@DatabaseView(
	viewName = "GameHistory",
	value = """
		SELECT g.gameId AS gameId,
			   s.userId AS userId,
			   s.sessionId AS sessionId,
			   s.startedAt AS sessionStartedAt,
			   g.gameNum AS gameNum,
			   g.dealerTotal AS dealerTotal,
			   COUNT(h.handId) AS playerHands,
			   COALESCE(SUM(h.payout), 0) + g.insurancePayout AS netChips
		FROM Games g
		JOIN Sessions s ON s.sessionId = g.sessionId
		LEFT JOIN Hands h ON h.gameId = g.gameId AND h.owner = 'PLAYER'
		GROUP BY g.gameId
	"""
)
data class GameHistory(
	val gameId: Long,
	val userId: String,
	val sessionId: Long,
	val sessionStartedAt: Long,
	val gameNum: Int,
	val dealerTotal: Int,
	val playerHands: Int,
	val netChips: Long
)
