package com.sseotdabwa.buyornot.feature.notification.ui

import androidx.compose.runtime.Immutable
import com.sseotdabwa.buyornot.core.designsystem.components.SnackBarIconTint
import com.sseotdabwa.buyornot.core.designsystem.icon.IconResource
import com.sseotdabwa.buyornot.core.designsystem.image.ImageModel
import com.sseotdabwa.buyornot.domain.model.NotificationFilter
import com.sseotdabwa.buyornot.domain.model.NotificationType

/**
 * 알림 아이템 데이터 모델
 *
 * @property time 서버가 알림 생성 시각을 주지 않아 투표 마감 시각으로 쓴다. 답글 알림처럼 그 시각이 의미 없으면 null이다.
 */
@Immutable
data class NotificationItem(
    val id: Long,
    val feedId: Long,
    val commentId: Long?,
    val type: NotificationType,
    val image: ImageModel,
    val headline: String,
    val message: String,
    val time: String?,
    val isRead: Boolean,
)

/**
 * 알림 화면의 UI 상태 (MVI State)
 *
 * 필터는 한 번 받은 목록을 화면에서 거른다. 칩마다 안 읽은 알림이 있는지 알려면 전체 목록이 필요하다.
 */
@Immutable
data class NotificationUiState(
    val isError: Boolean = false,
    val selectedFilter: NotificationFilter = NotificationFilter.ALL,
    val hasNotificationPermission: Boolean = false,
    val hasRequestedPermission: Boolean = false,
    val allNotifications: List<NotificationItem> = emptyList(),
) {
    val notifications: List<NotificationItem>
        get() = allNotifications.filter { selectedFilter.matches(it.type) }

    val unreadFilters: Set<NotificationFilter>
        get() =
            allNotifications
                .filterNot { it.isRead }
                .mapNotNullTo(mutableSetOf()) { NotificationFilter.categoryOf(it.type) }
}

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
        val notification: NotificationItem,
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
        val commentId: Long?,
    ) : NotificationSideEffect
}
