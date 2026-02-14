package com.sseotdabwa.buyornot.feature.auth.ui

import android.content.Context

data class LoginUiState(
    val isLoading: Boolean = false,
)

sealed interface LoginIntent {
    data class GoogleLogin(
        val context: Context,
    ) : LoginIntent

    data class KakaoLogin(
        val context: Context,
    ) : LoginIntent
}

sealed interface LoginSideEffect {
    data object NavigateToHome : LoginSideEffect

    data class ShowSnackbar(
        val message: String,
    ) : LoginSideEffect
}
