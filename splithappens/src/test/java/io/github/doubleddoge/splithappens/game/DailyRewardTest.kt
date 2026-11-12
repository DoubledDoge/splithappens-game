package io.github.doubleddoge.splithappens.game

import io.github.doubleddoge.splithappens.util.DailyReward
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class DailyRewardTest {
	private val day = 24 * 60 * 60 * 1000L

	@Test
	fun firstClaimIsDue() = assertTrue(DailyReward.isDue(0L, 1_000L))

	@Test fun notDueBeforeADayHasPassed() =
		assertFalse(DailyReward.isDue(1_000L, 1_000L + day - 1))

	@Test fun dueAfterADay() = assertTrue(DailyReward.isDue(1_000L, 1_000L + day))

	@Test fun clockRollbackIsNeverDue() = assertFalse(DailyReward.isDue(10_000L, 5_000L))

	@Test fun rewardIs500() = assertEquals(500L, DailyReward.AMOUNT)
}