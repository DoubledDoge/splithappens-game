package io.github.doubleddoge.splithappens.data.entity

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import io.github.doubleddoge.splithappens.game.model.Rank
import io.github.doubleddoge.splithappens.game.model.Suit

@Entity(
	tableName = "Cards",
	indices = [Index(value = ["suit", "rank"], unique = true)],
)
data class CardEntity(
	@PrimaryKey
	val cardId: Int, // Stable for a given suit and rank, so exports and imports keep working
	val suit: Suit,
	val rank: Rank,
) {
	companion object {
		fun idOf(
			suit: Suit,
			rank: Rank,
		): Int = suit.ordinal * Rank.entries.size + rank.ordinal

		fun standardDeck(): List<CardEntity> =
			Suit.entries.flatMap { suit ->
				Rank.entries.map { rank -> CardEntity(idOf(suit, rank), suit, rank) }
			}
	}
}
