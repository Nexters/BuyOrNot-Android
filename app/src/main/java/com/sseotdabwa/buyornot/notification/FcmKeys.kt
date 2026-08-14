package com.sseotdabwa.buyornot.notification

/**
 * FCM data 페이로드 / Intent extras 키 모음.
 *
 * feedId 키 이름은 백엔드와 아직 미확정이므로 이 곳 한 곳만 수정하면 되도록 상수화한다.
 */
object FcmKeys {
    const val FEED_ID = "feedId"
    const val NOTIFICATION_ID = "notificationId"

    /**
     * 알림 유형. 5종 모두에 항상 포함되므로 **알림 탭으로 열린 Intent인지 판별하는 마커**로도 쓴다.
     *
     * 마케팅 알림은 [FEED_ID]·[NOTIFICATION_ID]가 없어 이 키로만 탭을 감지할 수 있다.
     * 앱이 만든 마커를 쓰면 안 된다 — 백그라운드·종료 상태 탭은 FCM이 launch Intent를 만들기
     * 때문에 서버 data 페이로드 키만 실려 오고 앱이 심은 extra는 존재하지 않는다.
     */
    const val TYPE = "type"

    /** 서버가 [TYPE]을 주지 않았거나 알 수 없는 값일 때 로깅에 사용할 기본값. */
    const val UNKNOWN_TYPE = "UNKNOWN"
}
