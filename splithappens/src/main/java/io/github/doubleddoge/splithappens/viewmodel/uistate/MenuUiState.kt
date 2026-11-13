package io.github.doubleddoge.splithappens.viewmodel.uistate

data class MenuUiState(
	val isRulesBookletOpen: Boolean = false,
	val isLogoutConfirmOpen: Boolean = false,
	val soundEnabled: Boolean = true,
	val hapticsEnabled: Boolean = true,
)