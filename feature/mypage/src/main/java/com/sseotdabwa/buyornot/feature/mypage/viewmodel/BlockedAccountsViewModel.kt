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
            is BlockedAccountsIntent.UnblockUser -> unblockUser(intent.userId, intent.nickname)
        }
    }

    private fun unblockUser(
        userId: Long,
        nickname: String,
    ) {
        viewModelScope.launch {
            runCatchingCancellable {
                userRepository.unblockUser(userId)
            }.onSuccess {
                updateState { it.copy(blockedUsers = it.blockedUsers.filter { user -> user.userId != userId }) }
                sendSideEffect(BlockedAccountsSideEffect.ShowSnackbar("${nickname}의 차단이 해제되었어요."))
            }.onFailure { throwable ->
                Log.w(TAG, throwable.toString())
            }
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
