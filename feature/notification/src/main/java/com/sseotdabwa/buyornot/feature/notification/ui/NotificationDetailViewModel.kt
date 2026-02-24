package com.sseotdabwa.buyornot.feature.notification.ui

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.sseotdabwa.buyornot.core.common.util.runCatchingCancellable
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotIcons
import com.sseotdabwa.buyornot.core.ui.base.BaseViewModel
import com.sseotdabwa.buyornot.domain.repository.FeedRepository
import com.sseotdabwa.buyornot.domain.repository.NotificationRepository
import com.sseotdabwa.buyornot.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val feedRepository: FeedRepository,
    private val notificationRepository: NotificationRepository,
    private val userRepository: UserRepository,
) : BaseViewModel<NotificationDetailUiState, NotificationDetailIntent, NotificationDetailSideEffect>(
        NotificationDetailUiState(),
    ) {
    private val notificationId: Long = checkNotNull(savedStateHandle["notificationId"])
    private val feedId: Long = checkNotNull(savedStateHandle["feedId"])

    private var currentUserId: Long? = null

    init {
        loadDetail()
        markAsRead()
    }

    override fun handleIntent(intent: NotificationDetailIntent) {
        when (intent) {
            NotificationDetailIntent.OnRefresh -> loadDetail()
            NotificationDetailIntent.OnDeleteClicked -> handleDelete()
            NotificationDetailIntent.OnReportClicked -> handleReport()
        }
    }

    private fun loadDetail() {
        viewModelScope.launch {
            updateState { it.copy(isLoading = true, isError = false) }
            runCatchingCancellable {
                if (currentUserId == null) {
                    currentUserId = userRepository.getMyProfile().id
                }
                feedRepository.getFeed(feedId)
            }.onSuccess { feed ->
                val isOwner = currentUserId != null && feed.author.userId == currentUserId
                updateState { it.copy(isLoading = false, feed = feed, isOwner = isOwner) }
            }.onFailure {
                updateState { it.copy(isLoading = false, isError = true) }
            }
        }
    }

    private fun handleDelete() {
        viewModelScope.launch {
            runCatchingCancellable {
                feedRepository.deleteFeed(feedId)
            }.onSuccess {
                sendSideEffect(
                    NotificationDetailSideEffect.ShowSnackbar(
                        message = "삭제가 완료되었습니다.",
                        icon = BuyOrNotIcons.CheckCircle,
                    ),
                )
                sendSideEffect(NotificationDetailSideEffect.NavigateBack)
            }.onFailure { e ->
                Log.e("NotificationDetailViewModel", "Failed to delete feed: $feedId", e)
                sendSideEffect(
                    NotificationDetailSideEffect.ShowSnackbar(
                        message = "삭제에 실패했습니다.",
                        icon = null,
                    ),
                )
            }
        }
    }

    private fun handleReport() {
        viewModelScope.launch {
            runCatchingCancellable {
                feedRepository.reportFeed(feedId)
            }.onSuccess {
                sendSideEffect(
                    NotificationDetailSideEffect.ShowSnackbar(
                        message = "신고가 완료되었습니다.",
                        icon = BuyOrNotIcons.CheckCircle,
                    ),
                )
            }.onFailure { e ->
                Log.e("NotificationDetailViewModel", "Failed to report feed: $feedId", e)
                val errorMessage =
                    when {
                        e.message?.contains("400") == true -> "이미 신고한 피드이거나 본인의 피드입니다."
                        else -> "신고에 실패했습니다."
                    }
                sendSideEffect(
                    NotificationDetailSideEffect.ShowSnackbar(
                        message = errorMessage,
                        icon = null,
                    ),
                )
            }
        }
    }

    private fun markAsRead() {
        viewModelScope.launch {
            runCatchingCancellable {
                notificationRepository.markAsRead(notificationId)
            }
        }
    }
}
