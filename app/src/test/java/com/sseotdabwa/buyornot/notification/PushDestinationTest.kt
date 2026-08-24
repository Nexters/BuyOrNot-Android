package com.sseotdabwa.buyornot.notification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PushDestinationTest {
    @Test
    fun `투표_알림은_피드_상세로_보낸다`() {
        assertEquals(PushDestination.FEED_DETAIL, pushDestinationOf("FEED_DETAIL", 169L))
    }

    @Test
    fun `마케팅_투표_미등록은_홈으로_보낸다`() {
        assertEquals(PushDestination.HOME, pushDestinationOf("HOME", null))
    }

    @Test
    fun `재참여_온보딩_마케팅은_투표_등록_화면으로_보낸다`() {
        assertEquals(PushDestination.FEED_CREATE, pushDestinationOf("FEED_CREATE", null))
    }

    @Test
    fun `feedId가_있으면_screen보다_피드_상세가_이긴다`() {
        // 서버가 모순된 payload를 보내도 가장 구체적인 목적지로 간다
        assertEquals(PushDestination.FEED_DETAIL, pushDestinationOf("HOME", 169L))
        assertEquals(PushDestination.FEED_DETAIL, pushDestinationOf("FEED_CREATE", 169L))
        assertEquals(PushDestination.FEED_DETAIL, pushDestinationOf(null, 169L))
    }

    @Test
    fun `feedId가_없는_FEED_DETAIL은_이동하지_않는다`() {
        assertNull(pushDestinationOf("FEED_DETAIL", null))
    }

    @Test
    fun `screen이_없으면_이동하지_않는다`() {
        assertNull(pushDestinationOf(null, null))
    }

    @Test
    fun `알_수_없는_screen은_이동하지_않는다`() {
        // 서버가 신규 화면을 추가해도 구버전 앱은 홈으로 열리기만 해야 한다
        assertNull(pushDestinationOf("NOTIFICATIONS", null))
        assertNull(pushDestinationOf("MY_PAGE", null))
        assertNull(pushDestinationOf("", null))
    }

    @Test
    fun `screen은_대소문자를_구분한다`() {
        // 서버 계약은 대문자 SCREAMING_SNAKE_CASE다. 관용적으로 받아주면 계약 위반을 못 잡는다
        assertNull(pushDestinationOf("home", null))
        assertNull(pushDestinationOf("Feed_Create", null))
    }
}
