package com.sseotdabwa.buyornot.feature.auth.ui

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.messaging.FirebaseMessaging
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import com.sseotdabwa.buyornot.core.common.util.runCatchingCancellable
import com.sseotdabwa.buyornot.core.ui.base.BaseViewModel
import com.sseotdabwa.buyornot.domain.model.UserType
import com.sseotdabwa.buyornot.domain.repository.AuthRepository
import com.sseotdabwa.buyornot.domain.repository.UserPreferencesRepository
import com.sseotdabwa.buyornot.domain.repository.UserRepository
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
    private val userRepository: UserRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : BaseViewModel<LoginUiState, LoginIntent, LoginSideEffect>(LoginUiState()) {
    override fun handleIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.GoogleLogin -> googleLogin(intent.context)
            is LoginIntent.KakaoLogin -> kakaoLogin(intent.context)
            LoginIntent.SkipLogin -> skipLogin()
        }
    }

    private fun skipLogin() {
        viewModelScope.launch {
            userPreferencesRepository.updateUserType(UserType.GUEST)
            sendSideEffect(LoginSideEffect.NavigateToHome)
        }
    }

    private fun googleLogin(context: Context) {
        viewModelScope.launch {
            updateState { it.copy(isLoading = true) }
            try {
                val googleIdOption =
                    GetSignInWithGoogleOption
                        .Builder(context.getString(R.string.web_client_id))
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

                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    processGoogleLogin(googleIdTokenCredential.idToken)
                } else {
                    Log.d(TAG, credential.type)
                    handleGoogleLoginError("지원하지 않는 자격 증명 유형입니다.")
                }
            } catch (e: GetCredentialCancellationException) {
                updateState { it.copy(isLoading = false) }
            } catch (e: NoCredentialException) {
                Log.e(TAG, "사용 가능한 계정이 없음", e)
                sendSideEffect(LoginSideEffect.ShowSnackbar("로그인 가능한 구글 계정을 찾을 수 없습니다. 테스트 사용자 등록 여부를 확인해주세요."))
                updateState { it.copy(isLoading = false) }
            } catch (e: Exception) {
                Log.e(TAG, "Google 로그인 실패", e)
                handleGoogleLoginError("알 수 없는 오류가 발생했습니다.")
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
            runCatchingCancellable {
                authRepository.googleLogin(idToken)
            }.onSuccess {
                fetchAndStoreUserProfile()
                updateFcmToken()
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
            runCatchingCancellable {
                authRepository.kakaoLogin(accessToken)
            }.onSuccess {
                fetchAndStoreUserProfile()
                updateFcmToken()
                sendSideEffect(LoginSideEffect.NavigateToHome)
            }.onFailure {
                sendSideEffect(LoginSideEffect.ShowSnackbar(it.message ?: "카카오 로그인에 실패했습니다."))
            }
            updateState { it.copy(isLoading = false) }
        }
    }

    private fun fetchAndStoreUserProfile() {
        viewModelScope.launch {
            runCatchingCancellable {
                userRepository.getMyProfile()
            }.onSuccess { profile ->
                userPreferencesRepository.updateDisplayName(profile.nickname)
                userPreferencesRepository.updateProfileImageUrl(profile.profileImage)
            }.onFailure { e ->
                Log.e(TAG, "Failed to fetch user profile after login", e)
            }
        }
    }

    /**
     * FCM 토큰을 가져와 서버에 업데이트합니다.
     */
    private fun updateFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result ?: return@addOnCompleteListener
            viewModelScope.launch {
                runCatchingCancellable {
                    userRepository.updateFcmToken(token)
                }.onSuccess {
                    Log.d("FCM", "FCM Token successfully updated to server.")
                }.onFailure { e ->
                    Log.e("FCM", "Failed to update FCM token to server", e)
                }
            }
        }
    }

    private fun generateSecureRandomNonce(byteLength: Int = 32): String {
        val randomBytes = ByteArray(byteLength)
        SecureRandom.getInstanceStrong().nextBytes(randomBytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes)
    }
}
