package com.sseotdabwa.buyornot.feature.mypage.viewmodel

import android.content.Context
import com.sseotdabwa.buyornot.core.designsystem.components.SnackBarIconTint
import com.sseotdabwa.buyornot.core.designsystem.icon.IconResource
import com.sseotdabwa.buyornot.domain.model.UserProfile

data class AccountSettingUiState(
    val isLoading: Boolean = false,
    val userProfile: UserProfile? = null,
    val isLogoutDialogVisible: Boolean = false,
)

sealed interface AccountSettingIntent {
    data object FetchProfile : AccountSettingIntent

    data class Logout(
        val context: Context,
    ) : AccountSettingIntent

    data object ShowLogoutDialog : AccountSettingIntent

    data object DismissLogoutDialog : AccountSettingIntent
}

sealed interface AccountSettingSideEffect {
    data class ShowSnackbar(
        val message: String,
        val icon: IconResource? = null,
        val iconTint: SnackBarIconTint = SnackBarIconTint.Success,
    ) : AccountSettingSideEffect

    data object NavigateToLogin : AccountSettingSideEffect
}
