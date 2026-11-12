package io.github.doubleddoge.splithappens.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import io.github.doubleddoge.splithappens.data.pref.PrefFiles
import io.github.doubleddoge.splithappens.data.pref.PrefKeys
import io.github.doubleddoge.splithappens.util.triggerDeviceVibration
import io.github.doubleddoge.splithappens.viewmodel.uistate.SettingsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

private const val TOGGLE_HAPTIC_MILLIS = 30L

class SettingViewModel(private val application: Application) : ViewModel() {

	private val prefs: SharedPreferences =
		application.getSharedPreferences(PrefFiles.APP, Context.MODE_PRIVATE)

	private val preferenceListener =
		SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> loadPreferences() }

	private val _uiState = MutableStateFlow(readPreferences())
	val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

	init {
		prefs.registerOnSharedPreferenceChangeListener(preferenceListener)
	}

	fun loadPreferences() {
		_uiState.value = readPreferences()
	}

	fun toggleDarkTheme(enabled: Boolean) =
		storeAndBuzz(PrefKeys.DARK_THEME, enabled) { it.copy(darkTheme = enabled) }

	fun toggleShowCardTotal(enabled: Boolean) =
		storeAndBuzz(PrefKeys.SHOW_CARD_TOTAL, enabled) { it.copy(showCardTotal = enabled) }

	fun toggleDealerRuleBanner(enabled: Boolean) =
		storeAndBuzz(PrefKeys.DEALER_RULE_BANNER, enabled) { it.copy(dealerRuleBanner = enabled) }

	fun toggleSoundEffects(enabled: Boolean) =
		storeAndBuzz(PrefKeys.SOUND_EFFECTS, enabled) { it.copy(soundEffects = enabled) }

	fun toggleFastDeal(enabled: Boolean) =
		storeAndBuzz(PrefKeys.FAST_DEAL, enabled) { it.copy(fastDeal = enabled) }

	fun toggleAutoStandOn21(enabled: Boolean) =
		storeAndBuzz(PrefKeys.AUTO_STAND_ON_21, enabled) { it.copy(autoStandOn21 = enabled) }

	fun toggleHapticVibration(enabled: Boolean) {
		store(PrefKeys.HAPTIC_VIBRATION, enabled) { it.copy(hapticVibration = enabled) }
		if (enabled) triggerHaptic(force = true) // confirms the setting
	}

	private fun readPreferences(): SettingsUiState {
		val defaults = SettingsUiState()
		return SettingsUiState(
			darkTheme = prefs.getBoolean(PrefKeys.DARK_THEME, defaults.darkTheme),
			showCardTotal = prefs.getBoolean(PrefKeys.SHOW_CARD_TOTAL, defaults.showCardTotal),
			dealerRuleBanner = prefs.getBoolean(PrefKeys.DEALER_RULE_BANNER, defaults.dealerRuleBanner),
			soundEffects = prefs.getBoolean(PrefKeys.SOUND_EFFECTS, defaults.soundEffects),
			hapticVibration = prefs.getBoolean(PrefKeys.HAPTIC_VIBRATION, defaults.hapticVibration),
			fastDeal = prefs.getBoolean(PrefKeys.FAST_DEAL, defaults.fastDeal),
			autoStandOn21 = prefs.getBoolean(PrefKeys.AUTO_STAND_ON_21, defaults.autoStandOn21),
		)
	}

	private fun store(key: String, enabled: Boolean, transform: (SettingsUiState) -> SettingsUiState) {
		prefs.edit { putBoolean(key, enabled) }
		_uiState.update(transform)
	}

	private fun storeAndBuzz(key: String, enabled: Boolean, transform: (SettingsUiState) -> SettingsUiState) {
		store(key, enabled, transform)
		triggerHaptic()
	}

	private fun triggerHaptic(force: Boolean = false) {
		if (!force && !_uiState.value.hapticVibration) return
		triggerDeviceVibration(application, TOGGLE_HAPTIC_MILLIS)
	}

	override fun onCleared() {
		prefs.unregisterOnSharedPreferenceChangeListener(preferenceListener)
	}
}
