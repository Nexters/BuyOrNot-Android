package com.sseotdabwa.buyornot.feature.mypage.viewmodel

import com.sseotdabwa.buyornot.core.designsystem.components.SnackBarIconTint
import com.sseotdabwa.buyornot.core.designsystem.icon.IconResource
import com.sseotdabwa.buyornot.domain.model.UserProfile

data class MyPageUiState(
    val isLoading: Boolean = false,
    val userProfile: UserProfile? = null,
)

sealed interface MyPageIntent {
    data object LoadProfile : MyPageIntent

    /** 프로필 수정 후 돌아왔을 때 로딩 화면 없이 프로필만 다시 불러온다. */
    data object RefreshProfile : MyPageIntent
}

sealed interface MyPageSideEffect {
    data class ShowSnackbar(
        val message: String,
        val icon: IconResource? = null,
        val iconTint: SnackBarIconTint = SnackBarIconTint.Success,
    ) : MyPageSideEffect
}
