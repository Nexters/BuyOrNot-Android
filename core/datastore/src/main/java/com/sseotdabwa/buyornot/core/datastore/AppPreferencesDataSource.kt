package com.sseotdabwa.buyornot.core.datastore

import kotlinx.coroutines.flow.Flow

/**
 * 앱 전역 설정 및 권한 상태 DataSource
 *
 * 사용자 정보와 무관한 앱 레벨의 설정을 관리합니다.
 */
interface AppPreferencesDataSource {
    /**
     * 앱 설정 전체를 Flow로 제공
     */
    val preferences: Flow<AppPreferences>

    /**
     * 알림 권한 요청 이력을 Flow로 제공
     */
    val hasRequestedNotificationPermission: Flow<Boolean>

    /**
     * 최초 실행 여부를 Flow로 제공
     */
    val isFirstRun: Flow<Boolean>

    /**
     * 소프트 업데이트 다이얼로그 마지막 노출 시각 (epoch millis)을 Flow로 제공
     */
    val lastSoftUpdateShownTime: Flow<Long>

    /**
     * 알림 권한 요청 이력 업데이트
     */
    suspend fun updateNotificationPermissionRequested(requested: Boolean)

    /**
     * 최초 실행 여부 업데이트
     */
    suspend fun updateIsFirstRun(isFirstRun: Boolean)

    /**
     * 소프트 업데이트 다이얼로그 마지막 노출 시각 업데이트
     */
    suspend fun updateLastSoftUpdateShownTime(timeMillis: Long)

    /**
     * [date]의 AI 초안 생성 횟수를 1 늘린다. 이미 [limit]에 도달했으면 늘리지 않고 false를 반환한다.
     */
    suspend fun tryConsumePostDraftQuota(
        date: String,
        limit: Int,
    ): Boolean
}
