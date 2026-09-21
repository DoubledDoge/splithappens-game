package io.github.doubleddoge.splithappens.data.enums

enum class HandStatus {
	ACTIVE, // Might never reach database, rounds written at the end
	STOOD,
	BUSTED,
	BLACKJACK,
	SURRENDERED,
	SPLIT // Could overlap with child hands
}
