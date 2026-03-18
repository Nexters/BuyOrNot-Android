package com.sseotdabwa.buyornot.feature.mypage.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.sseotdabwa.buyornot.core.common.util.runCatchingCancellable
import com.sseotdabwa.buyornot.core.ui.base.BaseViewModel
import com.sseotdabwa.buyornot.domain.repository.UserRepository
import com.sseotdabwa.buyornot.feature.mypage.ui.BlockedUserItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "BlockedAccountsViewModel"

@HiltViewModel
class BlockedAccountsViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : BaseViewModel<BlockedAccountsUiState, BlockedAccountsIntent, BlockedAccountsSideEffect>(BlockedAccountsUiState()) {
    init {
        handleIntent(BlockedAccountsIntent.LoadBlockedUsers)
    }

    override fun handleIntent(intent: BlockedAccountsIntent) {
        when (intent) {
            is BlockedAccountsIntent.LoadBlockedUsers -> loadBlockedUsers()
        }
    }

    private fun loadBlockedUsers() {
        viewModelScope.launch {
            updateState { it.copy(isLoading = true) }
            runCatchingCancellable {
                userRepository.getBlockedUsers()
            }.onSuccess { blockedUsers ->
                updateState {
                    it.copy(
                        isLoading = false,
                        blockedUsers =
                            blockedUsers.map { user ->
                                BlockedUserItem(
                                    userId = user.userId,
                                    profileImageUrl = user.profileImage,
                                    nickname = user.nickname,
                                )
                            },
                    )
                }
            }.onFailure { throwable ->
                updateState { it.copy(isLoading = false) }
                Log.w(TAG, throwable.toString())
            }
        }
    }
}
