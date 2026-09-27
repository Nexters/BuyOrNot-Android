package com.sseotdabwa.buyornot.feature.auth.ui

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.sseotdabwa.buyornot.core.common.util.runCatchingCancellable
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotIcons
import com.sseotdabwa.buyornot.core.ui.base.BaseViewModel
import com.sseotdabwa.buyornot.core.ui.nickname.NicknamePolicy
import com.sseotdabwa.buyornot.domain.exception.ApiException
import com.sseotdabwa.buyornot.domain.repository.UserPreferencesRepository
import com.sseotdabwa.buyornot.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

private const val TAG = "NicknameSetupViewModel"

/**
 * 회원가입 직후 닉네임을 최초 설정하는 화면의 ViewModel
 *
 * 닉네임이 없는 계정은 서버가 내 정보 조회·프로필 수정·로그아웃을 제외한 모든 API를 막으므로,
 * 이 화면을 통과해야 홈으로 들어갈 수 있다.
 */
@HiltViewModel
class NicknameSetupViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : BaseViewModel<NicknameSetupUiState, NicknameSetupIntent, NicknameSetupSideEffect>(NicknameSetupUiState()) {
    override fun handleIntent(intent: NicknameSetupIntent) {
        when (intent) {
            is NicknameSetupIntent.UpdateNickname -> updateNickname(intent.nickname)
            NicknameSetupIntent.Submit -> submit()
        }
    }

    private fun updateNickname(nickname: String) {
        updateState { it.copy(nickname = NicknamePolicy.limit(nickname), errorMessage = null) }
    }

    private fun submit() {
        if (!currentState.isSubmitEnabled) return

        // 앞뒤 공백은 에러 없이 지우고 저장한다.
        val nickname = currentState.nickname.trim()
        // 공백만 입력하면 서버는 빈 문자열을 특수문자 에러(USER_007)로 돌려주므로 글자 수 문구를 직접 보여준다.
        if (nickname.isEmpty()) {
            updateState { it.copy(errorMessage = NicknamePolicy.errorMessageOf("USER_009")) }
            return
        }

        viewModelScope.launch {
            updateState { it.copy(isLoading = true) }
            runCatchingCancellable {
                userRepository.updateProfile(nickname = nickname)
            }.onSuccess { profile ->
                runCatchingCancellable {
                    profile.nickname?.let { userPreferencesRepository.updateDisplayName(it) }
                    userPreferencesRepository.updateProfileImageUrl(profile.profileImage)
                }.onFailure { Log.w(TAG, "Failed to update user preferences", it) }
                // 로그인 시점에는 닉네임이 없어 403으로 막혔으므로 여기서 등록한다.
                updateFcmToken()
                sendSideEffect(
                    NicknameSetupSideEffect.ShowSnackbar(
                        message = "회원가입이 완료되었어요.",
                        icon = BuyOrNotIcons.CheckCircle,
                    ),
                )
                // 화면을 떠날 때까지 isLoading을 유지해 CTA가 다시 눌리지 않게 한다.
                sendSideEffect(NicknameSetupSideEffect.NavigateToHome)
            }.onFailure { throwable ->
                Log.e(TAG, "Failed to set nickname", throwable)
                // 키패드가 떠 있어 스낵바는 가려지므로 모든 실패를 입력창 아래에 보여준다.
                val message =
                    (throwable as? ApiException)?.code?.let(NicknamePolicy::errorMessageOf)
                        ?: "닉네임을 설정하지 못했어요. 다시 시도해주세요."
                updateState { it.copy(isLoading = false, errorMessage = message) }
            }
        }
    }

    private suspend fun updateFcmToken() {
        runCatchingCancellable {
            val token = FirebaseMessaging.getInstance().token.await()
            userRepository.updateFcmToken(token)
        }.onFailure {
            Log.e(TAG, "Failed to update FCM token to server", it)
        }
    }
}
