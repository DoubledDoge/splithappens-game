package io.github.doubleddoge.splithappens.data.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index

@Entity(
	tableName = "HandCards",
	primaryKeys = ["handId", "dealtOrder"], // Same card can appear in the same hand twice now
	foreignKeys = [
		ForeignKey(
			entity = HandEntity::class,
			parentColumns = ["handId"],
			childColumns = ["handId"],
			onDelete = ForeignKey.CASCADE
		),
		ForeignKey(
			entity = CardEntity::class,
			parentColumns = ["cardId"],
			childColumns = ["cardId"]
		)
	],
	indices = [Index("cardId")]
)
data class HandCardEntity(
	val handId: Long,
	val dealtOrder: Int,
	val cardId: Int,
	val isHoleCard: Boolean = false
)
