package com.sseotdabwa.buyornot.feature.mypage.viewmodel

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewModelScope
import com.kakao.sdk.user.UserApiClient
import com.sseotdabwa.buyornot.core.common.util.runCatchingCancellable
import com.sseotdabwa.buyornot.core.ui.base.BaseViewModel
import com.sseotdabwa.buyornot.domain.repository.AuthRepository
import com.sseotdabwa.buyornot.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

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
        val socialAccount =
            currentState.userProfile?.socialAccount ?: run {
                sendSideEffect(AccountSettingSideEffect.ShowSnackbar("사용자 정보를 가져올 수 없습니다."))
                return
            }

        viewModelScope.launch {
            updateState { it.copy(isLoading = true) }

            // 1. 서버 & 소셜 로그아웃 (실패해도 괜찮음)
            withContext(Dispatchers.IO) {
                // 서버 로그아웃
                runCatchingCancellable {
                    authRepository.logout()
                }

                // 소셜 로그아웃
                runCatchingCancellable {
                    if (socialAccount == "KAKAO") {
                        suspendCancellableCoroutine { continuation ->
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
                }
            }

            // 2. (가장 중요) 어떤 경우든 로컬 토큰은 반드시 삭제
            withContext(NonCancellable) {
                runCatchingCancellable {
                    authRepository.clearUserInfo()
                }
            }

            // 3. 로그인 화면으로 이동
            updateState { it.copy(isLogoutDialogVisible = false) }
            sendSideEffect(AccountSettingSideEffect.NavigateToLogin)
        }
    }
}
