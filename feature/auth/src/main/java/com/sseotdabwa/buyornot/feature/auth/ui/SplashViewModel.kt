package com.sseotdabwa.buyornot.feature.auth.ui

import androidx.lifecycle.ViewModel
import com.sseotdabwa.buyornot.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * 스플래시 화면을 위한 ViewModel
 * 저장된 토큰 확인하여 자동 로그인 여부를 판단
 */
@HiltViewModel
class SplashViewModel @Inject constructor(
    userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {
    /**
     * 저장된 토큰이 있는지 확인
     * accessToken이 비어있지 않으면 true
     */
    val hasValidToken =
        userPreferencesRepository.userType.map { userType ->
            userType != com.sseotdabwa.buyornot.domain.model.UserType.GUEST
        }
}
