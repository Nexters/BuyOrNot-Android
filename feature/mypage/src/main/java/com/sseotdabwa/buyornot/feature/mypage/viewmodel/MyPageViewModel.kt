package com.sseotdabwa.buyornot.feature.mypage.viewmodel

import androidx.lifecycle.viewModelScope
import com.sseotdabwa.buyornot.core.common.util.runCatchingCancellable
import com.sseotdabwa.buyornot.core.ui.BaseViewModel
import com.sseotdabwa.buyornot.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val userRepository: UserRepository,
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
            }.onFailure { throwable ->
                updateState { it.copy(isLoading = false) }
                sendSideEffect(MyPageSideEffect.ShowSnackbar(throwable.message ?: "프로필을 불러오지 못했습니다."))
            }
        }
    }
}
