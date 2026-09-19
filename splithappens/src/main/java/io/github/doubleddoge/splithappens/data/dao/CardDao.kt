package io.github.doubleddoge.splithappens.data.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import io.github.doubleddoge.splithappens.data.entity.CardEntity

@Dao
interface CardDao {

	// OnConflictStrategy.IGNORE makes seeding idempotent
	@Insert(onConflict = OnConflictStrategy.IGNORE)
	suspend fun insertAll(cards: List<CardEntity>)

	@Query("SELECT COUNT(*) FROM Cards")
	suspend fun count(): Int
}
