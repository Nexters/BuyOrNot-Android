package com.sseotdabwa.buyornot.feature.mypage.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.sseotdabwa.buyornot.core.common.util.runCatchingCancellable
import com.sseotdabwa.buyornot.core.ui.base.BaseViewModel
import com.sseotdabwa.buyornot.domain.repository.UserPreferencesRepository
import com.sseotdabwa.buyornot.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "MyPageViewModel"

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
                    userPreferencesRepository.updateDisplayName(profile.nickname)
                    userPreferencesRepository.updateProfileImageUrl(profile.profileImage)
                }.onFailure {
                    Log.w(TAG, "Failed to update user preferences")
                }
            }.onFailure { throwable ->
                updateState { it.copy(isLoading = false) }
                sendSideEffect(MyPageSideEffect.ShowSnackbar("프로필을 불러오지 못했습니다."))
                Log.w(TAG, throwable.toString())
            }
        }
    }
}
