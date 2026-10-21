package io.github.doubleddoge.splithappens.game

import io.github.doubleddoge.splithappens.data.enum.GameMode
import io.github.doubleddoge.splithappens.data.enum.HandResult

object HandEvaluator{
    private val naturals = setOf(HandResult.BLACKJACK, HandResult.QUEEN_JACK, HandResult.NATURAL_BLACKJACK)

    fun premium(cards: List<Card>, afterSplit:Boolean, mode: GameMode): HandResult? {
        val hand = cards.evaluateHand(afterSplit)
        if (hand.isBust) return null
        val candidates = buildList {
            if (hand.isBlackjack) add(HandResult.BLACKJACK)
            if (hand.isQueenAce) add(HandResult.QUEEN_JACK)
            if (hand.isJackAce) add(HandResult.NATURAL_BLACKJACK)
            if (hand.isTripleSeven) add(HandResult.TRIPLE_SEVEN)
            if (hand.isSixCard) add(HandResult.SIX_CARD)
            if (hand.isSevenCard) add(HandResult.SEVEN_CARD)
        }
        return candidates.filter { it in mode.allowedResults }.maxByOrNull { it.multiplier }
    }

    fun settle(
        player: List<Card>,
        afterSplit: Boolean,
        surrendered: Boolean,
        dealer: List<Card>,
        mode: GameMode,
    ): HandResult {
        if (surrendered) return HandResult.SURRENDER
        val p=player.evaluateHand(afterSplit)
        if (p.isBust) return HandResult.LOSS

        val premium = premium(player, afterSplit, mode)
        val d = dealer.evaluateHand()
        if (d.isBlackjack) return if (premium in naturals) HandResult.PUSH else HandResult.LOSS
        if (premium !=null) return premium

        return when {
            d.isBust || p.total > d.total -> HandResult.WIN
            p.total == d.total -> HandResult.PUSH
            else -> HandResult.LOSS
        }
    }
}
