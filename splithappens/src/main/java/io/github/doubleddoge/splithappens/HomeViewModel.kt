package io.github.doubleddoge.splithappens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.doubleddoge.splithappens.data.dao.GameHistoryDao
import io.github.doubleddoge.splithappens.data.dao.UserDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val userId: String = "",
    val displayName: String = "Player 1",
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

        return userId == other.userId &&
                displayName == other.displayName &&
                chipsOwned == other.chipsOwned &&
                profilePictureBytes.contentEqualsNullable(other.profilePictureBytes) &&
                totalGamesPlayed == other.totalGamesPlayed &&
                totalWins == other.totalWins &&
                totalLosses == other.totalLosses &&
                winRate == other.winRate &&
                netChipsEarned == other.netChipsEarned &&
                isLoading == other.isLoading
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
                            displayName = "Player 1",
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
                            (wins.toDouble() / gamesPlayed) * 100
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

    fun addDailyReward(amount: Long) {
        val activeUserId = _uiState.value.userId
        if (activeUserId.isNotEmpty()) {
            viewModelScope.launch {
                val currentChips = _uiState.value.chipsOwned
                userDao.setChips(activeUserId, currentChips + amount)
            }
        }
    }
}

class HomeViewModelFactory(
    private val userDao: UserDao,
    private val gameHistoryDao: GameHistoryDao,
    private val currentUserId: String = "",
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                HomeViewModel(userDao, gameHistoryDao, currentUserId) as T
            }

            else -> {
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
}
