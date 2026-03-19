package com.sseotdabwa.buyornot.feature.mypage.viewmodel

import com.sseotdabwa.buyornot.core.designsystem.components.SnackBarIconTint
import com.sseotdabwa.buyornot.core.designsystem.icon.IconResource
import com.sseotdabwa.buyornot.feature.mypage.ui.BlockedUserItem

data class BlockedAccountsUiState(
    val isLoading: Boolean = false,
    val blockedUsers: List<BlockedUserItem> = emptyList(),
)

sealed interface BlockedAccountsIntent {
    data object LoadBlockedUsers : BlockedAccountsIntent

    data class UnblockUser(
        val userId: Long,
        val nickname: String,
    ) : BlockedAccountsIntent

    data class BlockUser(
        val userId: Long,
        val nickname: String,
    ) : BlockedAccountsIntent
}

sealed interface BlockedAccountsSideEffect {
    data class ShowSnackbar(
        val message: String,
        val icon: IconResource? = null,
        val iconTint: SnackBarIconTint = SnackBarIconTint.Success,
    ) : BlockedAccountsSideEffect
}
