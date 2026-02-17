package com.sseotdabwa.buyornot.feature.mypage.viewmodel

import com.sseotdabwa.buyornot.domain.model.UserProfile

data class MyPageUiState(
    val isLoading: Boolean = false,
    val userProfile: UserProfile? = null,
)

sealed interface MyPageIntent {
    data object LoadProfile : MyPageIntent
}

sealed interface MyPageSideEffect {
    data class ShowSnackbar(
        val message: String,
    ) : MyPageSideEffect
}
