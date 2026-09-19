package io.github.doubleddoge.splithappens.data.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.PrimaryKey

// Keeps image raw data out of every profile-list query
@Suppress("ArrayInDataClass") // never compared by value
@Entity(
	tableName = "ProfilePictures",
	foreignKeys = [
		ForeignKey(
			entity = UserEntity::class,
			parentColumns = ["userId"],
			childColumns = ["userId"],
			onDelete = ForeignKey.CASCADE
		)
	]
)
data class ProfilePictureEntity(
	@PrimaryKey val userId: String,
	val image: ByteArray
)
