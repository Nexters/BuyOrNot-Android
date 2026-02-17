package com.sseotdabwa.buyornot.feature.mypage.viewmodel

import android.content.Context
import com.sseotdabwa.buyornot.domain.model.UserProfile

data class WithdrawalUiState(
    val isLoading: Boolean = false,
    val userProfile: UserProfile? = null,
)

sealed interface WithdrawalIntent {
    data object FetchProfile : WithdrawalIntent

    data class Withdraw(
        val context: Context,
    ) : WithdrawalIntent
}

sealed interface WithdrawalSideEffect {
    data class ShowSnackbar(
        val message: String,
    ) : WithdrawalSideEffect

    data object NavigateToLogin : WithdrawalSideEffect
}
