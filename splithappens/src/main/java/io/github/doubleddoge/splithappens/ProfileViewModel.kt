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

// ================================================================
// PROFILE UI STATE
// ================================================================

data class ProfileUiState(
    val userId: String = "",
    val displayName: String = "Player 1",
    val chipsOwned: Long = 0L,
    val profilePictureBytes: ByteArray? = null,

    val gamesPlayed: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
    val winRate: Double = 0.0,

    val isLoading: Boolean = true
) {

    // ByteArray needs content-based equality
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProfileUiState) return false

        return userId == other.userId &&
                displayName == other.displayName &&
                chipsOwned == other.chipsOwned &&
                profilePictureBytes.contentEqualsNullable(
                    other.profilePictureBytes
                ) &&
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
        result = 31 * result +
                (profilePictureBytes?.contentHashCode() ?: 0)
        result = 31 * result + gamesPlayed
        result = 31 * result + wins
        result = 31 * result + losses
        result = 31 * result + winRate.hashCode()
        result = 31 * result + isLoading.hashCode()
        return result
    }
}

// Helper for comparing nullable ByteArrays
private fun ByteArray?.contentEqualsNullable(
    other: ByteArray?
): Boolean {
    return when {
        this == null && other == null -> true
        this == null || other == null -> false
        else -> this.contentEquals(other)
    }
}


// ================================================================
// PROFILE VIEW MODEL
// ================================================================

class ProfileViewModel(
    private val userDao: UserDao,
    private val gameHistoryDao: GameHistoryDao,
    private val userId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ProfileUiState(
            userId = userId,
            isLoading = true
        )
    )

    val uiState: StateFlow<ProfileUiState> =
        _uiState.asStateFlow()

    init {
        observeProfile()
    }

    // ============================================================
    // OBSERVE PROFILE
    // ============================================================

    private fun observeProfile() {

        if (userId.isBlank()) {
            _uiState.value = ProfileUiState(
                isLoading = false
            )
            return
        }

        viewModelScope.launch {

            combine(
                userDao.observeById(userId),
                gameHistoryDao.observeForUser(userId)
            ) { user, history ->

                if (user == null) {
                    ProfileUiState(
                        userId = userId,
                        isLoading = false
                    )
                } else {

                    val gamesPlayed = history.size

                    val wins = history.count {
                        it.netChips > 0
                    }

                    val losses = history.count {
                        it.netChips < 0
                    }

                    val winRate =
                        if (gamesPlayed > 0) {
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

                // Preserve the current picture while the rest of
                // the profile state updates.
                _uiState.value = state.copy(
                    profilePictureBytes =
                        _uiState.value.profilePictureBytes
                )

                // Load the picture if we don't have one yet.
                if (
                    state.userId.isNotBlank() &&
                    _uiState.value.profilePictureBytes == null
                ) {
                    loadProfilePicture(state.userId)
                }
            }
        }
    }

    // ============================================================
    // PROFILE PICTURE
    // ============================================================

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

        if (currentUserId.isBlank()) return
        if (bytes.isEmpty()) return

        viewModelScope.launch {

            userDao.savePicture(
                ProfilePictureEntity(
                    userId = currentUserId,
                    image = bytes
                )
            )

            _uiState.value =
                _uiState.value.copy(
                    profilePictureBytes = bytes
                )
        }
    }

    // ============================================================
    // DISPLAY NAME
    // ============================================================

    fun updateDisplayName(newName: String) {

        val currentUserId = _uiState.value.userId

        val cleanedName = newName.trim()

        if (currentUserId.isBlank()) return
        if (cleanedName.isBlank()) return

        viewModelScope.launch {

            val currentUser =
                userDao.getById(currentUserId)

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


// ================================================================
// VIEW MODEL FACTORY
// ================================================================

class ProfileViewModelFactory(
    private val userDao: UserDao,
    private val gameHistoryDao: GameHistoryDao,
    private val userId: String
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(
                userDao = userDao,
                gameHistoryDao = gameHistoryDao,
                userId = userId
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}