package com.sseotdabwa.buyornot.core.common.deeplink

import org.junit.Assert.assertEquals
import org.junit.Test

private const val HOST = "buy-or-not.com"

class FeedShareTextTest {
    @Test
    fun `공유_URL은_앱_링크와_같은_형식이다`() {
        val url = feedShareUrlOf(HOST, 169L)

        assertEquals("https://buy-or-not.com/feed/169", url)
        // 만든 링크를 파서가 다시 읽을 수 있어야 한다 — 두 규칙이 어긋나면 공유 링크가 홈으로 떨어진다
        assertEquals(169L, feedIdFromAppLink(HOST, listOf("feed", "169"), HOST))
    }

    @Test
    fun `제목이_있으면_제목과_URL을_함께_싣는다`() {
        val text = feedShareTextOf(HOST, 169L, "부스터 포켓프라")

        assertEquals("'부스터 포켓프라' 살까 말까?\nhttps://buy-or-not.com/feed/169", text)
    }

    @Test
    fun `제목이_비어_있으면_URL만_싣는다`() {
        // 따옴표만 남은 텍스트가 공유되면 안 된다
        assertEquals("https://buy-or-not.com/feed/169", feedShareTextOf(HOST, 169L, ""))
    }

    @Test
    fun `제목이_공백뿐이면_URL만_싣는다`() {
        assertEquals("https://buy-or-not.com/feed/169", feedShareTextOf(HOST, 169L, "   "))
        assertEquals("https://buy-or-not.com/feed/169", feedShareTextOf(HOST, 169L, "\n\t "))
    }

    @Test
    fun `제목_앞뒤_공백은_다듬는다`() {
        val text = feedShareTextOf(HOST, 169L, "  부스터  ")

        assertEquals("'부스터' 살까 말까?\nhttps://buy-or-not.com/feed/169", text)
    }

    @Test
    fun `제목의_따옴표나_줄바꿈은_그대로_둔다`() {
        // 공유 텍스트는 평문이라 이스케이프할 대상이 없다. 임의로 손대면 원문이 훼손된다
        val text = feedShareTextOf(HOST, 169L, "이거 '진짜' 살까")

        assertEquals("'이거 '진짜' 살까' 살까 말까?\nhttps://buy-or-not.com/feed/169", text)
    }

    @Test
    fun `dev_host도_그대로_쓴다`() {
        assertEquals("https://dev.buy-or-not.com/feed/1", feedShareUrlOf("dev.buy-or-not.com", 1L))
    }
}
