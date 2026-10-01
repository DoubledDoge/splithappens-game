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

data class SettingsUiState(
    val isDarkMode: Boolean = true,
    val showCardTotals: Boolean = true,
    val showDealerBanner: Boolean = true,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val fastDealEnabled: Boolean = false,
    val autoStandOn21: Boolean = true
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("SplitHappensSettings", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            isDarkMode = prefs.getBoolean(KEY_DARK_MODE, true),
            showCardTotals = prefs.getBoolean(KEY_SHOW_CARD_TOTALS, true),
            showDealerBanner = prefs.getBoolean(KEY_SHOW_DEALER_BANNER, true),
            soundEnabled = prefs.getBoolean(KEY_SOUND_EFFECTS, true),
            hapticsEnabled = prefs.getBoolean(KEY_HAPTICS_ENABLED, true),
            fastDealEnabled = prefs.getBoolean(KEY_FAST_DEAL, false),
            autoStandOn21 = prefs.getBoolean(KEY_AUTO_STAND_21, true)
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun toggleDarkMode(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_DARK_MODE, enabled) }
        _uiState.value = _uiState.value.copy(isDarkMode = enabled)
    }

    fun toggleShowCardTotals(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_SHOW_CARD_TOTALS, enabled) }
        _uiState.value = _uiState.value.copy(showCardTotals = enabled)
    }

    fun toggleShowDealerBanner(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_SHOW_DEALER_BANNER, enabled) }
        _uiState.value = _uiState.value.copy(showDealerBanner = enabled)
    }

    fun toggleSound(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_SOUND_EFFECTS, enabled) }
        _uiState.value = _uiState.value.copy(soundEnabled = enabled)
    }

    fun toggleHaptics(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_HAPTICS_ENABLED, enabled) }
        _uiState.value = _uiState.value.copy(hapticsEnabled = enabled)
    }

    fun toggleFastDeal(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_FAST_DEAL, enabled) }
        _uiState.value = _uiState.value.copy(fastDealEnabled = enabled)
    }

    fun toggleAutoStandOn21(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_AUTO_STAND_21, enabled) }
        _uiState.value = _uiState.value.copy(autoStandOn21 = enabled)
    }

    companion object {
        const val KEY_DARK_MODE = "dark_mode"
        const val KEY_SHOW_CARD_TOTALS = "show_card_totals"
        const val KEY_SHOW_DEALER_BANNER = "show_dealer_banner"
        const val KEY_SOUND_EFFECTS = "sound_effects"
        const val KEY_HAPTICS_ENABLED = "haptics_enabled"
        const val KEY_FAST_DEAL = "fast_deal"
        const val KEY_AUTO_STAND_21 = "auto_stand_21"
    }
}

class SettingsViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}