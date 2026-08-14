package com.sseotdabwa.buyornot.core.analytics

/**
 * 첫 [identify] 전에 발행된 이벤트를 큐에 담아 두었다가 identify 직후 순서대로 흘려보낸다.
 *
 * userId는 DataStore에서 비동기로 읽어오므로 콜드 스타트 직후에는 아직 등록돼 있지 않다.
 * 그대로 흘려보내면 로그인 유저의 이벤트가 user_id=null(=비로그인)로 기록된다.
 * 특히 push_opened는 MainActivity.onCreate에서 발행돼 identify보다 항상 앞선다.
 */
class IdentityBufferingAnalytics(
    private val delegate: Analytics,
) : Analytics {
    private val pending = ArrayDeque<AnalyticsEvent>()
    private var identified = false

    override fun track(event: AnalyticsEvent) {
        synchronized(this) {
            if (!identified) {
                // identify가 끝내 오지 않는 비정상 상황에서 큐가 무한히 자라지 않도록 오래된 것부터 버린다.
                if (pending.size >= MAX_PENDING_EVENTS) pending.removeFirst()
                pending.addLast(event)
                return
            }
        }
        delegate.track(event)
    }

    override fun identify(userId: String?) {
        delegate.identify(userId)
        // flush를 락 안에서 끝낸다. 락을 먼저 풀면 그 사이 다른 스레드의 track()이 identified=true를 보고
        // 새 이벤트를 pending보다 먼저 흘려보낼 수 있다. delegate.track은 큐에 넣고 바로 반환하므로
        // 최대 MAX_PENDING_EVENTS개를 락 안에서 처리해도 부담이 없다.
        synchronized(this) {
            if (identified) return
            identified = true
            pending.forEach(delegate::track)
            pending.clear()
        }
    }

    private companion object {
        const val MAX_PENDING_EVENTS = 100
    }
}
