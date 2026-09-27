package com.sseotdabwa.buyornot.feature.auth.ui

import androidx.compose.runtime.Immutable
import com.sseotdabwa.buyornot.core.designsystem.components.SnackBarIconTint
import com.sseotdabwa.buyornot.core.designsystem.icon.IconResource

/**
 * 닉네임 설정 화면의 UI 상태
 *
 * @property nickname 입력 중인 닉네임 (최대 10자)
 * @property errorMessage 서버가 거절한 사유. 입력이 바뀌면 지운다.
 * @property isLoading 닉네임 설정 요청 중 여부
 */
@Immutable
data class NicknameSetupUiState(
    val nickname: String = "",
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
) {
    // 형식 검사는 서버가 하므로 1자라도 입력하면 누를 수 있다.
    val isSubmitEnabled: Boolean
        get() = nickname.isNotEmpty() && !isLoading
}

sealed interface NicknameSetupIntent {
    data class UpdateNickname(
        val nickname: String,
    ) : NicknameSetupIntent

    data object Submit : NicknameSetupIntent
}

sealed interface NicknameSetupSideEffect {
    data object NavigateToHome : NicknameSetupSideEffect

    data class ShowSnackbar(
        val message: String,
        val icon: IconResource? = null,
        val iconTint: SnackBarIconTint = SnackBarIconTint.Success,
    ) : NicknameSetupSideEffect
}
