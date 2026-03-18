package com.sseotdabwa.buyornot.feature.notification.ui

import androidx.compose.runtime.Immutable
import com.sseotdabwa.buyornot.core.designsystem.icon.IconResource
import com.sseotdabwa.buyornot.domain.model.Feed

@Immutable
data class NotificationDetailUiState(
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val feed: Feed? = null,
    val voterProfileImageUrl: String = "",
    val isOwner: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val showBlockDialog: Boolean = false,
    val isGuest: Boolean = false,
)

sealed interface NotificationDetailIntent {
    data object OnRefresh : NotificationDetailIntent

    data object ShowDeleteDialog : NotificationDetailIntent

    data object DismissDeleteDialog : NotificationDetailIntent

    data object OnDeleteConfirmed : NotificationDetailIntent

    data object OnReportClicked : NotificationDetailIntent

    data object ShowBlockDialog : NotificationDetailIntent

    data object DismissBlockDialog : NotificationDetailIntent

    data object OnBlockConfirmed : NotificationDetailIntent
}

sealed interface NotificationDetailSideEffect {
    data class ShowSnackbar(
        val message: String,
        val icon: IconResource? = null,
    ) : NotificationDetailSideEffect

    data object NavigateBack : NotificationDetailSideEffect
}
