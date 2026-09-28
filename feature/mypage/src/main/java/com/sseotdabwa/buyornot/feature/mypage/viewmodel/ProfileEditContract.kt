package com.sseotdabwa.buyornot.feature.mypage.viewmodel

import androidx.compose.runtime.Immutable
import com.sseotdabwa.buyornot.core.designsystem.components.SnackBarIconTint
import com.sseotdabwa.buyornot.core.designsystem.icon.IconResource

/**
 * 프로필 설정(수정) 화면의 UI 상태
 *
 * @property originalNickname 서버에 저장된 현재 닉네임. 변경 여부 판단 기준
 * @property nickname 입력 중인 닉네임 (최대 10자)
 * @property profileImageUrl 서버에 저장된 현재 프로필 이미지 URL
 * @property selectedImageUri 새로 고른(자른) 이미지 URI. 고르지 않았으면 null
 * @property errorMessage 서버가 거절한 사유. 입력이 바뀌면 지운다.
 * @property isLoading 프로필 조회·수정 요청 중 여부
 * @property isNicknameDialogVisible 닉네임 변경 확인 다이얼로그 노출 여부
 * @property isProfileLoaded 현재 프로필을 불러왔는지 여부. 불러오기 전에는 입력·저장을 막는다.
 */
@Immutable
data class ProfileEditUiState(
    val originalNickname: String = "",
    val nickname: String = "",
    val profileImageUrl: String = "",
    val selectedImageUri: String? = null,
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
    val isNicknameDialogVisible: Boolean = false,
    val isProfileLoaded: Boolean = false,
) {
    // 프로필을 불러온 뒤의 로딩은 저장 중이다.
    val isSaving: Boolean
        get() = isLoading && isProfileLoaded

    // 앞뒤 공백은 저장할 때 지우므로 비교도 공백을 뺀 값으로 한다.
    val isNicknameChanged: Boolean
        get() = nickname.trim() != originalNickname

    val isSubmitEnabled: Boolean
        get() =
            isProfileLoaded &&
                (isNicknameChanged || selectedImageUri != null) &&
                errorMessage == null &&
                nickname.isNotEmpty() &&
                !isLoading
}

sealed interface ProfileEditIntent {
    data class UpdateNickname(
        val nickname: String,
    ) : ProfileEditIntent

    /** 자르기까지 끝낸 이미지를 새 프로필 이미지로 고른다. */
    data class SelectImage(
        val uri: String,
    ) : ProfileEditIntent

    data object Submit : ProfileEditIntent

    data object ConfirmNicknameChange : ProfileEditIntent

    data object DismissNicknameDialog : ProfileEditIntent
}

sealed interface ProfileEditSideEffect {
    data class ShowSnackbar(
        val message: String,
        val icon: IconResource? = null,
        val iconTint: SnackBarIconTint = SnackBarIconTint.Success,
    ) : ProfileEditSideEffect

    /** 현재 프로필을 불러오지 못해 수정할 수 없으므로 이전 화면으로 돌아간다. */
    data object NavigateBack : ProfileEditSideEffect

    /** 저장에 성공해 이전 화면으로 돌아간다. 이전 화면들은 프로필을 다시 불러온다. */
    data object NavigateBackWithUpdate : ProfileEditSideEffect
}
