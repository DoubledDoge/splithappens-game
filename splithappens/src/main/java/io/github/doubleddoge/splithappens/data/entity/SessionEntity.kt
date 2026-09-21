package io.github.doubleddoge.splithappens.data.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import io.github.doubleddoge.splithappens.data.enums.SessionStatus

@Entity(
	tableName = "Sessions",
	foreignKeys = [
		ForeignKey(
			entity = UserEntity::class,
			parentColumns = ["userId"],
			childColumns = ["userId"],
			onDelete = ForeignKey.CASCADE
		)
	],
	indices = [Index("userId")]
)
data class SessionEntity(
	@PrimaryKey(autoGenerate = true) val sessionId: Long = 0,
	val userId: String,
	val stakes: Long,
	val status: SessionStatus = SessionStatus.ONGOING,
	val startedAt: Long = System.currentTimeMillis(),
	val endedAt: Long? = null
)
