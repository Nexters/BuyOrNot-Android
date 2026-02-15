package com.sseotdabwa.buyornot.feature.mypage.viewmodel

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewModelScope
import com.kakao.sdk.user.UserApiClient
import com.sseotdabwa.buyornot.core.common.util.runCatchingCancellable
import com.sseotdabwa.buyornot.core.ui.BaseViewModel
import com.sseotdabwa.buyornot.domain.repository.AuthRepository
import com.sseotdabwa.buyornot.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@HiltViewModel
class AccountSettingViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
) : BaseViewModel<AccountSettingUiState, AccountSettingIntent, AccountSettingSideEffect>(AccountSettingUiState()) {
    init {
        handleIntent(AccountSettingIntent.FetchProfile)
    }

    override fun handleIntent(intent: AccountSettingIntent) {
        when (intent) {
            is AccountSettingIntent.FetchProfile -> fetchProfile()
            is AccountSettingIntent.Logout -> logout(intent.context)
            is AccountSettingIntent.ShowLogoutDialog -> updateState { it.copy(isLogoutDialogVisible = true) }
            is AccountSettingIntent.DismissLogoutDialog -> updateState { it.copy(isLogoutDialogVisible = false) }
        }
    }

    private fun fetchProfile() {
        viewModelScope.launch {
            updateState { it.copy(isLoading = true) }
            runCatchingCancellable {
                userRepository.getMyProfile()
            }.onSuccess { profile ->
                updateState { it.copy(isLoading = false, userProfile = profile) }
            }.onFailure { throwable ->
                updateState { it.copy(isLoading = false) }
                sendSideEffect(AccountSettingSideEffect.ShowSnackbar(throwable.message ?: "프로필을 불러오지 못했습니다."))
            }
        }
    }

    private fun logout(context: Context) {
        val socialAccount = currentState.userProfile?.socialAccount ?: return
        viewModelScope.launch {
            updateState { it.copy(isLoading = true) }
            runCatchingCancellable {
                // 1. ViewModel에서 소셜 SDK 로그아웃 처리
                if (socialAccount == "KAKAO") {
                    // 실제 구현은 UserApiClient의 콜백을 코루틴으로 변환해야 함
                    suspendCoroutine { continuation ->
                        UserApiClient.instance.logout { error ->
                            if (error != null) {
                                continuation.resumeWithException(error)
                            } else {
                                continuation.resume(Unit)
                            }
                        }
                    }
                } else {
                    val credentialManager = CredentialManager.create(context)
                    credentialManager.clearCredentialState(ClearCredentialStateRequest())
                }
                // 2. Repository를 통해 로컬 토큰 삭제
                authRepository.clearTokens()
            }.onSuccess {
                sendSideEffect(AccountSettingSideEffect.NavigateToLogin)
            }.onFailure {
                updateState { it.copy(isLoading = false) }
                sendSideEffect(AccountSettingSideEffect.ShowSnackbar("로그아웃 실패"))
            }
        }
    }
}
