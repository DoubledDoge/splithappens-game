package io.github.doubleddoge.splithappens.data.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import io.github.doubleddoge.splithappens.data.model.HandOwner
import io.github.doubleddoge.splithappens.data.model.HandResult
import io.github.doubleddoge.splithappens.data.model.HandStatus

// Net profit can't be derived from bet x result
@Entity(
	tableName = "Hands",
	foreignKeys = [
		ForeignKey(
			entity = GameEntity::class,
			parentColumns = ["gameId"],
			childColumns = ["gameId"],
			onDelete = ForeignKey.CASCADE
		),
		ForeignKey(
			entity = HandEntity::class,
			parentColumns = ["handId"],
			childColumns = ["parentHandId"],
			onDelete = ForeignKey.CASCADE
		)
	],
	indices = [Index("gameId"), Index("parentHandId")]
)
data class HandEntity(
	@PrimaryKey(autoGenerate = true) val handId: Long = 0,
	val gameId: Long,
	val parentHandId: Long? = null,
	val owner: HandOwner,
	val betAmount: Long,
	val handStatus: HandStatus,
	val result: HandResult?,
	val payout: Long = 0 // Can now survive rule changes if need be
)
