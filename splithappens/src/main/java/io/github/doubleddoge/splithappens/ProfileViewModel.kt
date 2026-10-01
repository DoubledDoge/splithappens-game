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


data class ProfileUiState(
    val displayName: String = "Player 1",
    val chipsOwned: Long = 2500,
    val gamesPlayed: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
    val winRate: Double = 0.0,
    val isLoading: Boolean = true

)
class ProfileViewModel(
    private val userDao: UserDao,
    private val gameHistoryDao: GameHistoryDao
): ViewModel(){
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init{
        observeProfileData()
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeProfileData(){
        viewModelScope.launch {
            userDao.observeAll().flatMapLatest { users ->
                val user = users.firstOrNull()

                if (user == null){
                    flowOf(ProfileUiState(isLoading = true))
                }else {
                    combine(
                        gameHistoryDao.observeForUser(user.userId),
                        gameHistoryDao.observeNetChips(user.userId)
                    ){historyList, _ ->
                        val total = historyList.size
                        val wins = historyList.count{it.netChips> 0}
                        val losses = historyList.count(){it.netChips < 0}
                        val rate = if (total > 0)(wins.toDouble()/total) * 100 else 0.0

                        ProfileUiState(
                            displayName = user.displayName,
                            chipsOwned = user.chipsOwned,
                            gamesPlayed = total,
                            wins = wins,
                            losses = losses,
                            winRate = rate,
                            isLoading = false
                        )
                    }
                }
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
}

class ProfileViewModelFactory(
    private val userDao: UserDao,
    private val gameHistoryDao: GameHistoryDao

): ViewModelProvider.Factory{
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProfileViewModelFactory(userDao, gameHistoryDao) as T
    }
}