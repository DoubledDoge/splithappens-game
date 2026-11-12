package io.github.doubleddoge.splithappens.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.doubleddoge.splithappens.data.dao.GameHistoryDao
import io.github.doubleddoge.splithappens.data.dao.UserDao
import io.github.doubleddoge.splithappens.util.PlayerName
import io.github.doubleddoge.splithappens.viewmodel.uistate.HomeUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val PERCENTAGE_MULTIPLIER = 100.0

class HomeViewModel(
	private val userDao: UserDao,
	private val gameHistoryDao: GameHistoryDao,
	private val currentUserId: String = "",
) : ViewModel() {
	private val _uiState = MutableStateFlow(HomeUiState())
	val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

	init {
		observeUserDataAndStats()
	}

	@OptIn(ExperimentalCoroutinesApi::class)
	private fun observeUserDataAndStats() {
		viewModelScope.launch {
			val userFlow = if (currentUserId.isNotBlank()) {
				userDao.observeById(currentUserId)
			} else {
				userDao.observeAll().map { users ->
					users.firstOrNull()
				}
			}

			userFlow.flatMapLatest { user ->
				if (user == null) {
					flowOf(
						HomeUiState(
							userId = currentUserId,
							displayName = PlayerName.DEFAULT_NAME,
							chipsOwned = 2500L,
							isLoading = false,
						)
					)
				} else {
					combine(
						gameHistoryDao.observeForUser(user.userId),
						gameHistoryDao.observeNetChips(user.userId),
					) { historyList, totalNetChips ->
						val gamesPlayed = historyList.size
						val wins = historyList.count { it.netChips > 0 }
						val losses = historyList.count { it.netChips < 0 }
						val winRate = if (gamesPlayed > 0) {
							(wins.toDouble() / gamesPlayed) * PERCENTAGE_MULTIPLIER
						} else {
							0.0
						}
						HomeUiState(
							userId = user.userId,
							displayName = user.displayName,
							chipsOwned = user.chipsOwned,
							profilePictureBytes = null,
							totalGamesPlayed = gamesPlayed,
							totalWins = wins,
							totalLosses = losses,
							winRate = winRate,
							netChipsEarned = totalNetChips,
							isLoading = false,
						)
					}
				}
			}.catch { e ->
				if (e !is IllegalStateException || e.message?.contains("closed") != true) throw e
			}.collect { state ->
				val existingPicture = if (state.userId == _uiState.value.userId) {
					_uiState.value.profilePictureBytes
				} else {
					null
				}
				_uiState.value = state.copy(profilePictureBytes = existingPicture)

				if (state.userId.isNotBlank() && existingPicture == null) {
					refreshProfilePicture()
				}
			}
		}
	}

	fun refreshProfilePicture() {
		val activeUserId = _uiState.value.userId
		if (activeUserId.isNotEmpty()) {
			viewModelScope.launch {
				val picture = userDao.getPicture(activeUserId)
				_uiState.update { it.copy(profilePictureBytes = picture) }
			}
		}
	}
}


