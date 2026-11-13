package io.github.doubleddoge.splithappens.data.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Transaction
import io.github.doubleddoge.splithappens.data.entity.GameEntity
import io.github.doubleddoge.splithappens.data.entity.HandCardEntity
import io.github.doubleddoge.splithappens.data.entity.HandEntity
import io.github.doubleddoge.splithappens.data.enum.HandOwner

// Everything that happened in one finished round, as it comes out of the game logic.
data class RoundRecord(
	val game: GameEntity,
	val hands: List<HandRecord>,
) {
	// Chips the player won or lost this round
	val netChips: Long
		get() = hands
			.filter { it.hand.owner == HandOwner.PLAYER }
			.sumOf { it.hand.payout - it.hand.betAmount } +
				game.insurancePayout - game.insuranceBet
}

data class HandRecord(
	val hand: HandEntity,
	val parentIndex: Int? = null,
	val cards: List<HandCardEntity>,
)

@Dao
abstract class RoundDao {
	@Insert
	abstract suspend fun insertGame(game: GameEntity): Long

	@Insert
	abstract suspend fun insertHand(hand: HandEntity): Long

	@Insert
	abstract suspend fun insertHandCards(cards: List<HandCardEntity>)

	@Query("SELECT * FROM Hands WHERE gameId = :gameId ORDER BY handId")
	abstract suspend fun getHands(gameId: Long): List<HandEntity>

	@Query("SELECT COUNT(*) FROM Games")
	abstract suspend fun countGames(): Int

	@Query("SELECT COUNT(*) FROM Hands")
	abstract suspend fun countHands(): Int

	@Query("SELECT COUNT(*) FROM HandCards")
	abstract suspend fun countHandCards(): Int

	//cascading deletion tests and constraints
	@Query("SELECT * FROM HandCards WHERE handId = :handId ORDER BY dealtOrder")
	abstract suspend fun getHandCards(handId: Long): List<HandCardEntity>

	@Query("DELETE FROM Games WHERE gameId = :gameId")
	abstract suspend fun deleteGame(gameId: Long)

	@Query("DELETE FROM Hands WHERE handId = :handId")
	abstract suspend fun deleteHand(handId: Long)

	@Query("UPDATE Users SET chipsOwned = chipsOwned + :delta WHERE userId = :userId")
	abstract suspend fun addChips(
		userId: String,
		delta: Long,
	)

	@Transaction
	open suspend fun saveRound(
		round: RoundRecord,
		userId: String,
	): Long {
		val gameId = insertGame(round.game)
		val handIds = ArrayList<Long>(round.hands.size)

		for (record in round.hands) {
			val parentId =
				record.parentIndex?.let { index ->
					require(index in handIds.indices) { "Split hand must come after its parent hand" }
					handIds[index]
				}

			val handId = insertHand(record.hand.copy(gameId = gameId, parentHandId = parentId))
			handIds += handId
			insertHandCards(record.cards.map { it.copy(handId = handId) })
		}

		addChips(userId, round.netChips)

		return gameId
	}
}
