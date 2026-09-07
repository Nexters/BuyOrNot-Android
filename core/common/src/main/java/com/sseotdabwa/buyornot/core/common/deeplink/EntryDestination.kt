package com.sseotdabwa.buyornot.core.common.deeplink

/**
 * 외부 유입(푸시 알림·앱 링크)으로 진입했을 때 이동할 화면.
 *
 * FCM data의 `screen` 값과 이름이 1:1 대응한다. 앱 링크는 항상 [FEED_DETAIL]이다.
 */
enum class EntryDestination {
    FEED_DETAIL,
    HOME,
    FEED_CREATE,
}
