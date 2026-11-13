package io.github.doubleddoge.splithappens.game.model

// Split can instead be determined if another hand points at it through parentHandId
enum class HandStatus {
	STOOD,
	BUSTED,
	BLACKJACK,
	SURRENDERED,
}
