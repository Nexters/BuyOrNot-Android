package com.sseotdabwa.buyornot.feature.notification.ui

import androidx.lifecycle.viewModelScope
import com.sseotdabwa.buyornot.core.common.util.TimeUtils
import com.sseotdabwa.buyornot.core.common.util.runCatchingCancellable
import com.sseotdabwa.buyornot.core.ui.base.BaseViewModel
import com.sseotdabwa.buyornot.domain.model.NotificationFilter
import com.sseotdabwa.buyornot.domain.repository.AppPreferencesRepository
import com.sseotdabwa.buyornot.domain.repository.NotificationRepository
import com.sseotdabwa.buyornot.feature.notification.viewmodel.NotificationIntent
import com.sseotdabwa.buyornot.feature.notification.viewmodel.NotificationItem
import com.sseotdabwa.buyornot.feature.notification.viewmodel.NotificationSideEffect
import com.sseotdabwa.buyornot.feature.notification.viewmodel.NotificationUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val appPreferencesRepository: AppPreferencesRepository,
    private val notificationRepository: NotificationRepository,
) : BaseViewModel<NotificationUiState, NotificationIntent, NotificationSideEffect>(NotificationUiState()) {
    init {
        loadPermissionState()
        loadNotifications()
    }

    /**
     * 권한 상태 초기화 (깜빡임 방지)
     * Context를 통해 현재 권한 상태를 즉시 설정
     */
    fun initializePermissionState(hasPermission: Boolean) {
        updateState { it.copy(hasNotificationPermission = hasPermission) }
    }

    /**
     * DataStore에서 권한 요청 이력 로드
     */
    private fun loadPermissionState() {
        viewModelScope.launch {
            val hasRequested = appPreferencesRepository.hasRequestedNotificationPermission.first()
            updateState { it.copy(hasRequestedPermission = hasRequested) }
        }
    }

    override fun handleIntent(intent: NotificationIntent) {
        when (intent) {
            is NotificationIntent.OnFilterSelected -> handleFilterSelection(intent.filter)
            is NotificationIntent.OnPermissionRequested -> handlePermissionRequest()
            is NotificationIntent.OnPermissionGranted -> handlePermissionGranted()
            is NotificationIntent.OnPermissionDenied -> handlePermissionDenied()
            is NotificationIntent.OnNotificationClick -> handleNotificationClick(intent.notificationId)
            is NotificationIntent.OnRefreshNotifications -> loadNotifications()
        }
    }

    private fun handleFilterSelection(filter: NotificationFilter) {
        if (uiState.value.selectedFilter == filter) return
        updateState { it.copy(selectedFilter = filter) }
        loadNotifications()
    }

    private fun handlePermissionRequest() {
        viewModelScope.launch {
            // 권한 요청 시도 기록 (영구 거부 판단용)
            appPreferencesRepository.updateNotificationPermissionRequested(true)
            updateState { it.copy(hasRequestedPermission = true) }

            // SideEffect 방출
            sendSideEffect(NotificationSideEffect.RequestNotificationPermission)
        }
    }

    private fun handlePermissionGranted() {
        updateState { it.copy(hasNotificationPermission = true) }
    }

    private fun handlePermissionDenied() {
        updateState { it.copy(hasNotificationPermission = false) }
    }

    /**
     * 알림 클릭 처리 (읽음 처리 API 호출)
     */
    private fun handleNotificationClick(notificationId: String) {
        viewModelScope.launch {
            // 화면 이동 SideEffect 발생
            sendSideEffect(NotificationSideEffect.NavigateToNotificationDetail(notificationId))

            runCatchingCancellable {
                notificationRepository.markAsRead(notificationId.toLong())
            }.onSuccess {
                // UI 상태 업데이트 (읽음 처리된 상태로 변경)
                val updatedNotifications =
                    uiState.value.notifications.map {
                        if (it.id == notificationId) it.copy(isRead = true) else it
                    }
                updateState { it.copy(notifications = updatedNotifications) }
            }
        }
    }

    /**
     * 배너 클릭 시 권한 요청 또는 설정 화면 이동 판단
     */
    fun handleBannerClick(shouldShowRationale: Boolean) {
        val hasRequestedPermission = uiState.value.hasRequestedPermission
        when {
            shouldShowRationale -> sendSideEffect(NotificationSideEffect.RequestNotificationPermission)
            hasRequestedPermission -> sendSideEffect(NotificationSideEffect.OpenAppSettings)
            else -> handlePermissionRequest()
        }
    }

    /**
     * 권한 상태 업데이트 (화면 재진입 시)
     */
    fun updatePermissionState(hasPermission: Boolean) {
        updateState { it.copy(hasNotificationPermission = hasPermission) }
    }

    /**
     * 실제 알림 데이터 로드
     */
    private fun loadNotifications() {
        viewModelScope.launch {
            val type =
                when (uiState.value.selectedFilter) {
                    NotificationFilter.ALL -> null
                    NotificationFilter.MY_VOTE -> "MY_FEED_CLOSED"
                    NotificationFilter.PARTICIPATED -> "PARTICIPATED_FEED_CLOSED"
                }

            runCatchingCancellable {
                notificationRepository.getNotifications(type)
            }.onSuccess { notifications ->
                val notificationItems =
                    notifications.map {
                        NotificationItem(
                            id = it.notificationId.toString(),
                            imageUrl = it.viewUrl,
                            title = it.title,
                            description = it.body,
                            time = TimeUtils.formatRelativeTime(it.voteClosedAt),
                            isRead = it.isRead,
                        )
                    }
                updateState {
                    it.copy(
                        notifications = notificationItems,
                        isError = false,
                    )
                }
            }.onFailure {
                updateState { it.copy(isError = true) }
            }
        }
    }
}
