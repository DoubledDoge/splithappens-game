package io.github.doubleddoge.splithappens

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class MenuUiState(
    val isRulesBookletOpen: Boolean = false,
    val isLogoutConfirmOpen: Boolean = false,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true
)

@Suppress("unused")
class MenuViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("split_happens_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(MenuUiState())
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    init {
        loadPreferences()
    }

    fun loadPreferences() {
        _uiState.update {
            it.copy(
                soundEnabled = prefs.getBoolean("pref_sound_enabled", true),
                hapticsEnabled = prefs.getBoolean("pref_haptics_enabled", true)
            )
        }
    }

    fun openRulesBooklet() {
        _uiState.update { it.copy(isRulesBookletOpen = true) }
    }

    fun closeRulesBooklet() {
        _uiState.update { it.copy(isRulesBookletOpen = false) }
    }

    fun openLogoutConfirmation() {
        _uiState.update { it.copy(isLogoutConfirmOpen = true) }
    }

    fun closeLogoutConfirmation() {
        _uiState.update { it.copy(isLogoutConfirmOpen = false) }
    }

    fun logout(onLoggedOut: () -> Unit) {
        prefs.edit {
            remove("active_user_id")
        }
        _uiState.update { it.copy(isLogoutConfirmOpen = false) }
        onLoggedOut()
    }
}

@Suppress("unused")
class MenuViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MenuViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MenuViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}