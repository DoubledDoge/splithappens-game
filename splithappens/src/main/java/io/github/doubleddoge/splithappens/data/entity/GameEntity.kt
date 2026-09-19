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
	indices = [Index(value = ["sessionId", "gameNum"], unique = true)]
)
data class GameEntity(
	@PrimaryKey(autoGenerate = true) val gameId: Long = 0,
	val sessionId: Long,
	val gameNum: Int,
	val dealerTotal: Int,
	val insuranceBet: Long = 0,
	val insurancePayout: Long = 0
)
