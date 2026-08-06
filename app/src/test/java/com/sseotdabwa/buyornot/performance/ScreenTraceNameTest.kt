package com.sseotdabwa.buyornot.performance

import com.sseotdabwa.buyornot.core.analytics.performance.TraceNames
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ScreenTraceNameTest {
    @Test
    fun `패키지_경로를_벗기고_Route_접미사를_제거한다`() {
        assertEquals(
            "home",
            screenTraceNameOf("com.sseotdabwa.buyornot.feature.home.navigation.HomeRoute"),
        )
    }

    @Test
    fun `여러_단어는_snake_case로_바꾼다`() {
        assertEquals(
            "my_page",
            screenTraceNameOf("com.sseotdabwa.buyornot.feature.mypage.navigation.MyPageRoute"),
        )
    }

    @Test
    fun `경로_인자를_제거해_이름이_인자값마다_갈라지지_않게_한다`() {
        val name = "com.sseotdabwa.buyornot.feature.notification.navigation.FeedDetailRoute"
        assertEquals("feed_detail", screenTraceNameOf("$name/{feedId}"))
        assertEquals("feed_detail", screenTraceNameOf("$name/123"))
    }

    @Test
    fun `쿼리_인자도_제거한다`() {
        assertEquals(
            "home",
            screenTraceNameOf("com.sseotdabwa.buyornot.feature.home.navigation.HomeRoute?initialTab={initialTab}"),
        )
    }

    @Test
    fun `Route_접미사가_없어도_처리한다`() {
        assertEquals("image_viewer", screenTraceNameOf("com.sseotdabwa.buyornot.core.ui.imageviewer.ImageViewer"))
    }

    @Test
    fun `이름을_만들_수_없으면_null을_반환한다`() {
        assertNull(screenTraceNameOf(null))
        assertNull(screenTraceNameOf(""))
        assertNull(screenTraceNameOf("   "))
        assertNull(screenTraceNameOf("Route"))
    }

    @Test
    fun `Trace_이름_상한을_넘지_않도록_길이를_자른다`() {
        val absurdlyLong = "com.example." + "A".repeat(300) + "Route"
        val name = requireNotNull(screenTraceNameOf(absurdlyLong))

        // Firebase Trace 이름 상한은 100자다. 접두사(`screen_render_`)를 더해도 남아야 한다.
        assertTrue("length=${name.length}", name.length <= 60)
        assertTrue(TraceNames.screenRender(name).length <= 100)
    }

    @Test
    fun `구분자로_시작하거나_끝나지_않는다`() {
        val name = requireNotNull(screenTraceNameOf("com.example.UploadRoute"))

        assertFalse(name.startsWith("_"))
        assertFalse(name.endsWith("_"))
    }
}
