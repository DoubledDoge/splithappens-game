package io.github.doubleddoge.splithappens

import io.github.doubleddoge.splithappens.game.Card

fun Card.toTableCard(uid: Long, isFaceUp: Boolean = true): TableCard{
    return TableCard(
        uid = uid,
        suit = this.suit,
        rank = this.rank,
        isFaceUp = isFaceUp
    )
}

fun List<Card>.toTableCards(hideHoleCard: Boolean = false): List<TableCard> {
    return mapIndexed { index, card ->
        val faceUp = !(hideHoleCard && index == 1)
        card.toTableCard(uid = index.toLong() + card.cardId * 100L, isFaceUp = faceUp)
    }
}