package io.github.doubleddoge.splithappens.viewmodel.uistate

import io.github.doubleddoge.splithappens.util.PlayerName

data class ProfileUiState(
	val userId: String = PlayerName.LEGACY_DEFAULT_USER_ID,
	val displayName: String = PlayerName.DEFAULT_NAME,
	val chipsOwned: Long = 2500L,
	val profilePictureBytes: ByteArray? = null,
	val gamesPlayed: Int = 0,
	val wins: Int = 0,
	val losses: Int = 0,
	val winRate: Double = 0.0,
	val netChipsEarned: Long = 0L,
	val isLoading: Boolean = true,
	val errorMessage: String? = null,
) {
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is ProfileUiState) return false

		return (userId == other.userId) &&
				(displayName == other.displayName) &&
				(chipsOwned == other.chipsOwned) &&
				(profilePictureBytes.contentEqualsNullable(other.profilePictureBytes)) &&
				(gamesPlayed == other.gamesPlayed) &&
				(wins == other.wins) &&
				(losses == other.losses) &&
				(winRate == other.winRate) &&
				(netChipsEarned == other.netChipsEarned) &&
				(isLoading == other.isLoading) &&
				(errorMessage == other.errorMessage)
	}

	override fun hashCode(): Int {
		var result = userId.hashCode()
		result = 31 * result + displayName.hashCode()
		result = 31 * result + chipsOwned.hashCode()
		result = 31 * result + (profilePictureBytes?.contentHashCode() ?: 0)
		result = 31 * result + gamesPlayed
		result = 31 * result + wins
		result = 31 * result + losses
		result = 31 * result + winRate.hashCode()
		result = 31 * result + netChipsEarned.hashCode()
		result = 31 * result + isLoading.hashCode()
		result = 31 * result + errorMessage.hashCode()
		return result
	}
}

private fun ByteArray?.contentEqualsNullable(other: ByteArray?): Boolean {
	return when {
		this == null && other == null -> true
		this == null || other == null -> false
		else -> this.contentEquals(other)
	}
}