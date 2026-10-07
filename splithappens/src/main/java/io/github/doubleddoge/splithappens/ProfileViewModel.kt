package io.github.doubleddoge.splithappens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.doubleddoge.splithappens.data.dao.GameHistoryDao
import io.github.doubleddoge.splithappens.data.dao.UserDao
import io.github.doubleddoge.splithappens.data.entity.ProfilePictureEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

data class ProfileUiState(
    val userId: String = "",
    val displayName: String = "Player 1",
    val chipsOwned: Long = 2500,
    val profilePictureBytes: ByteArray? = null,
    val gamesPlayed: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
    val winRate: Double = 0.0,
    val isLoading: Boolean = true
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProfileUiState

        if (userId != other.userId) return false
        if (displayName != other.displayName) return false
        if (chipsOwned != other.chipsOwned) return false
        if (profilePictureBytes != null) {
            if (other.profilePictureBytes == null) return false
            if (!profilePictureBytes.contentEquals(other.profilePictureBytes)) return false
        } else if (other.profilePictureBytes != null) return false
        if (gamesPlayed != other.gamesPlayed) return false
        if (wins != other.wins) return false
        if (losses != other.losses) return false
        if (winRate != other.winRate) return false
        if (isLoading != other.isLoading) return false

        return true
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
        result = 31 * result + isLoading.hashCode()
        return result
    }
}

class ProfileViewModel(
    private val userDao: UserDao,
    private val gameHistoryDao: GameHistoryDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        observeProfileData()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeProfileData() {
        viewModelScope.launch {
            userDao.observeAll().flatMapLatest { users ->
                val user = users.firstOrNull()

                if (user == null) {
                    flowOf(ProfileUiState(isLoading = true))
                } else {
                    val picture = userDao.getPicture(user.userId)

                    combine(
                        gameHistoryDao.observeForUser(user.userId),
                        gameHistoryDao.observeNetChips(user.userId)
                    ) { historyList, _ ->
                        val total = historyList.size
                        val wins = historyList.count { it.netChips > 0 }
                        val losses = historyList.count { it.netChips < 0 }
                        val rate = if (total > 0) (wins.toDouble() / total) * 100 else 0.0

                        ProfileUiState(
                            userId = user.userId,
                            displayName = user.displayName,
                            chipsOwned = user.chipsOwned,
                            profilePictureBytes = picture,
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

    fun updateProfilePicture(bytes: ByteArray) {
        val currentUserId = _uiState.value.userId
        if (currentUserId.isBlank()) return

        viewModelScope.launch {
            userDao.savePicture(
                ProfilePictureEntity(
                    userId = currentUserId,
                    image = bytes
                )
            )
            // Refresh state with newly updated picture
            _uiState.value = _uiState.value.copy(profilePictureBytes = bytes)
        }
    }

    fun updateDisplayName(newName: String) {
        val currentUserId = _uiState.value.userId
        if (currentUserId.isBlank() || newName.isBlank()) return

        viewModelScope.launch {
            val user = userDao.getById(currentUserId)
            if (user != null) {
                userDao.update(user.copy(displayName = newName.trim()))
            }
        }
    }
}

class ProfileViewModelFactory(
    private val userDao: UserDao,
    private val gameHistoryDao: GameHistoryDao
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProfileViewModel(userDao, gameHistoryDao) as T
    }
}