package io.github.doubleddoge.splithappens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.doubleddoge.splithappens.data.dao.GameHistoryDao
import io.github.doubleddoge.splithappens.data.dao.UserDao
import io.github.doubleddoge.splithappens.data.entity.UserEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class SettingsUiState(
    val currentUserId: String = "",
    val displayName: String = "Player 1",
    val chipsOwned: Long = 2500L,
    val totalHands: Int = 0,
    val handsWon: Int = 0,
    val bestStreak: Int = 0,
    val netEarnings: Long = 0L,
    val isLoading: Boolean = true
)

class SettingsViewModel(
    private val userDao: UserDao,
    private val gameHistoryDao: GameHistoryDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observeUserData()
    }

    private fun observeUserData() {
        viewModelScope.launch {
            userDao.observeAll().collect { users ->
                val user = users.firstOrNull()
                if (user != null) {
                    combine(
                        gameHistoryDao.observeForUser(user.userId),
                        gameHistoryDao.observeNetChips(user.userId)
                    ) { historyList, netChips ->
                        val totalHands = historyList.size
                        val handsWon = historyList.count { it.netChips > 0 }

                        var currentStreak = 0
                        var maxStreak = 0
                        historyList.asReversed().forEach { round ->
                            if (round.netChips > 0) {
                                currentStreak++
                                if (currentStreak > maxStreak) maxStreak = currentStreak
                            } else if (round.netChips < 0) {
                                currentStreak = 0
                            }
                        }

                        SettingsUiState(
                            currentUserId = user.userId,
                            displayName = user.displayName,
                            chipsOwned = user.chipsOwned,
                            totalHands = totalHands,
                            handsWon = handsWon,
                            bestStreak = maxStreak,
                            netEarnings = netChips,
                            isLoading = false
                        )
                    }.collect { state ->
                        _uiState.value = state
                    }
                } else {
                    _uiState.value = SettingsUiState(isLoading = false)
                }
            }
        }
    }

    fun resetBankroll(amount: Long = 2500L) {
        val userId = _uiState.value.currentUserId
        if (userId.isNotEmpty()) {
            viewModelScope.launch {
                userDao.setChips(userId, amount)
            }
        }
    }
}

class SettingsViewModelFactory(
    private val userDao: UserDao,
    private val gameHistoryDao: GameHistoryDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(userDao, gameHistoryDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}