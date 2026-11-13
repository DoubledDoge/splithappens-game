package io.github.doubleddoge.splithappens.viewmodel.uistate

import io.github.doubleddoge.splithappens.data.entity.UserEntity

@Suppress("ArrayInDataClass")
data class UserProfileItem(
	val user: UserEntity,
	val lastUsedFormatted: String = "Recently",
	val profilePictureBytes: ByteArray? = null,
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