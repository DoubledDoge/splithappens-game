package io.github.doubleddoge.splithappens.data.enums


// Has a multiplier to enable different payouts decided by game logic
enum class HandResult(val multiplier: Double) {
	LOSS(0.0),
	SURRENDER(0.5),
	PUSH(1.0),                       // Tie
	WIN(2.0),
	BLACKJACK(3.0),                  // ace + a ten-value card
	QUEEN_JACK(10.0),                // queen + ace
	NATURAL_BLACKJACK(50.0),         // jack + ace
	TRIPLE_SEVEN(100.0),             // 21 with three 7s
	SIX_CARD(120.0),                 // six cards without busting
	SEVEN_CARD(250.0);               // seven cards without busting

	// Rounds down (always favour the house)
	fun payoutFor(bet: Long): Long = (bet * multiplier).toLong()
}