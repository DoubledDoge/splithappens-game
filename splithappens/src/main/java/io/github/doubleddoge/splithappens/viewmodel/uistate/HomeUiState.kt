package io.github.doubleddoge.splithappens.viewmodel.uistate

import io.github.doubleddoge.splithappens.util.PlayerName

data class HomeUiState(
	val userId: String = "",
	val displayName: String = PlayerName.DEFAULT_NAME,
	val chipsOwned: Long = 2500,
	val profilePictureBytes: ByteArray? = null,
	val totalGamesPlayed: Int = 0,
	val totalWins: Int = 0,
	val totalLosses: Int = 0,
	val winRate: Double = 0.0,
	val netChipsEarned: Long = 0,
	val isLoading: Boolean = true,
) {
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is HomeUiState) return false

		return (userId == other.userId) &&
				(displayName == other.displayName) &&
				(chipsOwned == other.chipsOwned) &&
				(profilePictureBytes.contentEqualsNullable(other.profilePictureBytes)) &&
				(totalGamesPlayed == other.totalGamesPlayed) &&
				(totalWins == other.totalWins) &&
				(totalLosses == other.totalLosses) &&
				(winRate == other.winRate) &&
				(netChipsEarned == other.netChipsEarned) &&
				(isLoading == other.isLoading)
	}

	override fun hashCode(): Int {
		var result = userId.hashCode()
		result = 31 * result + displayName.hashCode()
		result = 31 * result + chipsOwned.hashCode()
		result = 31 * result + (profilePictureBytes?.contentHashCode() ?: 0)
		result = 31 * result + totalGamesPlayed
		result = 31 * result + totalWins
		result = 31 * result + totalLosses
		result = 31 * result + winRate.hashCode()
		result = 31 * result + netChipsEarned.hashCode()
		result = 31 * result + isLoading.hashCode()
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