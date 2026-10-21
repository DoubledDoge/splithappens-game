package io.github.doubleddoge.splithappens

import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.doubleddoge.splithappens.data.dao.UserDao
import io.github.doubleddoge.splithappens.data.entity.UserEntity
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class UserProfileItem(
    val user: UserEntity,
    val lastUsedFormatted: String = "Recently",
)

data class LoginUiState(
    val username: String = "",
    val rememberMe: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSwitchProfileDialogOpen: Boolean = false,
    val existingProfiles: List<UserProfileItem> = emptyList(),
    val userPendingDelete: UserEntity? = null,
)

class LoginViewModel(
    private val userDao: UserDao,
    context: Context,
) : ViewModel() {

    private val prefs = context.getSharedPreferences("split_happens_auth", Context.MODE_PRIVATE)

    private val _uiState =
        MutableStateFlow(
            LoginUiState(
                username = prefs.getString("saved_username", "") ?: "",
                rememberMe = prefs.getBoolean("remember_me", true),
            )
        )
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        loadExistingProfiles()
    }

    private fun loadExistingProfiles() {
        viewModelScope.launch {
            userDao.observeAll().collect { users ->
                val items = users.map { user ->
                    UserProfileItem(
                        user = user,
                        lastUsedFormatted = "Saved",
                    )
                }
                _uiState.value = _uiState.value.copy(existingProfiles = items)
            }
        }
    }

    fun onUsernameChange(name: String) {
        _uiState.value = _uiState.value.copy(username = name, errorMessage = null)
    }

    fun onRememberMeChange(checked: Boolean) {
        _uiState.value = _uiState.value.copy(rememberMe = checked)
    }

    fun openSwitchProfileDialog() {
        _uiState.value = _uiState.value.copy(isSwitchProfileDialogOpen = true)
    }

    fun closeSwitchProfileDialog() {
        _uiState.value = _uiState.value.copy(isSwitchProfileDialogOpen = false)
    }

    fun selectProfile(user: UserEntity, onSuccess: () -> Unit) {
        _uiState.value =
            _uiState.value.copy(
                username = user.displayName,
                isSwitchProfileDialogOpen = false,
            )

        prefs.edit {
            if (_uiState.value.rememberMe) {
                putBoolean("remember_me", true)
                putString("saved_username", user.displayName)
                putString("saved_user_id", user.userId)
            }
        }

        onSuccess()
    }

    fun promptDeleteUser(user: UserEntity) {
        _uiState.value = _uiState.value.copy(userPendingDelete = user)
    }

    fun cancelDeleteUser() {
        _uiState.value = _uiState.value.copy(userPendingDelete = null)
    }

    fun confirmDeleteUser() {
        val userToDelete = _uiState.value.userPendingDelete ?: return
        viewModelScope.launch {
            try {
                userDao.delete(userToDelete)

                val savedUserId = prefs.getString("saved_user_id", null)
                if (savedUserId == userToDelete.userId) {
                    prefs.edit {
                        remove("saved_username")
                        remove("saved_user_id")
                    }
                }

                if (_uiState.value.username == userToDelete.displayName) {
                    _uiState.value = _uiState.value.copy(username = "")
                }

                _uiState.value = _uiState.value.copy(userPendingDelete = null)
            } catch (e: CancellationException) {
                throw e
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                _uiState.value =
                    _uiState.value.copy(
                        userPendingDelete = null,
                        errorMessage = e.localizedMessage ?: "Failed to delete profile",
                    )
            }
        }
    }

    fun checkAutoLogin(onSuccess: (UserEntity) -> Unit) {
        val shouldRemember = prefs.getBoolean("remember_me", false)
        val savedUserId = prefs.getString("saved_user_id", null)

        if (shouldRemember && !savedUserId.isNullOrBlank()) {
            viewModelScope.launch {
                val existing = userDao.getById(savedUserId)
                if (existing != null) {
                    onSuccess(existing)
                }
            }
        }
    }

    fun login(onSuccess: (UserEntity) -> Unit) {
        val inputName = _uiState.value.username.trim()

        if (inputName.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter a player name")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val allUsers = userDao.observeAll().first()
                val existingUser = allUsers.firstOrNull {
                    it.displayName.equals(inputName, ignoreCase = true)
                }

                val user =
                    if (existingUser != null) {
                        existingUser
                    } else {
                        val newUser =
                            UserEntity(
                                userId = UUID.randomUUID().toString(),
                                displayName = inputName,
                                chipsOwned = 2500L,
                            )
                        userDao.insert(newUser)
                        newUser
                    }

                prefs.edit {
                    if (_uiState.value.rememberMe) {
                        putBoolean("remember_me", true)
                        putString("saved_username", user.displayName)
                        putString("saved_user_id", user.userId)
                    } else {
                        remove("remember_me")
                        remove("saved_username")
                        remove("saved_user_id")
                    }
                }

                _uiState.value = _uiState.value.copy(isLoading = false)
                onSuccess(user)
            } catch (e: CancellationException) {
                throw e
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                _uiState.value =
                    _uiState.value.copy(
                        isLoading = false,
                        errorMessage = e.localizedMessage ?: "Failed to log in",
                    )
            }
        }
    }
}

class LoginViewModelFactory(
    private val userDao: UserDao,
    private val context: Context,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        when {
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(userDao, context) as T
            }

            else -> {
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
}