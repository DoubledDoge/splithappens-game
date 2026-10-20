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
import kotlinx.coroutines.launch

data class HomeUiState(
    val userId: String = "",
    val displayName: String = "Player 1",
    val chipsOwned: Long = 2500,
    val totalGamesPlayed: Int = 0,
    val totalWins: Int = 0,
    val netChipsEarned: Long = 0,
    val isLoading: Boolean = true,
)

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
                userDao.observeAll().flatMapLatest { users ->
                    flowOf(users.firstOrNull())
                }
            }

            userFlow.flatMapLatest { user ->
                if (user == null) {
                    flowOf(HomeUiState(isLoading = true))
                } else {
                    combine(
                        gameHistoryDao.observeForUser(user.userId),
                        gameHistoryDao.observeNetChips(user.userId),
                    ) { historyList, totalNetChips ->
                        HomeUiState(
                            userId = user.userId,
                            displayName = user.displayName,
                            chipsOwned = user.chipsOwned,
                            totalGamesPlayed = historyList.size,
                            totalWins = historyList.count { it.netChips > 0 },
                            netChipsEarned = totalNetChips,
                            isLoading = false,
                        )
                    }
                }
            }.collect { state ->
                _uiState.value = state
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