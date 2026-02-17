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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@HiltViewModel
class WithdrawalViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
) : BaseViewModel<WithdrawalUiState, WithdrawalIntent, WithdrawalSideEffect>(WithdrawalUiState()) {
    init {
        handleIntent(WithdrawalIntent.FetchProfile)
    }

    override fun handleIntent(intent: WithdrawalIntent) {
        when (intent) {
            is WithdrawalIntent.FetchProfile -> fetchProfile()
            is WithdrawalIntent.Withdraw -> withdraw(intent.context)
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
                sendSideEffect(WithdrawalSideEffect.ShowSnackbar(throwable.message ?: "프로필을 불러오지 못했습니다."))
            }
        }
    }

    private fun withdraw(context: Context) {
        val socialAccount =
            currentState.userProfile?.socialAccount ?: run {
                sendSideEffect(WithdrawalSideEffect.ShowSnackbar("사용자 정보를 가져올 수 없습니다."))
                return
            }

        viewModelScope.launch {
            updateState { it.copy(isLoading = true) }

            withContext(Dispatchers.IO) {
                // 1. 서버에 회원 탈퇴 요청
                runCatchingCancellable {
                    userRepository.deleteMyAccount()
                }

                // 2. ViewModel에서 소셜 SDK 연결 해제
                runCatchingCancellable {
                    if (socialAccount == "KAKAO") {
                        suspendCancellableCoroutine { continuation ->
                            UserApiClient.instance.unlink { error ->
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

            // 3. 성공/실패 여부와 관계없이 항상 로컬 토큰을 삭제하고 로그인 화면으로 이동
            withContext(NonCancellable) {
                runCatchingCancellable {
                    authRepository.clearTokens()
                }
            }

            sendSideEffect(WithdrawalSideEffect.NavigateToLogin)
        }
    }
}
