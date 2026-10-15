package io.github.doubleddoge.splithappens.game

import io.github.doubleddoge.splithappens.data.enum.Rank
import kotlin.random.Random
import io.github.doubleddoge.splithappens.data.enum.Suit
class Shoe(
    private val deckCount: Int=1,
    private val random: Random=Random.Default,
    private val reshuffleAt: Double=0.25,
){
    private val cards = ArrayDeque<Card>()

    init {reshuffle()}

    //check for round logic between rounds, NOT between hands! This prevents a hand being interrupted by a reshuffle.
    val cardsLeft get()= cards.size
    val needsReshuffle get() = cardsLeft < deckCount * 52 * reshuffleAt

    fun deal():Card{
        if (cards.isEmpty()) reshuffle()
        return cards.removeFirst()
    }

    fun reshuffle(){
        cards.clear()
        repeat(deckCount) {
        Suit.entries.forEach { s-> Rank.entries.forEach { r->cards.add(Card(s,r)) }
        }
            cards.shuffle(random)
        }
    }
}