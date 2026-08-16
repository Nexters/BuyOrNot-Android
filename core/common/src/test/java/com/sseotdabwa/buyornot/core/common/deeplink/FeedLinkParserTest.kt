package com.sseotdabwa.buyornot.core.common.deeplink

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

private const val HOST = "buy-or-not.com"

class FeedLinkParserTest {
    @Test
    fun `feed_경로에서_feedId를_뽑는다`() {
        assertEquals(123L, feedIdFromAppLink(HOST, listOf("feed", "123"), HOST))
    }

    @Test
    fun `쿼리_파라미터는_path에_영향을_주지_않는다`() {
        // ?utm_source=kakao 가 붙어도 pathSegments는 그대로다
        assertEquals(123L, feedIdFromAppLink(HOST, listOf("feed", "123"), HOST))
    }

    @Test
    fun `feedId_뒤에_세그먼트가_더_붙어도_뽑는다`() {
        assertEquals(123L, feedIdFromAppLink(HOST, listOf("feed", "123", "share"), HOST))
    }

    @Test
    fun `host가_다르면_null을_반환한다`() {
        assertNull(feedIdFromAppLink("dev.buy-or-not.com", listOf("feed", "123"), HOST))
    }

    @Test
    fun `www가_붙은_host는_다른_도메인으로_취급한다`() {
        assertNull(feedIdFromAppLink("www.buy-or-not.com", listOf("feed", "123"), HOST))
    }

    @Test
    fun `host가_없으면_null을_반환한다`() {
        assertNull(feedIdFromAppLink(null, listOf("feed", "123"), HOST))
    }

    @Test
    fun `feed가_아닌_경로는_null을_반환한다`() {
        assertNull(feedIdFromAppLink(HOST, listOf("terms"), HOST))
        assertNull(feedIdFromAppLink(HOST, listOf("notice", "123"), HOST))
    }

    @Test
    fun `세그먼트가_부족하면_null을_반환한다`() {
        assertNull(feedIdFromAppLink(HOST, listOf("feed"), HOST))
        assertNull(feedIdFromAppLink(HOST, emptyList(), HOST))
    }

    @Test
    fun `숫자가_아닌_feedId는_null을_반환한다`() {
        assertNull(feedIdFromAppLink(HOST, listOf("feed", "abc"), HOST))
    }

    @Test
    fun `Long_범위를_넘는_feedId는_null을_반환한다`() {
        assertNull(feedIdFromAppLink(HOST, listOf("feed", "99999999999999999999"), HOST))
    }

    @Test
    fun `0과_음수_feedId는_null을_반환한다`() {
        // NotificationDetailViewModel이 feedId를 checkNotNull 하고 서버도 양수만 안다
        assertNull(feedIdFromAppLink(HOST, listOf("feed", "0"), HOST))
        assertNull(feedIdFromAppLink(HOST, listOf("feed", "-1"), HOST))
    }
}
