package io.github.doubleddoge.splithappens.game

import io.github.doubleddoge.splithappens.data.enum.Rank
import io.github.doubleddoge.splithappens.data.enum.Suit
import kotlin.random.Random

class Shoe(
    private val deckCount: Int = 1,
    private val random: Random = Random.Default,
    private val reshuffleAt: Double = 0.25,
) {
  private val cards = ArrayDeque<Card>()

  init {
    reshuffle()
  }

  val remaining
    get() = cards.size

  val needsReshuffle
    get() =
        remaining <
            deckCount *
                52 *
                reshuffleAt // check for round logic between rounds, NOT between hands! This
                            // prevents a hand being interrupted by a reshuffle.

  fun deal(): Card {
    if (cards.isEmpty()) reshuffle()
    return cards.removeFirst()
  }

  // Tests the next deal to come out in this order (player, dealer up, player, dealer hole, draws)
  fun stackTop(top: List<Card>) {
    cards.addAll(0, top)
  }

  fun reshuffle() {
    cards.clear()
    repeat(deckCount) {
      Suit.entries.forEach { s ->
        Rank.entries.forEach { r -> cards.add(Card(s, r)) }
      }
    }
    cards.shuffle(random)
  }
}
