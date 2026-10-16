package io.github.doubleddoge.splithappens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.doubleddoge.splithappens.data.dao.GameHistoryDao
import io.github.doubleddoge.splithappens.data.dao.UserDao
import io.github.doubleddoge.splithappens.data.entity.ProfilePictureEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

// PROFILE UI STATE

data class ProfileUiState(
    val userId: String = "default_player",
    val displayName: String = "Player 1",
    val chipsOwned: Long = 2500L,
    val profilePictureBytes: ByteArray? = null,
    val gamesPlayed: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
    val winRate: Double = 0.0,
    val isLoading: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProfileUiState) return false

        return userId == other.userId &&
                displayName == other.displayName &&
                chipsOwned == other.chipsOwned &&
                profilePictureBytes.contentEqualsNullable(other.profilePictureBytes) &&
                gamesPlayed == other.gamesPlayed &&
                wins == other.wins &&
                losses == other.losses &&
                winRate == other.winRate &&
                isLoading == other.isLoading
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

private fun ByteArray?.contentEqualsNullable(other: ByteArray?): Boolean {
    return when {
        this == null && other == null -> true
        this == null || other == null -> false
        else -> this.contentEquals(other)
    }
}

// PROFILE VIEW MODEL

class ProfileViewModel(
    private val userDao: UserDao,
    private val gameHistoryDao: GameHistoryDao,
    private val userId: String = "default_player"
) : ViewModel() {

    private val targetUserId = userId.ifBlank { "default_player" }

    private val _uiState = MutableStateFlow(
        ProfileUiState(
            userId = targetUserId,
            isLoading = true
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
                gameHistoryDao.observeForUser(targetUserId)
            ) { user, history ->
                if (user == null) {
                    ProfileUiState(
                        userId = targetUserId,
                        displayName = "Player 1",
                        chipsOwned = 2500L,
                        isLoading = false
                    )
                } else {
                    val gamesPlayed = history.size
                    val wins = history.count { it.netChips > 0 }
                    val losses = history.count { it.netChips < 0 }
                    val winRate = if (gamesPlayed > 0) {
                        (wins.toDouble() / gamesPlayed) * 100
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
                        isLoading = false
                    )
                }
            }.collect { state ->
                _uiState.value = state.copy(
                    profilePictureBytes = _uiState.value.profilePictureBytes
                )

                if (state.userId.isNotBlank() && _uiState.value.profilePictureBytes == null) {
                    loadProfilePicture(state.userId)
                }
            }
        }
    }

    private fun loadProfilePicture(userId: String) {
        viewModelScope.launch {
            val picture = userDao.getPicture(userId)
            _uiState.value = _uiState.value.copy(
                profilePictureBytes = picture
            )
        }
    }

    fun updateProfilePicture(bytes: ByteArray) {
        val currentUserId = _uiState.value.userId
        if (currentUserId.isBlank() || bytes.isEmpty()) return

        viewModelScope.launch {
            userDao.savePicture(
                ProfilePictureEntity(
                    userId = currentUserId,
                    image = bytes
                )
            )
            _uiState.value = _uiState.value.copy(
                profilePictureBytes = bytes
            )
        }
    }

    fun updateDisplayName(newName: String) {
        val currentUserId = _uiState.value.userId
        val cleanedName = newName.trim()
        if (currentUserId.isBlank() || cleanedName.isBlank()) return

        viewModelScope.launch {
            val currentUser = userDao.getById(currentUserId)
            if (currentUser != null) {
                userDao.update(
                    currentUser.copy(
                        displayName = cleanedName
                    )
                )
            }
        }
    }
}

// VIEW MODEL FACTORY

class ProfileViewModelFactory(
    private val userDao: UserDao,
    private val gameHistoryDao: GameHistoryDao,
    private val userId: String = "default_player"
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(
                userDao = userDao,
                gameHistoryDao = gameHistoryDao,
                userId = userId.ifBlank { "default_player" }
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}