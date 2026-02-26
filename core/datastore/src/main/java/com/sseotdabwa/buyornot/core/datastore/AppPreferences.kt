package com.sseotdabwa.buyornot.core.datastore

/**
 * 앱 전역 설정 및 권한 상태
 *
 * 사용자 정보와 무관한 앱 레벨의 설정을 관리합니다.
 */
data class AppPreferences(
    /**
     * 알림 권한 요청 이력
     * 영구 거부 판단을 위해 사용
     */
    val hasRequestedNotificationPermission: Boolean = false,
    /**
     * 최초 실행 여부
     */
    val isFirstRun: Boolean = true,
)
