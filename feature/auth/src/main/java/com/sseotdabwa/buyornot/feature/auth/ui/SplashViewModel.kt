package com.sseotdabwa.buyornot.feature.auth.ui

import androidx.lifecycle.viewModelScope
import com.sseotdabwa.buyornot.core.ui.base.BaseViewModel
import com.sseotdabwa.buyornot.domain.model.UserType
import com.sseotdabwa.buyornot.domain.repository.UserPreferencesRepository
import com.sseotdabwa.buyornot.feature.auth.viewmodel.SplashIntent
import com.sseotdabwa.buyornot.feature.auth.viewmodel.SplashSideEffect
import com.sseotdabwa.buyornot.feature.auth.viewmodel.SplashUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

private const val SPLASH_TIMEOUT_MILLIS = 2300L

/**
 * 스플래시 화면을 위한 ViewModel
 *
 * 토큰 존재 여부를 확인하고, 2.3초 후 자동으로 네비게이션 SideEffect를 방출합니다.
 */
@HiltViewModel
class SplashViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
) : BaseViewModel<SplashUiState, SplashIntent, SplashSideEffect>(SplashUiState()) {
    init {
        checkTokenAndNavigate()
    }

    override fun handleIntent(intent: SplashIntent) {
        // 스플래시 화면은 사용자 액션이 없으므로 비어있음
    }

    /**
     * 토큰 존재 여부를 확인하고 적절한 화면으로 이동
     */
    private fun checkTokenAndNavigate() {
        viewModelScope.launch {
            val hasValidToken =
                try {
                    val userType = userPreferencesRepository.userType.first()
                    userType != UserType.GUEST
                } catch (e: CancellationException) {
                   throw e
                } catch (e: Exception) {
                    false // DataStore 오류 시 비로그인 화면으로 폴백
                }   //

            delay(SPLASH_TIMEOUT_MILLIS)

            // 토큰 유효성에 따라 SideEffect 방출
            if (hasValidToken) {
                sendSideEffect(SplashSideEffect.NavigateToHome)
            } else {
                sendSideEffect(SplashSideEffect.NavigateToLogin)
            }

            updateState { it.copy(isLoading = false) }
        }
    }
}
