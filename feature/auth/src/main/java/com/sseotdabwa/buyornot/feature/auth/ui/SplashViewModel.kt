package com.sseotdabwa.buyornot.feature.auth.ui

import androidx.lifecycle.ViewModel
import com.sseotdabwa.buyornot.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import com.sseotdabwa.buyornot.domain.model.UserType

/**
 * 스플래시 화면을 위한 ViewModel
 * 저장된 토큰 확인하여 자동 로그인 여부를 판단
 */
@HiltViewModel
class SplashViewModel @Inject constructor(
    userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {
    /**
     * 유효한 사용자 세션이 있는지 확인
     * 사용자 타입이 GUEST가 아니면 true
     */
    val hasValidToken =
        userPreferencesRepository.userType.map { userType ->
            userType != UserType.GUEST
        }
}
