package io.github.doubleddoge.splithappens

import androidx.lifecycle.ViewModel
import io.github.doubleddoge.splithappens.data.dao.GameHistoryDao
import io.github.doubleddoge.splithappens.data.dao.UserDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


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

    }
}