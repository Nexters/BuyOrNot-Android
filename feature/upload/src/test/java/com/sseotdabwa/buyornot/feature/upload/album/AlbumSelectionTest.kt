package com.sseotdabwa.buyornot.feature.upload.album

import android.net.Uri
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNull

// android.net.Uri가 필요해 Robolectric으로 돌리고, 로컬 JDK 17에서도 뜨는 SDK로 고정한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AlbumSelectionTest {
    private val a = Uri.parse("content://media/1")
    private val b = Uri.parse("content://media/2")
    private val c = Uri.parse("content://media/3")

    @Test
    fun `고른_순서대로_뒤에_붙인다`() {
        assertEquals(listOf(b, a), listOf(b).toggled(a, maxCount = 3))
    }

    @Test
    fun `이미_고른_사진을_다시_누르면_빼고_나머지_순서는_유지한다`() {
        assertEquals(listOf(a, c), listOf(a, b, c).toggled(b, maxCount = 3))
    }

    @Test
    fun `최대_개수를_넘겨_고르면_null을_돌려준다`() {
        assertNull(listOf(a, b).toggled(c, maxCount = 2))
    }
}
