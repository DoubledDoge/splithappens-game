package io.github.doubleddoge.splithappens.game.model

enum class GameMode(val allowedResults: Set<HandResult>) {
	// Full experience
	WILDCARD(HandResult.entries.toSet()),

	// Regular Blackjack
	TRADITIONAL(
		setOf(
			HandResult.LOSS,
			HandResult.SURRENDER,
			HandResult.PUSH,
			HandResult.WIN,
			HandResult.BLACKJACK,
		),
	),
}