package com.sseotdabwa.buyornot.feature.auth.viewmodel

import androidx.compose.runtime.Immutable

/**
 * 스플래시 화면의 UI 상태 (MVI State)
 *
 * @property isLoading 초기 로딩 중 여부
 */
@Immutable
data class SplashUiState(
    val isLoading: Boolean = true,
)

/**
 * 스플래시 화면에서 발생하는 사용자 액션 (MVI Intent)
 * 스플래시 화면은 자동으로 진행되므로 사용자 Intent는 없음
 */
sealed interface SplashIntent

/**
 * 스플래시 화면의 일회성 이벤트 (MVI SideEffect)
 */
sealed interface SplashSideEffect {
    /**
     * 로그인 화면으로 이동
     */
    data object NavigateToLogin : SplashSideEffect

    /**
     * 홈 화면으로 이동
     */
    data object NavigateToHome : SplashSideEffect
}
