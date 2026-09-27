package com.sseotdabwa.buyornot.feature.mypage.viewmodel

import androidx.lifecycle.viewModelScope
import com.sseotdabwa.buyornot.core.common.util.runCatchingCancellable
import com.sseotdabwa.buyornot.core.ui.base.BaseViewModel
import com.sseotdabwa.buyornot.domain.repository.UserPreferencesRepository
import com.sseotdabwa.buyornot.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : BaseViewModel<MyPageUiState, MyPageIntent, MyPageSideEffect>(MyPageUiState()) {
    init {
        handleIntent(MyPageIntent.LoadProfile)
    }

    override fun handleIntent(intent: MyPageIntent) {
        when (intent) {
            is MyPageIntent.LoadProfile -> loadProfile()
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            updateState { it.copy(isLoading = true) }
            runCatchingCancellable {
                userRepository.getMyProfile()
            }.onSuccess { profile ->
                updateState { it.copy(isLoading = false, userProfile = profile) }
                runCatchingCancellable {
                    profile.nickname?.let { userPreferencesRepository.updateDisplayName(it) }
                    userPreferencesRepository.updateProfileImageUrl(profile.profileImage)
                }.onFailure {
                    Timber.w("Failed to update user preferences")
                }
            }.onFailure { throwable ->
                updateState { it.copy(isLoading = false) }
                sendSideEffect(MyPageSideEffect.ShowSnackbar("프로필을 불러오지 못했습니다."))
                Timber.w(throwable)
            }
        }
    }
}
