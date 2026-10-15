package io.github.doubleddoge.splithappens

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SettingUiState(
    val darkTheme: Boolean = false,
    val showCardTotal: Boolean = true,
    val dealerRuleBanner: Boolean = true,
    val soundEffects: Boolean = true,
    val hapticVibration: Boolean = true,
    val fastDeal: Boolean = false,
    val autoStandOn21: Boolean = true
)

@Suppress("unused")
class SettingViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs: SharedPreferences =
        application.getSharedPreferences("split_happens_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(
        SettingUiState(
            darkTheme = prefs.getBoolean("pref_dark_theme", false),
            showCardTotal = prefs.getBoolean("pref_show_card_total", true),
            dealerRuleBanner = prefs.getBoolean("pref_dealer_rule_banner", true),
            soundEffects = prefs.getBoolean("pref_sound_effects", true),
            hapticVibration = prefs.getBoolean("pref_haptic_vibration", true),
            fastDeal = prefs.getBoolean("pref_fast_deal", false),
            autoStandOn21 = prefs.getBoolean("pref_auto_stand_21", true)
        )
    )
    val uiState: StateFlow<SettingUiState> = _uiState.asStateFlow()

    fun toggleDarkTheme(enabled: Boolean) {
        prefs.edit { putBoolean("pref_dark_theme", enabled) }
        _uiState.update { it.copy(darkTheme = enabled) }
        triggerHaptic()
    }

    fun toggleShowCardTotal(enabled: Boolean) {
        prefs.edit { putBoolean("pref_show_card_total", enabled) }
        _uiState.update { it.copy(showCardTotal = enabled) }
        triggerHaptic()
    }

    fun toggleDealerRuleBanner(enabled: Boolean) {
        prefs.edit { putBoolean("pref_dealer_rule_banner", enabled) }
        _uiState.update { it.copy(dealerRuleBanner = enabled) }
        triggerHaptic()
    }

    fun toggleSoundEffects(enabled: Boolean) {
        prefs.edit { putBoolean("pref_sound_effects", enabled) }
        _uiState.update { it.copy(soundEffects = enabled) }
        triggerHaptic()
    }

    fun toggleHapticVibration(enabled: Boolean) {
        prefs.edit { putBoolean("pref_haptic_vibration", enabled) }
        _uiState.update { it.copy(hapticVibration = enabled) }
        if (enabled) triggerHaptic(force = true)
    }

    fun toggleFastDeal(enabled: Boolean) {
        prefs.edit { putBoolean("pref_fast_deal", enabled) }
        _uiState.update { it.copy(fastDeal = enabled) }
        triggerHaptic()
    }

    fun toggleAutoStandOn21(enabled: Boolean) {
        prefs.edit { putBoolean("pref_auto_stand_21", enabled) }
        _uiState.update { it.copy(autoStandOn21 = enabled) }
        triggerHaptic()
    }

    private fun triggerHaptic(force: Boolean = false) {
        if (!force && !_uiState.value.hapticVibration) return
        triggerDeviceVibration(getApplication(), 30)
    }
}

@Suppress("unused")
class SettingViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}