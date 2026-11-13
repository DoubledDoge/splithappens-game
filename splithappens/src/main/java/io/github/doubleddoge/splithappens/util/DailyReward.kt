package io.github.doubleddoge.splithappens.util

object DailyReward {
	const val AMOUNT = 500L
	const val INTERVAL_MILLIS = 24L * 60 * 60 * 1000 // 24 hours

	// Due when nothing was claimed yet or a full interval has passed.
	fun isDue(lastClaimMillis: Long, nowMillis: Long): Boolean =
		nowMillis >= lastClaimMillis &&
				(lastClaimMillis == 0L || nowMillis - lastClaimMillis >= INTERVAL_MILLIS)
}