package io.github.doubleddoge.splithappens.game

import io.github.doubleddoge.splithappens.game.model.HandResult
import org.junit.Test
import kotlin.test.assertEquals

class OutcomeTextTest {
	@Test
	fun split_winAndLoss() =
		assertEquals("SPLIT: 1 WON, 1 LOST", outcomeMessage(listOf(HandResult.WIN, HandResult.LOSS), false))
	@Test fun split_winAndPush() =
		assertEquals("SPLIT: 1 WON, 1 PUSH", outcomeMessage(listOf(HandResult.WIN, HandResult.PUSH), false))
	@Test fun split_lossAndPush() =
		assertEquals("SPLIT: 1 LOST, 1 PUSH", outcomeMessage(listOf(HandResult.LOSS, HandResult.PUSH), false))
	@Test fun split_allPush() =
		assertEquals("ALL HANDS PUSHED", outcomeMessage(listOf(HandResult.PUSH, HandResult.PUSH), false))
	@Test fun lossIsBustedOnlyWhenBust() {
		assertEquals("BUSTED!", outcomeMessage(listOf(HandResult.LOSS), true))
		assertEquals("DEALER WINS", outcomeMessage(listOf(HandResult.LOSS), false))
	}
}