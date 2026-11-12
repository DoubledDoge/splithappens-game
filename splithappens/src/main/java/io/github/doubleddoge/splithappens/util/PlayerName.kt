package io.github.doubleddoge.splithappens.util

object PlayerName {
	const val LEGACY_DEFAULT_USER_ID = "default_player"
	const val DEFAULT_NAME = "Player 1"
	const val MAX_LENGTH = 20
	const val BLANK_MESSAGE = "Please enter a player name"
	const val TOO_LONG_MESSAGE = "Maximum $MAX_LENGTH characters allowed"
	const val INVALID_MESSAGE = "Only letters A-Z are allowed (max $MAX_LENGTH chars)"

	fun sanitize(raw: String): String = raw.filter { it.isAsciiLetter() }
	fun isValid(name: String): Boolean = name.length <= MAX_LENGTH && name.all { it.isAsciiLetter() }
}

private fun Char.isAsciiLetter() = this in 'a'..'z' || this in 'A'..'Z'