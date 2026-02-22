package com.sseotdabwa.buyornot.feature.notification.ui

import androidx.compose.runtime.Immutable
import com.sseotdabwa.buyornot.domain.model.Feed

@Immutable
data class NotificationDetailUiState(
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val feed: Feed? = null,
)

sealed interface NotificationDetailIntent {
    data object OnRefresh : NotificationDetailIntent
}

sealed interface NotificationDetailSideEffect
