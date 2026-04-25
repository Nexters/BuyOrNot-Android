package com.sseotdabwa.buyornot.core.ui.imageviewer

import androidx.compose.ui.geometry.Offset
import org.junit.Assert.assertEquals
import org.junit.Test

class FocalOffsetTest {
    private fun assertOffsetEquals(
        expected: Offset,
        actual: Offset,
        delta: Float = 0.01f,
    ) {
        assertEquals(expected.x, actual.x, delta)
        assertEquals(expected.y, actual.y, delta)
    }

    // centroid가 컨테이너 중앙(200,200)이면 중앙 기준 zoom → offset 변화 없음 (pan=Zero)
    @Test
    fun `centroid at center zoom 2x returns zero offset`() {
        val result =
            computeFocalOffset(
                currentOffset = Offset.Zero,
                centroid = Offset(200f, 200f),
                containerWidth = 400,
                containerHeight = 400,
                currentScale = 1f,
                newScale = 2f,
                pan = Offset.Zero,
            )
        assertOffsetEquals(Offset.Zero, result)
    }

    // centroid가 좌상단(0,0)이면 좌상단 고정으로 확대 → offset이 우하단으로 이동
    // c = (-200, -200), actualZoom=2
    // result = (-200,-200)*(1-2) + (0,0)*2 + (0,0) = (200,200)
    @Test
    fun `centroid at top-left zoom 2x shifts offset to bottom-right`() {
        val result =
            computeFocalOffset(
                currentOffset = Offset.Zero,
                centroid = Offset(0f, 0f),
                containerWidth = 400,
                containerHeight = 400,
                currentScale = 1f,
                newScale = 2f,
                pan = Offset.Zero,
            )
        assertOffsetEquals(Offset(200f, 200f), result)
    }

    // newScale = 1f이면 반드시 Offset.Zero 반환
    @Test
    fun `newScale 1f returns zero regardless of other params`() {
        val result =
            computeFocalOffset(
                currentOffset = Offset(100f, 50f),
                centroid = Offset(50f, 50f),
                containerWidth = 400,
                containerHeight = 400,
                currentScale = 2f,
                newScale = 1f,
                pan = Offset(10f, 10f),
            )
        assertOffsetEquals(Offset.Zero, result)
    }

    // centroid=중앙, zoom=1, pan=(10,5) → offset이 pan만큼 이동
    @Test
    fun `pan offset applied correctly`() {
        val result =
            computeFocalOffset(
                currentOffset = Offset.Zero,
                centroid = Offset(200f, 200f),
                containerWidth = 400,
                containerHeight = 400,
                currentScale = 2f,
                newScale = 2f,
                pan = Offset(10f, 5f),
            )
        assertOffsetEquals(Offset(10f, 5f), result)
    }

    // 이미 offset이 있는 상태에서 centroid=중앙으로 zoom → offset이 actualZoom 배로 증폭
    // c = Zero, actualZoom=2, currentOffset=(50,30)
    // result = Zero*(1-2) + (50,30)*2 + Zero = (100,60)
    @Test
    fun `existing offset amplified by zoom when centroid at center`() {
        val result =
            computeFocalOffset(
                currentOffset = Offset(50f, 30f),
                centroid = Offset(200f, 200f),
                containerWidth = 400,
                containerHeight = 400,
                currentScale = 1f,
                newScale = 2f,
                pan = Offset.Zero,
            )
        assertOffsetEquals(Offset(100f, 60f), result)
    }
}
