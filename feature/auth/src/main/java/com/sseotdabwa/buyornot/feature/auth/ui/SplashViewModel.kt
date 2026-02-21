package com.sseotdabwa.buyornot.feature.auth.ui

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.sseotdabwa.buyornot.core.common.util.runCatchingCancellable
import com.sseotdabwa.buyornot.core.ui.base.BaseViewModel
import com.sseotdabwa.buyornot.domain.model.UserType
import com.sseotdabwa.buyornot.domain.repository.UserPreferencesRepository
import com.sseotdabwa.buyornot.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

private const val SPLASH_TIMEOUT_MILLIS = 2300L

/**
 * 스플래시 화면을 위한 ViewModel
 *
 * 토큰 존재 여부를 확인하고, 2.3초 후 자동으로 네비게이션 SideEffect를 방출합니다.
 */
@HiltViewModel
class SplashViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : BaseViewModel<SplashUiState, SplashIntent, SplashSideEffect>(SplashUiState()) {
    init {
        checkTokenAndNavigate()
        updateFcmToken()
    }

    override fun handleIntent(intent: SplashIntent) {
        // 스플래시 화면은 사용자 액션이 없으므로 비어있음
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
                    if (e !is CancellationException) {
                        Log.e("FCM", "Failed to update FCM token to server", e)
                    }
                }
            }
        }
    }

    /**
     * 토큰 존재 여부를 확인하고 적절한 화면으로 이동
     */
    private fun checkTokenAndNavigate() {
        viewModelScope.launch {
            val hasValidToken =
                try {
                    val userType = userPreferencesRepository.userType.first()
                    userType != UserType.GUEST
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    false // DataStore 오류 시 비로그인 화면으로 폴백
                }

            delay(SPLASH_TIMEOUT_MILLIS)

            // 토큰 유효성에 따라 SideEffect 방출
            if (hasValidToken) {
                sendSideEffect(SplashSideEffect.NavigateToHome)
            } else {
                sendSideEffect(SplashSideEffect.NavigateToLogin)
            }

            updateState { it.copy(isLoading = false) }
        }
    }
}
