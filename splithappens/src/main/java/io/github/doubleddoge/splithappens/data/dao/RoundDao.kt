package io.github.doubleddoge.splithappens.data.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Transaction
import io.github.doubleddoge.splithappens.data.entity.GameEntity
import io.github.doubleddoge.splithappens.data.entity.HandCardEntity
import io.github.doubleddoge.splithappens.data.entity.HandEntity


// Everything that happened in one finished round, as it comes out of the game logic.
data class RoundRecord(
	val game: GameEntity,
	val hands: List<HandRecord>
)

data class HandRecord(
	val hand: HandEntity,
	val parentIndex: Int? = null,
	val cards: List<HandCardEntity>
)

@Dao
abstract class RoundDao {

	@Insert
	abstract suspend fun insertGame(game: GameEntity): Long

	@Insert
	abstract suspend fun insertHand(hand: HandEntity): Long

	@Insert
	abstract suspend fun insertHandCards(cards: List<HandCardEntity>)

	@Query("UPDATE Users SET chipsOwned = :chips WHERE userId = :userId")
	abstract suspend fun setChips(userId: String, chips: Long)

	/*
		End-of-round operations done here

		Room version 3.0.3 fixes a potential deadlock and an IllegalMonitorStateException here
	 */
	@Transaction
	open suspend fun saveRound(round: RoundRecord, userId: String, newChipsOwned: Long): Long {
		val gameId = insertGame(round.game)
		val handIds = ArrayList<Long>(round.hands.size)

		for (record in round.hands) {
			val parentId = record.parentIndex?.let { index ->
				require(index in handIds.indices) { "Split hand must come after its parent hand" }
				handIds[index]
			}

			val handId = insertHand(record.hand.copy(gameId = gameId, parentHandId = parentId))
			handIds += handId
			insertHandCards(record.cards.map { it.copy(handId = handId) })
		}

		setChips(userId, newChipsOwned)

		return gameId
	}
}
