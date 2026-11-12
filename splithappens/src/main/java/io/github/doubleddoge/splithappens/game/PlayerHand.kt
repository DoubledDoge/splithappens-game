package io.github.doubleddoge.splithappens.game

import io.github.doubleddoge.splithappens.game.model.HandStatus

class PlayerHand(
	val cards: MutableList<Card>,
	var bet: Long,
	val parentIndex: Int? = null,
) {
	var afterSplit = parentIndex != null
	var isDoubled = false
	var stood = false
	var surrendered = false

	val value
		get() = HandValue(cards, isSplit = afterSplit)

	fun status(): HandStatus =
		if (surrendered) HandStatus.SURRENDERED else value.toHandStatus()

	fun snapshot(): PlayerHand =
		PlayerHand(cards.toMutableList(), bet, parentIndex).also {
			it.afterSplit = afterSplit
			it.isDoubled = isDoubled
			it.stood = stood
			it.surrendered = surrendered
		}
}

fun List<PlayerHand>.snapshot(): List<PlayerHand> = map { it.snapshot() }