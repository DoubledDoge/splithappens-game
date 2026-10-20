package io.github.doubleddoge.splithappens

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.doubleddoge.splithappens.data.dao.UserDao
import io.github.doubleddoge.splithappens.data.entity.UserEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

data class LoginUiState(
    val username: String = "",
    val rememberMe: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class LoginViewModel(
    private val userDao: UserDao,
    context: Context
) : ViewModel() {

    private val prefs = context.getSharedPreferences("split_happens_auth", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(
        LoginUiState(
            username = prefs.getString("saved_username", "") ?: "",
            rememberMe = prefs.getBoolean("remember_me", true)
        )
    )
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onUsernameChange(name: String) {
        _uiState.value = _uiState.value.copy(username = name, errorMessage = null)
    }

    fun onRememberMeChange(checked: Boolean) {
        _uiState.value = _uiState.value.copy(rememberMe = checked)
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

                val user = if (existingUser != null) {
                    existingUser
                } else {
                    val newUser = UserEntity(
                        userId = UUID.randomUUID().toString(),
                        displayName = inputName,
                        chipsOwned = 2500L
                    )
                    userDao.insert(newUser)
                    newUser
                }

                val editor = prefs.edit()
                if (_uiState.value.rememberMe) {
                    editor.putBoolean("remember_me", true)
                    editor.putString("saved_username", user.displayName)
                    editor.putString("saved_user_id", user.userId)
                } else {
                    editor.remove("remember_me")
                    editor.remove("saved_username")
                    editor.remove("saved_user_id")
                }
                editor.apply()

                _uiState.value = _uiState.value.copy(isLoading = false)
                onSuccess(user)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Failed to log in"
                )
            }
        }
    }
}

class LoginViewModelFactory(
    private val userDao: UserDao,
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(userDao, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}