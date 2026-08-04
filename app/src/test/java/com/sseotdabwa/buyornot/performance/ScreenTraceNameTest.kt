package com.sseotdabwa.buyornot.performance

import com.sseotdabwa.buyornot.core.analytics.performance.TraceNames
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ScreenTraceNameTest {
    @Test
    fun `패키지 경로를 벗기고 Route 접미사를 제거한다`() {
        assertEquals(
            "home",
            screenTraceNameOf("com.sseotdabwa.buyornot.feature.home.navigation.HomeRoute"),
        )
    }

    @Test
    fun `여러 단어는 snake_case로 바꾼다`() {
        assertEquals(
            "my_page",
            screenTraceNameOf("com.sseotdabwa.buyornot.feature.mypage.navigation.MyPageRoute"),
        )
    }

    @Test
    fun `경로 인자를 제거해 이름이 인자값마다 갈라지지 않게 한다`() {
        val name = "com.sseotdabwa.buyornot.feature.notification.navigation.FeedDetailRoute"
        assertEquals("feed_detail", screenTraceNameOf("$name/{feedId}"))
        assertEquals("feed_detail", screenTraceNameOf("$name/123"))
    }

    @Test
    fun `쿼리 인자도 제거한다`() {
        assertEquals(
            "home",
            screenTraceNameOf("com.sseotdabwa.buyornot.feature.home.navigation.HomeRoute?initialTab={initialTab}"),
        )
    }

    @Test
    fun `Route 접미사가 없어도 처리한다`() {
        assertEquals("image_viewer", screenTraceNameOf("com.sseotdabwa.buyornot.core.ui.imageviewer.ImageViewer"))
    }

    @Test
    fun `이름을 만들 수 없으면 null을 반환한다`() {
        assertNull(screenTraceNameOf(null))
        assertNull(screenTraceNameOf(""))
        assertNull(screenTraceNameOf("   "))
        assertNull(screenTraceNameOf("Route"))
    }

    @Test
    fun `Trace 이름 상한을 넘지 않도록 길이를 자른다`() {
        val absurdlyLong = "com.example." + "A".repeat(300) + "Route"
        val name = requireNotNull(screenTraceNameOf(absurdlyLong))

        // Firebase Trace 이름 상한은 100자다. 접두사(`screen_render_`)를 더해도 남아야 한다.
        assertTrue("length=${name.length}", name.length <= 60)
        assertTrue(TraceNames.screenRender(name).length <= 100)
    }

    @Test
    fun `구분자로 시작하거나 끝나지 않는다`() {
        val name = requireNotNull(screenTraceNameOf("com.example.UploadRoute"))

        assertFalse(name.startsWith("_"))
        assertFalse(name.endsWith("_"))
    }
}
