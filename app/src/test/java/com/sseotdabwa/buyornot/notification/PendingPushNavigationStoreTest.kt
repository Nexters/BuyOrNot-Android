package com.sseotdabwa.buyornot.notification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PendingPushNavigationStoreTest {
    private val store = PendingPushNavigationStore()

    @Test
    fun `set한_목적지를_그대로_들고_있는다`() {
        store.set(PendingPushNavigation(PushDestination.FEED_CREATE))

        assertEquals(PushDestination.FEED_CREATE, store.pending.value?.destination)
    }

    @Test
    fun `여러_번_읽어도_소비되지_않는다`() {
        // Activity 재생성 시 새 Composition이 같은 값을 다시 읽어야 이동이 살아남는다.
        // 읽기만으로 지워지면 재생성 구간에서 목적지가 사라진다.
        store.set(PendingPushNavigation(PushDestination.HOME))

        assertEquals(PushDestination.HOME, store.pending.value?.destination)
        assertEquals(PushDestination.HOME, store.pending.value?.destination)
        assertEquals(PushDestination.HOME, store.pending.value?.destination)
    }

    @Test
    fun `consume하면_비워진다`() {
        store.set(PendingPushNavigation(PushDestination.HOME))

        store.consume()

        assertNull(store.pending.value)
    }

    @Test
    fun `consume은_여러_번_불려도_안전하다`() {
        store.consume()
        store.consume()

        assertNull(store.pending.value)
    }

    @Test
    fun `나중에_들어온_알림이_이전_목적지를_덮어쓴다`() {
        // 인증 대기 중 알림을 연달아 탭하면 마지막 것으로 가는 게 사용자 기대에 맞다.
        store.set(PendingPushNavigation(PushDestination.HOME))

        store.set(PendingPushNavigation(PushDestination.FEED_DETAIL, feedId = 169L, notificationId = 42L))

        assertEquals(PushDestination.FEED_DETAIL, store.pending.value?.destination)
        assertEquals(169L, store.pending.value?.feedId)
        assertEquals(42L, store.pending.value?.notificationId)
    }

    @Test
    fun `마케팅_알림은_feedId와_notificationId가_없다`() {
        store.set(PendingPushNavigation(PushDestination.FEED_CREATE))

        assertNull(store.pending.value?.feedId)
        assertNull(store.pending.value?.notificationId)
    }
}
