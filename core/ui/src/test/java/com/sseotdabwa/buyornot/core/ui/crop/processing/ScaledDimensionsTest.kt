package com.sseotdabwa.buyornot.core.ui.crop.processing

import org.junit.Assert.assertEquals
import org.junit.Test

class ScaledDimensionsTest {
    private val max = 1024

    @Test
    fun `너비_높이_모두_max_이하이면_변환_없이_그대로_반환된다`() {
        val r = computeScaledDimensions(800, 600, max)
        assertEquals(800, r.width)
        assertEquals(600, r.height)
    }

    @Test
    fun `너비_높이가_정확히_max이면_그대로_반환된다`() {
        val r = computeScaledDimensions(1024, 1024, max)
        assertEquals(1024, r.width)
        assertEquals(1024, r.height)
    }

    @Test
    fun `가로가_더_길면_가로를_max로_맞추고_세로는_비율을_유지한다`() {
        val r = computeScaledDimensions(2000, 1500, max)
        assertEquals(1024, r.width)
        assertEquals(768, r.height) // 1500 * 1024/2000 = 768
    }

    @Test
    fun `세로가_더_길면_세로를_max로_맞추고_가로는_비율을_유지한다`() {
        val r = computeScaledDimensions(1500, 2000, max)
        assertEquals(768, r.width) // 1500 * 1024/2000 = 768
        assertEquals(1024, r.height)
    }

    @Test
    fun `정사각형은_양변_모두_max로_맞춘다`() {
        val r = computeScaledDimensions(2000, 2000, max)
        assertEquals(1024, r.width)
        assertEquals(1024, r.height)
    }

    @Test
    fun `극단적으로_긴_가로_비율도_최소_1픽셀은_보장된다`() {
        val r = computeScaledDimensions(10000, 5, max)
        assertEquals(1024, r.width)
        // 5 * 1024/10000 = 0.512 → toInt = 0 → coerceAtLeast(1) = 1
        assertEquals(1, r.height)
    }
}
