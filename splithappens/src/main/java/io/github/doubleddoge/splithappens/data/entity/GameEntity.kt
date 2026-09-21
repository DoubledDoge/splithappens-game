package io.github.doubleddoge.splithappens.data.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
	tableName = "Games",
	foreignKeys = [
		ForeignKey(
			entity = SessionEntity::class,
			parentColumns = ["sessionId"],
			childColumns = ["sessionId"],
			onDelete = ForeignKey.CASCADE
		)
	],
	// Its leading column also serves as the FK index on sessionId.
	indices = [Index(value = ["sessionId", "gameNum"], unique = true)]
)
data class GameEntity(
	@PrimaryKey(autoGenerate = true) val gameId: Long = 0,
	val sessionId: Long,
	val gameNum: Int,
	val dealerTotal: Int,
	val insuranceBet: Long = 0,
	val insurancePayout: Long = 0 // Deterministic based on dealerTotal
) {
	companion object {
		const val INSURANCE_RETURN_MULTIPLIER = 3 // Insurance pays 2:1 ratio so 1 + 2 = 3

		// A finished game shenanigans
		fun create(
			sessionId: Long,
			gameNum: Int,
			dealerTotal: Int,
			insuranceBet: Long = 0,
			dealerHadBlackjack: Boolean = false
		): GameEntity {
			require(insuranceBet >= 0) { "Insurance bet cannot be negative" }

			return GameEntity(
				sessionId = sessionId,
				gameNum = gameNum,
				dealerTotal = dealerTotal,
				insuranceBet = insuranceBet,
				insurancePayout = if (dealerHadBlackjack) insuranceBet * INSURANCE_RETURN_MULTIPLIER else 0L
			)
		}
	}
}