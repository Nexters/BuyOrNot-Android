package com.sseotdabwa.buyornot.feature.auth.ui

import androidx.compose.runtime.Immutable

sealed interface UpdateDialogType {
    data object None : UpdateDialogType
    data object Soft : UpdateDialogType
    data object Force : UpdateDialogType
}

/**
 * 스플래시 화면의 UI 상태 (MVI State)
 *
 * @property isLoading 초기 로딩 중 여부
 * @property updateDialogType 표시할 업데이트 다이얼로그 타입
 */
@Immutable
data class SplashUiState(
    val isLoading: Boolean = true,
    val updateDialogType: UpdateDialogType = UpdateDialogType.None,
)

/**
 * 스플래시 화면에서 발생하는 사용자 액션 (MVI Intent)
 */
sealed interface SplashIntent {
    data object DismissSoftUpdate : SplashIntent
}

/**
 * 스플래시 화면의 일회성 이벤트 (MVI SideEffect)
 */
sealed interface SplashSideEffect {
    data object NavigateToLogin : SplashSideEffect

    data object NavigateToHome : SplashSideEffect
}
