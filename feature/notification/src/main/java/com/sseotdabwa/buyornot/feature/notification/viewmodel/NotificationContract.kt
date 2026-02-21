package com.sseotdabwa.buyornot.feature.notification.viewmodel

import androidx.compose.runtime.Immutable

/**
 * 알림 화면의 탭/필터 정의
 */
enum class NotificationFilter(
    val label: String,
) {
    ALL("전체"),
    MY_VOTE("내가 올린 투표"),
    PARTICIPATED("참여한 투표"),
}

/**
 * 알림 아이템 데이터 모델
 */
@Immutable
data class NotificationItem(
    val id: String,
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
}

/**
 * 알림 화면의 일회성 이벤트 (MVI SideEffect)
 */
sealed interface NotificationSideEffect {
    data object RequestNotificationPermission : NotificationSideEffect

    data object OpenAppSettings : NotificationSideEffect
}
