package io.github.doubleddoge.splithappens.viewmodel.uistate

data class SettingsUiState(
	val darkTheme: Boolean = true,
	val showCardTotal: Boolean = true,
	val dealerRuleBanner: Boolean = true,
	val soundEffects: Boolean = true,
	val hapticVibration: Boolean = true,
	val fastDeal: Boolean = false,
	val autoStandOn21: Boolean = true,
)