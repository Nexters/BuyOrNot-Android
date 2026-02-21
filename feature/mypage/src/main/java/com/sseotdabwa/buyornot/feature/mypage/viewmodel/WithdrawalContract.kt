package com.sseotdabwa.buyornot.feature.mypage.viewmodel

import android.content.Context
import com.sseotdabwa.buyornot.core.designsystem.components.SnackBarIconTint
import com.sseotdabwa.buyornot.core.designsystem.icon.IconResource
import com.sseotdabwa.buyornot.domain.model.UserProfile

data class WithdrawalUiState(
    val isLoading: Boolean = false,
    val userProfile: UserProfile? = null,
    val isWithdrawalDialogVisible: Boolean = false,
)

sealed interface WithdrawalIntent {
    data object FetchProfile : WithdrawalIntent

    data class Withdraw(
        val context: Context,
    ) : WithdrawalIntent

    data object ShowWithdrawalDialog : WithdrawalIntent

    data object DismissWithdrawalDialog : WithdrawalIntent
}

sealed interface WithdrawalSideEffect {
    data class ShowSnackbar(
        val message: String,
        val icon: IconResource? = null,
        val iconTint: SnackBarIconTint = SnackBarIconTint.Success,
    ) : WithdrawalSideEffect

    data object NavigateToLogin : WithdrawalSideEffect
}
