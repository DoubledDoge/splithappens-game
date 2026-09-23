package io.github.doubleddoge.splithappens.data.enums

// Simply helps the backend determine if we could use our special payouts for queenjack, etc or not
enum class GameMode(
    val allowedResults: Set<HandResult>,
) {
    WILDCARD((HandResult.entries.toSet())), // Full Experience (default)
    TRADITIONAL(
        (
            setOf(
                HandResult.LOSS,
                HandResult.SURRENDER,
                HandResult.PUSH,
                HandResult.WIN,
                HandResult.BLACKJACK,
            )
        ),
    ), // Regular Blackjack
}
