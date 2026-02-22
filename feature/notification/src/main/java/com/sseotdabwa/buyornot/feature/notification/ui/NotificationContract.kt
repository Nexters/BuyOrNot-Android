package com.sseotdabwa.buyornot.feature.notification.ui

import androidx.compose.runtime.Immutable
import com.sseotdabwa.buyornot.core.designsystem.components.SnackBarIconTint
import com.sseotdabwa.buyornot.core.designsystem.icon.IconResource
import com.sseotdabwa.buyornot.domain.model.NotificationFilter

/**
 * 알림 아이템 데이터 모델
 */
@Immutable
data class NotificationItem(
    val id: Long,
    val feedId: Long,
    val imageUrl: String,
    val title: String,
    val description: String,
    val time: String,
    val isRead: Boolean,
)

/**
 * 알림 화면의 UI 상태 (MVI State)
 */
@Immutable
data class NotificationUiState(
    val isError: Boolean = false,
    val selectedFilter: NotificationFilter = NotificationFilter.ALL,
    val hasNotificationPermission: Boolean = false,
    val hasRequestedPermission: Boolean = false,
    val notifications: List<NotificationItem> = emptyList(),
)

/**
 * 알림 화면에서 발생하는 사용자 액션 (MVI Intent)
 */
sealed interface NotificationIntent {
    data class OnFilterSelected(
        val filter: NotificationFilter,
    ) : NotificationIntent

    data object OnPermissionRequested : NotificationIntent

    data object OnPermissionGranted : NotificationIntent

    data object OnPermissionDenied : NotificationIntent

    data class OnNotificationClick(
        val notificationId: Long,
        val feedId: Long,
    ) : NotificationIntent

    data object OnRefreshNotifications : NotificationIntent
}

/**
 * 알림 화면의 일회성 이벤트 (MVI SideEffect)
 */
sealed interface NotificationSideEffect {
    data object RequestNotificationPermission : NotificationSideEffect

    data object OpenAppSettings : NotificationSideEffect

    data class ShowSnackbar(
        val message: String,
        val icon: IconResource? = null,
        val iconTint: SnackBarIconTint = SnackBarIconTint.Success,
    ) : NotificationSideEffect

    data class NavigateToNotificationDetail(
        val notificationId: Long,
        val feedId: Long,
    ) : NotificationSideEffect
}
