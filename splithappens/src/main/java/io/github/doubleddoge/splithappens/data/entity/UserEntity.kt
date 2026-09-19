package io.github.doubleddoge.splithappens.data.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import java.util.UUID

@Entity(tableName = "Users")
data class UserEntity(
	@PrimaryKey val userId: String = UUID.randomUUID().toString(), // Must be globally unique
	val displayName: String,
	val chipsOwned: Long,
	val createdAt: Long = System.currentTimeMillis()
)
