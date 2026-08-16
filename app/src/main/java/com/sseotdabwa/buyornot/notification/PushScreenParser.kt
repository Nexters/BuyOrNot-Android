package com.sseotdabwa.buyornot.notification

import com.sseotdabwa.buyornot.core.common.deeplink.NavigationDestination

/**
 * FCM data의 `screen`과 `feedId` 조합에서 이동 대상을 정한다.
 *
 * 계측기 없이 단위 테스트로 규칙을 고정하기 위해 Intent가 아니라 이미 분해된 값을 받는다
 * (`feedIdFromAppLink`와 같은 이유).
 *
 * 규칙:
 * - **feedId가 있으면 피드 상세가 이긴다.** 서버가 `screen=HOME` + `feedId`처럼 모순된 payload를
 *   보내도 «가장 구체적인 목적지»로 가는 편이 안전하고, 기존 동작과도 호환된다.
 * - feedId 없는 `FEED_DETAIL`은 이동할 대상이 없으므로 null. 앱만 열린다.
 * - 알 수 없는 `screen`도 null. 신규 화면이 추가돼도 구버전 앱이 깨지지 않고 홈으로 열린다.
 *
 * @param screen 수신 Intent의 [FcmKeys.SCREEN] extra
 * @param feedId 수신 Intent의 [FcmKeys.FEED_ID] extra (파싱된 값)
 * @return 이동 대상, 정할 수 없으면 null
 */
fun pushDestinationOf(
    screen: String?,
    feedId: Long?,
): NavigationDestination? {
    if (feedId != null) return NavigationDestination.FEED_DETAIL
    if (screen == null) return null
    return NavigationDestination.entries
        .find { it.name == screen }
        ?.takeIf { it != NavigationDestination.FEED_DETAIL }
}
