package io.github.doubleddoge.splithappens.data.model

// Enums also stored natively
enum class SessionStatus {
	ONGOING,
	COMPLETED
}

enum class HandOwner {
	PLAYER,
	DEALER
}

enum class HandStatus {
	ACTIVE, // Might never reach database, rounds written at the end
	STOOD,
	BUSTED,
	BLACKJACK,
	SURRENDERED,
	SPLIT // Could overlap with child hands
}

enum class HandResult {
	IN_PROGRESS, // Might never reach database, rounds written at the end
	WIN,
	LOSS,
	PUSH
}

enum class Suit {
	CLUBS,
	DIAMONDS,
	HEARTS,
	SPADES
}

enum class Rank {
	ACE,
	TWO,
	THREE,
	FOUR,
	FIVE,
	SIX,
	SEVEN,
	EIGHT,
	NINE,
	TEN,
	JACK,
	QUEEN,
	KING
}

