package io.github.doubleddoge.splithappens.game

import io.github.doubleddoge.splithappens.game.enum.Rank
import io.github.doubleddoge.splithappens.game.enum.Suit
import org.jetbrains.annotations.VisibleForTesting
import kotlin.random.Random

class Shoe(
	private val deckCount: Int = DEFAULT_DECK_COUNT,
	private val random: Random = Random.Default,
	private val reshuffleAt: Double = DEFAULT_RESHUFFLE_THRESHOLD,
) {
	private val cards = ArrayDeque<Card>()

	init {
		reshuffle()
	}

	val remaining get() = cards.size

	/**
	 * True once the shoe drops below the reshuffle threshold. Check this between rounds, NOT
	 * between hands, so a hand is never interrupted by a reshuffle.
	 */
	val needsReshuffle get() = remaining < deckCount * CARDS_PER_DECK * reshuffleAt

	fun deal(): Card {
		if (cards.isEmpty()) reshuffle()
		return cards.removeFirst()
	}

	// TODO: Used for testing perhaps?
	@VisibleForTesting
	fun stackTop(top: List<Card>) {
		cards.addAll(0, top)
	}

	fun reshuffle() {
		cards.clear()
		repeat(deckCount) { cards.addAll(freshDeck()) }
		cards.shuffle(random)
	}

	private companion object {
		const val DEFAULT_DECK_COUNT = 1
		const val DEFAULT_RESHUFFLE_THRESHOLD = 0.25
		val CARDS_PER_DECK = Suit.entries.size * Rank.entries.size

		fun freshDeck(): List<Card> =
			Suit.entries.flatMap { suit -> Rank.entries.map { rank -> Card(suit, rank) } }
	}
}
