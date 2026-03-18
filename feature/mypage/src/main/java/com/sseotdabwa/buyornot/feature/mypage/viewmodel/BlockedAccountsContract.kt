package com.sseotdabwa.buyornot.feature.mypage.viewmodel

import com.sseotdabwa.buyornot.feature.mypage.ui.BlockedUserItem

data class BlockedAccountsUiState(
    val isLoading: Boolean = false,
    val blockedUsers: List<BlockedUserItem> = emptyList(),
)

sealed interface BlockedAccountsIntent {
    data object LoadBlockedUsers : BlockedAccountsIntent
}

sealed interface BlockedAccountsSideEffect
