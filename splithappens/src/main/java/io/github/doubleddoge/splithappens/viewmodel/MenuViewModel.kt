package io.github.doubleddoge.splithappens.viewmodel

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import io.github.doubleddoge.splithappens.data.pref.PrefFiles
import io.github.doubleddoge.splithappens.data.pref.PrefKeys
import io.github.doubleddoge.splithappens.viewmodel.uistate.MenuUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MenuViewModel(application: Application) : ViewModel() {
	private val prefs = application.getSharedPreferences(PrefFiles.APP, Context.MODE_PRIVATE)
	private val authPrefs = application.getSharedPreferences(PrefFiles.AUTH, Context.MODE_PRIVATE)

	private val _uiState = MutableStateFlow(MenuUiState())
	val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

	fun openRulesBooklet() = _uiState.update { it.copy(isRulesBookletOpen = true) }
	fun closeRulesBooklet() = _uiState.update { it.copy(isRulesBookletOpen = false) }
	fun openLogoutConfirmation() = _uiState.update { it.copy(isLogoutConfirmOpen = true) }
	fun closeLogoutConfirmation() = _uiState.update { it.copy(isLogoutConfirmOpen = false) }

	fun logout(onLoggedOut: () -> Unit) {
		authPrefs.edit {
			remove(PrefKeys.SAVED_USER_ID)
			remove(PrefKeys.SAVED_USERNAME)
			putBoolean(PrefKeys.REMEMBER_ME, false)
		}
		prefs.edit { remove(PrefKeys.ACTIVE_USER_ID) }
		_uiState.update { it.copy(isLogoutConfirmOpen = false) }
		onLoggedOut()
	}
}
