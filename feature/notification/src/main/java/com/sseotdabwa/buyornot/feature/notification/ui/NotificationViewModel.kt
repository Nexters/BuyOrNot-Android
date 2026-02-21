package com.sseotdabwa.buyornot.feature.notification.ui

import androidx.lifecycle.viewModelScope
import com.sseotdabwa.buyornot.core.ui.base.BaseViewModel
import com.sseotdabwa.buyornot.domain.repository.AppPreferencesRepository
import com.sseotdabwa.buyornot.feature.notification.viewmodel.NotificationIntent
import com.sseotdabwa.buyornot.feature.notification.viewmodel.NotificationItem
import com.sseotdabwa.buyornot.feature.notification.viewmodel.NotificationSideEffect
import com.sseotdabwa.buyornot.feature.notification.viewmodel.NotificationUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 알림 화면을 위한 ViewModel
 * MVI 패턴을 적용하여 NotificationUiState, NotificationIntent, NotificationSideEffect를 관리합니다.
 */
@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val appPreferencesRepository: AppPreferencesRepository,
) : BaseViewModel<NotificationUiState, NotificationIntent, NotificationSideEffect>(NotificationUiState()) {
    init {
        loadPermissionState()
        loadDummyNotifications()
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
        }
    }

    private fun handleFilterSelection(filter: com.sseotdabwa.buyornot.feature.notification.viewmodel.NotificationFilter) {
        updateState { it.copy(selectedFilter = filter) }
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
     * 배너 클릭 시 권한 요청 또는 설정 화면 이동 판단
     */
    fun handleBannerClick(
        shouldShowRationale: Boolean,
        hasRequestedPermission: Boolean,
    ) {
        viewModelScope.launch {
            when {
                // Case 1: 1회 거부 후 (재요청 가능)
                shouldShowRationale -> {
                    sendSideEffect(NotificationSideEffect.RequestNotificationPermission)
                }
                // Case 2: 영구 거부 (설정으로 이동)
                hasRequestedPermission -> {
                    sendSideEffect(NotificationSideEffect.OpenAppSettings)
                }
                // Case 3: 첫 요청
                else -> {
                    handlePermissionRequest()
                }
            }
        }
    }

    /**
     * 권한 상태 업데이트 (화면 재진입 시)
     */
    fun updatePermissionState(hasPermission: Boolean) {
        updateState { it.copy(hasNotificationPermission = hasPermission) }
    }

    /**
     * 더미 알림 데이터 로드 (임시)
     */
    private fun loadDummyNotifications() {
        val dummyNotifications =
            List(10) { index ->
                NotificationItem(
                    id = "${index + 1}",
                    imageUrl = "https://picsum.photos/20$index",
                    title = "투표 종료",
                    description =
                        when (index % 5) {
                            0 -> "78% '애매하긴 해!'"
                            1 -> "56% '사! 가즈아!'"
                            2 -> "90% '애매하긴 해!'"
                            3 -> "무승부! 2차전 가보자고!"
                            else -> "결과를 확인해보세요"
                        },
                    time =
                        when {
                            index < 1 -> "${index + 6}시간 전"
                            index < 3 -> "${index}일 전"
                            index < 6 -> "${index - 2}일 전"
                            else -> "${(index - 5) / 7 + 1}주 전"
                        },
                    isRead = index > 1,
                )
            }
        updateState { it.copy(notifications = dummyNotifications) }
    }
}
