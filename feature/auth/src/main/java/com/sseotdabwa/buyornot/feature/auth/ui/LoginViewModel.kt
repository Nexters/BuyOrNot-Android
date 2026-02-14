package com.sseotdabwa.buyornot.feature.auth.ui

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import com.sseotdabwa.buyornot.core.ui.BaseViewModel
import com.sseotdabwa.buyornot.domain.repository.AuthRepository
import com.sseotdabwa.buyornot.feature.auth.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.security.SecureRandom
import java.util.Base64
import javax.inject.Inject

private const val TAG = "LoginViewModel"

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : BaseViewModel<LoginUiState, LoginIntent, LoginSideEffect>(LoginUiState()) {
    override fun handleIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.GoogleLogin -> googleLogin(intent.context)
            is LoginIntent.KakaoLogin -> kakaoLogin(intent.context)
        }
    }

    private fun googleLogin(context: Context) {
        viewModelScope.launch {
            updateState { it.copy(isLoading = true) }
            try {
                // 1. GetGoogleIdOption 설정
                val googleIdOption =
                    GetGoogleIdOption
                        .Builder()
                        .setFilterByAuthorizedAccounts(false)
                        .setServerClientId(context.getString(R.string.web_client_id))
                        .setNonce(generateSecureRandomNonce())
                        .build()

                val credentialManager = CredentialManager.create(context)
                val request =
                    GetCredentialRequest
                        .Builder()
                        .addCredentialOption(googleIdOption)
                        .build()

                val result = credentialManager.getCredential(context, request)
                val credential = result.credential

                if (credential is GoogleIdTokenCredential) {
                    processGoogleLogin(credential.id)
                } else {
                    handleGoogleLoginError("지원하지 않는 자격 증명 유형입니다.")
                }
            } catch (e: androidx.credentials.exceptions.GetCredentialCancellationException) {
                // 사용자가 선택창에서 뒤로가기를 누른 경우 - 자연스러운 종료이므로 에러 메시지 생략
                updateState { it.copy(isLoading = false) }
            } catch (e: androidx.credentials.exceptions.NoCredentialException) {
                // 3. 기기에 구글 계정이 없거나 테스트 사용자가 아닐 때 발생
                Log.e(TAG, "사용 가능한 계정이 없음", e)
                sendSideEffect(LoginSideEffect.ShowSnackbar("로그인 가능한 구글 계정을 찾을 수 없습니다. 테스트 사용자 등록 여부를 확인해주세요."))
                updateState { it.copy(isLoading = false) }
            } catch (e: Exception) {
                Log.e(TAG, "Google 로그인 실패", e)
                handleGoogleLoginError(e.message ?: "알 수 없는 오류가 발생했습니다.")
            }
        }
    }

    // 에러 처리를 위한 공통 함수
    private fun handleGoogleLoginError(message: String) {
        sendSideEffect(LoginSideEffect.ShowSnackbar("Google 로그인 실패: $message"))
        updateState { it.copy(isLoading = false) }
    }

    private fun processGoogleLogin(idToken: String) {
        viewModelScope.launch {
            authRepository
                .googleLogin(idToken)
                .onSuccess {
                    sendSideEffect(LoginSideEffect.NavigateToHome)
                }.onFailure {
                    sendSideEffect(LoginSideEffect.ShowSnackbar(it.message ?: "구글 로그인에 실패했습니다."))
                }
            updateState { it.copy(isLoading = false) }
        }
    }

    private fun kakaoLogin(context: Context) {
        viewModelScope.launch {
            updateState { it.copy(isLoading = true) }

            val kakaoLoginCallback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
                if (error != null) {
                    Log.e(TAG, "카카오계정으로 로그인 실패", error)
                    sendSideEffect(LoginSideEffect.ShowSnackbar("카카오 로그인 실패: ${error.message}"))
                    updateState { it.copy(isLoading = false) }
                } else if (token != null) {
                    Log.i(TAG, "카카오계정으로 로그인 성공 ${token.accessToken}")
                    processKakaoLogin(token.accessToken)
                } else {
                    updateState { it.copy(isLoading = false) }
                }
            }

            if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
                UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
                    if (error != null) {
                        Log.e(TAG, "카카오톡으로 로그인 실패", error)
                        if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                            updateState { it.copy(isLoading = false) }
                            return@loginWithKakaoTalk
                        }
                        UserApiClient.instance.loginWithKakaoAccount(
                            context,
                            callback = kakaoLoginCallback,
                        )
                    } else if (token != null) {
                        Log.i(TAG, "카카오톡으로 로그인 성공 ${token.accessToken}")
                        processKakaoLogin(token.accessToken)
                    } else {
                        updateState { it.copy(isLoading = false) }
                    }
                }
            } else {
                UserApiClient.instance.loginWithKakaoAccount(context, callback = kakaoLoginCallback)
            }
        }
    }

    private fun processKakaoLogin(accessToken: String) {
        viewModelScope.launch {
            authRepository
                .kakaoLogin(accessToken)
                .onSuccess {
                    sendSideEffect(LoginSideEffect.NavigateToHome)
                }.onFailure {
                    sendSideEffect(LoginSideEffect.ShowSnackbar(it.message ?: "카카오 로그인에 실패했습니다."))
                }
            updateState { it.copy(isLoading = false) }
        }
    }

    private fun generateSecureRandomNonce(byteLength: Int = 32): String {
        val randomBytes = ByteArray(byteLength)
        SecureRandom.getInstanceStrong().nextBytes(randomBytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes)
    }
}
