package com.sseotdabwa.buyornot.feature.mypage.viewmodel

import androidx.lifecycle.viewModelScope
import com.sseotdabwa.buyornot.core.common.util.runCatchingCancellable
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotIcons
import com.sseotdabwa.buyornot.core.ui.base.BaseViewModel
import com.sseotdabwa.buyornot.core.ui.nickname.NicknamePolicy
import com.sseotdabwa.buyornot.domain.exception.ApiException
import com.sseotdabwa.buyornot.domain.repository.FeedRepository
import com.sseotdabwa.buyornot.domain.repository.UserPreferencesRepository
import com.sseotdabwa.buyornot.domain.repository.UserRepository
import com.sseotdabwa.buyornot.feature.mypage.image.ProfileImageReader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * 계정 설정에서 진입하는 프로필 설정(닉네임·프로필 이미지 수정) 화면의 ViewModel
 *
 * 닉네임은 서버 정책상 20일마다 1번만 바꿀 수 있어 변경 전에 확인 다이얼로그를 띄우고,
 * 이미지만 바꾸는 경우에는 바로 저장한다.
 */
@HiltViewModel
class ProfileEditViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val feedRepository: FeedRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val profileImageReader: ProfileImageReader,
) : BaseViewModel<ProfileEditUiState, ProfileEditIntent, ProfileEditSideEffect>(ProfileEditUiState()) {
    init {
        loadProfile()
    }

    override fun handleIntent(intent: ProfileEditIntent) {
        when (intent) {
            is ProfileEditIntent.UpdateNickname -> updateNickname(intent.nickname)
            is ProfileEditIntent.SelectImage -> updateState { it.copy(selectedImageUri = intent.uri) }
            ProfileEditIntent.Submit -> submit()
            ProfileEditIntent.ConfirmNicknameChange -> {
                updateState { it.copy(isNicknameDialogVisible = false) }
                save()
            }
            ProfileEditIntent.DismissNicknameDialog -> updateState { it.copy(isNicknameDialogVisible = false) }
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            updateState { it.copy(isLoading = true) }
            runCatchingCancellable {
                userRepository.getMyProfile()
            }.onSuccess { profile ->
                val nickname = profile.nickname.orEmpty()
                updateState {
                    it.copy(
                        isLoading = false,
                        originalNickname = nickname,
                        nickname = nickname,
                        profileImageUrl = profile.profileImage,
                        isProfileLoaded = true,
                    )
                }
            }.onFailure { throwable ->
                Timber.w(throwable, "Failed to load profile")
                updateState { it.copy(isLoading = false) }
                sendSideEffect(ProfileEditSideEffect.ShowSnackbar("프로필을 불러오지 못했어요."))
                sendSideEffect(ProfileEditSideEffect.NavigateBack)
            }
        }
    }

    private fun updateNickname(nickname: String) {
        // 불러오기 전에 입력하면 불러온 닉네임에 덮어써지므로 받지 않는다.
        if (!currentState.isProfileLoaded) return
        updateState { it.copy(nickname = NicknamePolicy.limit(nickname), errorMessage = null) }
    }

    private fun submit() {
        if (!currentState.isSubmitEnabled) return

        // 공백만 입력하면 서버는 빈 문자열을 특수문자 에러(USER_007)로 돌려주므로 글자 수 문구를 직접 보여준다.
        if (currentState.nickname.trim().isEmpty()) {
            updateState { it.copy(errorMessage = NicknamePolicy.errorMessageOf("USER_009")) }
            return
        }

        // 닉네임은 20일에 1번만 바꿀 수 있으므로 한 번 더 묻는다.
        if (currentState.isNicknameChanged) {
            updateState { it.copy(isNicknameDialogVisible = true) }
        } else {
            save()
        }
    }

    private fun save() {
        if (currentState.isLoading) return

        val state = currentState
        // 바뀌지 않은 값은 null로 보내 서버가 건드리지 않게 한다.
        val nickname = state.nickname.trim().takeIf { state.isNicknameChanged }
        val imageUri = state.selectedImageUri

        viewModelScope.launch {
            updateState { it.copy(isLoading = true) }
            runCatchingCancellable {
                val profileImage = imageUri?.let { uploadImage(it) }
                userRepository.updateProfile(nickname = nickname, profileImage = profileImage)
            }.onSuccess { profile ->
                runCatchingCancellable {
                    profile.nickname?.let { userPreferencesRepository.updateDisplayName(it) }
                    userPreferencesRepository.updateProfileImageUrl(profile.profileImage)
                }.onFailure { Timber.w(it, "Failed to update user preferences") }
                sendSideEffect(
                    ProfileEditSideEffect.ShowSnackbar(
                        message = "프로필을 수정했어요.",
                        icon = BuyOrNotIcons.CheckCircle,
                    ),
                )
                // 화면을 떠날 때까지 isLoading을 유지해 CTA가 다시 눌리지 않게 한다.
                sendSideEffect(ProfileEditSideEffect.NavigateBackWithUpdate)
            }.onFailure { throwable ->
                Timber.e(throwable, "Failed to update profile")
                val nicknameError = (throwable as? ApiException)?.code?.let(NicknamePolicy::errorMessageOf)
                updateState { it.copy(isLoading = false, errorMessage = nicknameError) }
                if (nicknameError == null) {
                    sendSideEffect(ProfileEditSideEffect.ShowSnackbar("프로필을 수정하지 못했어요. 다시 시도해주세요."))
                }
            }
        }
    }

    /** presigned URL을 받아 S3에 올리고, 프로필 수정 API에 넘길 조회용 URL을 돌려준다. */
    private suspend fun uploadImage(uri: String): String {
        val file = profileImageReader.read(uri)
        val uploadInfo = feedRepository.getPresignedUrl(file.fileName, file.contentType)
        feedRepository.uploadImage(uploadInfo.uploadUrl, file.bytes, file.contentType)
        return uploadInfo.viewUrl
    }
}
