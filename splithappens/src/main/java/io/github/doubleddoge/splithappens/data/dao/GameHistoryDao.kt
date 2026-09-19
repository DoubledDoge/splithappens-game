package io.github.doubleddoge.splithappens.data.dao

import androidx.room3.Dao
import androidx.room3.Query
import io.github.doubleddoge.splithappens.data.view.GameHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface GameHistoryDao {

	@Query("SELECT * FROM GameHistory WHERE userId = :userId ORDER BY sessionStartedAt DESC, gameNum DESC")
	fun observeForUser(userId: String): Flow<List<GameHistory>>

	@Query("SELECT COALESCE(SUM(netChips), 0) FROM GameHistory WHERE userId = :userId")
	fun observeNetChips(userId: String): Flow<Long>
}
