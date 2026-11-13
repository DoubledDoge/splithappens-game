package io.github.doubleddoge.splithappens.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.doubleddoge.splithappens.data.dao.GameHistoryDao
import io.github.doubleddoge.splithappens.data.dao.UserDao
import io.github.doubleddoge.splithappens.data.entity.ProfilePictureEntity
import io.github.doubleddoge.splithappens.util.PlayerName
import io.github.doubleddoge.splithappens.viewmodel.uistate.ProfileUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val MAX_NAME_LENGTH = 20
private const val PERCENTAGE_MULTIPLIER = 100

class ProfileViewModel(
	private val userDao: UserDao,
	private val gameHistoryDao: GameHistoryDao,
	private val userId: String = PlayerName.LEGACY_DEFAULT_USER_ID,
) : ViewModel() {

	private val targetUserId = userId.ifBlank { PlayerName.LEGACY_DEFAULT_USER_ID }

	private val _uiState =
		MutableStateFlow(
			ProfileUiState(
				userId = targetUserId,
				isLoading = true,
			)
		)

	val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

	init {
		observeProfile()
	}

	private fun observeProfile() {
		viewModelScope.launch {
			combine(
				userDao.observeById(targetUserId),
				gameHistoryDao.observeForUser(targetUserId),
				gameHistoryDao.observeNetChips(targetUserId),
			) { user, history, totalNetChips ->
				if (user == null) {
					ProfileUiState(
						userId = targetUserId,
						displayName = PlayerName.DEFAULT_NAME,
						chipsOwned = 2500L,
						isLoading = false,
					)
				} else {
					val gamesPlayed = history.size
					val wins = history.count { it.netChips > 0 }
					val losses = history.count { it.netChips < 0 }
					val winRate =
						if (gamesPlayed > 0) {
							(wins.toDouble() / gamesPlayed) * PERCENTAGE_MULTIPLIER
						} else {
							0.0
						}

					ProfileUiState(
						userId = user.userId,
						displayName = user.displayName,
						chipsOwned = user.chipsOwned,
						gamesPlayed = gamesPlayed,
						wins = wins,
						losses = losses,
						winRate = winRate,
						netChipsEarned = totalNetChips,
						isLoading = false,
					)
				}
			}
				.collect { state ->
					_uiState.value =
						state.copy(profilePictureBytes = _uiState.value.profilePictureBytes)

					if (state.userId.isNotBlank() && _uiState.value.profilePictureBytes == null) {
						loadProfilePicture(state.userId)
					}
				}
		}
	}

	private fun loadProfilePicture(userId: String) {
		viewModelScope.launch {
			val picture = userDao.getPicture(userId)
			_uiState.value = _uiState.value.copy(profilePictureBytes = picture)
		}
	}

	fun updateProfilePicture(bytes: ByteArray) {
		val currentUserId = _uiState.value.userId
		if (currentUserId.isBlank() || bytes.isEmpty()) return

		viewModelScope.launch {
			userDao.savePicture(
				ProfilePictureEntity(
					userId = currentUserId,
					image = bytes,
				)
			)
			_uiState.value = _uiState.value.copy(profilePictureBytes = bytes)
		}
	}

	fun updateDisplayName(newName: String) {
		val currentUserId = _uiState.value.userId
		val cleanedName = newName.trim()
		if (currentUserId.isBlank() || cleanedName.isBlank()) return

		if (cleanedName.length > MAX_NAME_LENGTH) {
			_uiState.update {
				it.copy(errorMessage = "Name must be $MAX_NAME_LENGTH characters or less.")
			}
			return
		}

		viewModelScope.launch {
			val allUsers = userDao.observeAll().first()
			val isTaken = allUsers.any {
				it.userId != currentUserId && it.displayName.equals(cleanedName, ignoreCase = true)
			}
			if (isTaken) {
				_uiState.update { it.copy(errorMessage = "That name is already taken by another profile.") }
				return@launch
			}

			val currentUser = userDao.getById(currentUserId)
			if (currentUser != null) {
				userDao.update(currentUser.copy(displayName = cleanedName))
				_uiState.update { it.copy(errorMessage = null) }
			}
		}
	}

	fun clearError() {
		_uiState.update { it.copy(errorMessage = null) }
	}
}


