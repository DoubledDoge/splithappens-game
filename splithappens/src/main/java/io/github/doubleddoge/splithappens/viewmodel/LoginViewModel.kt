package io.github.doubleddoge.splithappens.viewmodel

import android.app.Application
import android.content.Context
import android.text.format.DateUtils
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.doubleddoge.splithappens.SplitHappensApp
import io.github.doubleddoge.splithappens.data.dao.UserDao
import io.github.doubleddoge.splithappens.data.entity.UserEntity
import io.github.doubleddoge.splithappens.data.pref.PrefFiles
import io.github.doubleddoge.splithappens.data.pref.PrefKeys
import io.github.doubleddoge.splithappens.util.Economy
import io.github.doubleddoge.splithappens.util.PlayerName
import io.github.doubleddoge.splithappens.viewmodel.uistate.LoginUiState
import io.github.doubleddoge.splithappens.viewmodel.uistate.UserProfileItem
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
	private val userDao: UserDao,
	private val application: Application
) : ViewModel() {

	private val prefs = application.getSharedPreferences(PrefFiles.AUTH, Context.MODE_PRIVATE)
	private val appPrefs = application.getSharedPreferences(PrefFiles.APP, Context.MODE_PRIVATE)

	private val _uiState =
		MutableStateFlow(
			LoginUiState(
				username = prefs.getString(PrefKeys.SAVED_USERNAME, "") ?: "",
				rememberMe = prefs.getBoolean(PrefKeys.REMEMBER_ME, true),
			)
		)
	val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

	init {
		loadExistingProfiles()
	}

	private fun loadExistingProfiles() {
		viewModelScope.launch {
			userDao.observeAll().collect { users ->
				val items =
					users
						.map { user ->
							val lastUsed = prefs.getLong(PrefKeys.lastUsed(user.userId), user.createdAt)

							UserProfileItem(
								user = user,
								lastUsedFormatted = formatTimeAgo(lastUsed),
								profilePictureBytes = userDao.getPicture(user.userId),
							)
						}
						.sortedByDescending { (user, _, _) ->
							prefs.getLong(PrefKeys.lastUsed(user.userId), user.createdAt)
						}
				_uiState.value = _uiState.value.copy(existingProfiles = items)
			}
		}
	}

	private fun formatTimeAgo(timeMs: Long): String {
		val now = System.currentTimeMillis()
		if ((now - timeMs) < DateUtils.MINUTE_IN_MILLIS) {
			return "Just now"
		}
		return DateUtils.getRelativeTimeSpanString(
			timeMs,
			now,
			DateUtils.MINUTE_IN_MILLIS,
		)
			.toString()
	}

	fun onUsernameChange(name: String) {
		val filtered = PlayerName.sanitize(name)
		val tooLong = filtered.length > PlayerName.MAX_LENGTH
		_uiState.update {
			it.copy(
				username = filtered.take(PlayerName.MAX_LENGTH),
				errorMessage = if (tooLong) PlayerName.TOO_LONG_MESSAGE else null,
			)
		}
	}

	fun onRememberMeChange(checked: Boolean) {
		_uiState.value = _uiState.value.copy(rememberMe = checked)
	}

	fun openSwitchProfileDialog() {
		viewModelScope.launch {
			val refreshed =
				_uiState.value.existingProfiles.map { item ->
					item.copy(profilePictureBytes = userDao.getPicture(item.user.userId))
				}
			_uiState.value =
				_uiState.value.copy(
					existingProfiles = refreshed,
					isSwitchProfileDialogOpen = true,
				)
		}
	}

	fun closeSwitchProfileDialog() {
		_uiState.value = _uiState.value.copy(isSwitchProfileDialogOpen = false)
	}

	fun selectProfile(user: UserEntity) {
		_uiState.value =
			_uiState.value.copy(
				username = user.displayName,
				errorMessage = null,
				isSwitchProfileDialogOpen = false,
			)
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
			runCatching {
				userDao.delete(userToDelete)

				val savedUserId = prefs.getString(PrefKeys.SAVED_USER_ID, null)
				if (savedUserId == userToDelete.userId) {
					prefs.edit {
						remove(PrefKeys.SAVED_USERNAME)
						remove(PrefKeys.SAVED_USER_ID)
					}
					appPrefs.edit {
						remove(PrefKeys.ACTIVE_USER_ID)
					}
				}

				if (_uiState.value.username == userToDelete.displayName) {
					_uiState.value = _uiState.value.copy(username = "")
				}

				_uiState.value = _uiState.value.copy(userPendingDelete = null)
			}.onFailure { e ->
				if (e is CancellationException) throw e
				_uiState.value =
					_uiState.value.copy(
						userPendingDelete = null,
						errorMessage = e.localizedMessage ?: "Failed to delete profile",
					)
			}
		}
	}

	suspend fun tryAutoLogin(): Boolean {
		val shouldRemember = prefs.getBoolean(PrefKeys.REMEMBER_ME, false)
		val savedUserId = prefs.getString(PrefKeys.SAVED_USER_ID, null)

		if (shouldRemember && !savedUserId.isNullOrBlank()) {
			val existing = userDao.getById(savedUserId)
			if (existing != null) {
				prefs.edit {
					putLong(PrefKeys.lastUsed(existing.userId), System.currentTimeMillis())
				}
				(application as? SplitHappensApp)?.checkAndClaimDailyReward(existing.userId)
				return true
			}
		}
		return false
	}

	fun login(onSuccess: (UserEntity) -> Unit) {
		val inputName = _uiState.value.username.trim()

		if (inputName.isBlank()) { showError(PlayerName.BLANK_MESSAGE); return }
		if (!PlayerName.isValid(inputName)) { showError(PlayerName.INVALID_MESSAGE); return }

		_uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

		viewModelScope.launch {
			runCatching {
				val allUsers = userDao.observeAll().first()
				val existingUser = allUsers.firstOrNull {
					it.displayName.equals(inputName, ignoreCase = true)
				}

				val user = existingUser
					?: UserEntity(displayName = inputName, chipsOwned = Economy.STARTING_CHIPS)
						.also { userDao.insert(it) }

				// Always write the current active user ID so HomeViewModel and GameScreen bind to them
				prefs.edit {
					putBoolean(PrefKeys.REMEMBER_ME, _uiState.value.rememberMe)
					putString(PrefKeys.SAVED_USERNAME, user.displayName)
					putString(PrefKeys.SAVED_USER_ID, user.userId)
					putLong(PrefKeys.lastUsed(user.userId), System.currentTimeMillis())
				}

				appPrefs.edit {
					putString(PrefKeys.ACTIVE_USER_ID, user.userId)
				}

				(application as? SplitHappensApp)?.checkAndClaimDailyReward(user.userId)

				_uiState.value = _uiState.value.copy(isLoading = false)
				onSuccess(user)
			}.onFailure { e ->
				if (e is CancellationException) throw e
				_uiState.value =
					_uiState.value.copy(
						isLoading = false,
						errorMessage = e.localizedMessage ?: "Failed to log in",
					)
			}
		}
	}

	private fun showError(message: String?) = _uiState.update { it.copy(errorMessage = message) }
}


