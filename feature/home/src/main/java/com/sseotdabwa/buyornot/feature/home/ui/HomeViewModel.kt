package com.sseotdabwa.buyornot.feature.home.ui

import androidx.lifecycle.ViewModel
import com.sseotdabwa.buyornot.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * HomeScreen을 위한 ViewModel
 * 현재는 UserType Flow만 제공하지만, 향후 홈 화면의 모든 비즈니스 로직을 관리할 예정
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {
    val userType = userPreferencesRepository.userType
}
