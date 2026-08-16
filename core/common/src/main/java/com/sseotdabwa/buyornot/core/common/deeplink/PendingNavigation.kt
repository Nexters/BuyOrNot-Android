package com.sseotdabwa.buyornot.core.common.deeplink

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 외부 유입으로 정해진 이동 대상. 인증 게이트를 통과할 때까지 들고 있다가 한 번만 소비된다.
 *
 * @param feedId [NavigationDestination.FEED_DETAIL]일 때만 존재한다.
 * @param notificationId 읽음 처리에 쓴다. 마케팅 알림과 앱 링크에는 없다.
 */
data class PendingNavigation(
    val destination: NavigationDestination,
    val feedId: Long? = null,
    val notificationId: Long? = null,
)

/**
 * 인증 게이트 통과 전까지 이동 대상을 보관하는 저장소.
 *
 * **Activity가 아니라 여기 두는 이유**: 진입 Intent의 extras·data는 처리 직후 소비되므로,
 * 이동이 실행되기 전에 Activity가 재생성되면 Intent에도 값이 없고 Activity 필드도 초기화되어
 * 목적지가 양쪽에서 사라진다. 재생성 경로는 회전만이 아니다 — `MainActivity`에 `configChanges`가
 * 없어 폴더블 접기·폰트 크기·다크모드·언어 변경이 모두 재생성을 유발한다.
 *
 * 손실 구간도 짧지 않다. 스플래시가 고정 대기를 하고, 미로그인 사용자는 로그인 화면에서
 * 무한정 머문다 — 마케팅 알림의 대상이 바로 그 이탈 사용자다.
 *
 * `@Singleton`이라 Activity 수명과 무관하게 살아남는다. 프로세스가 죽으면 함께 사라지지만,
 * 그 경우 Intent도 함께 사라지므로 어떤 방식으로도 복원할 수 없는 범위다.
 *
 * **`app`이 아니라 `core/common`에 두는 이유**: `MainActivity`가 쓰고 `BuyOrNotApp`이 소비하는 게
 * 주 흐름이지만, `feature/auth`의 스플래시도 «외부 유입인지»를 알아야 고정 대기를 건너뛸 수 있다.
 * 두 모듈이 함께 보는 상태다.
 */
@Singleton
class PendingNavigationStore @Inject constructor() {
    private val _pending = MutableStateFlow<PendingNavigation?>(null)
    val pending: StateFlow<PendingNavigation?> = _pending.asStateFlow()

    /** 외부 유입으로 진입했는가. 스플래시가 고정 대기를 건너뛸지 판단할 때 쓴다. */
    val hasPending: Boolean get() = _pending.value != null

    fun set(navigation: PendingNavigation) {
        _pending.value = navigation
    }

    fun consume() {
        _pending.value = null
    }
}
