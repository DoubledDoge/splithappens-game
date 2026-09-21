package io.github.doubleddoge.splithappens.data.enums

// Has a multiplier to enable different payouts decided by game logic
enum class HandResult(val multiplier: Int) {
	LOSS(0),
	PUSH(1),                // Tie
	WIN(2),
	BLACKJACK(3),           // ace + a ten-value card
	QUEEN_JACK(10),         // queen + ace
	NATURAL_BLACKJACK(50),  // jack + ace
	TRIPLE_SEVEN(100),      // 21 with three 7s
	SIX_CARD(120),          // six cards without busting
	SEVEN_CARD(250);        // seven cards without busting

	fun payoutFor(bet: Long): Long = bet * multiplier
}