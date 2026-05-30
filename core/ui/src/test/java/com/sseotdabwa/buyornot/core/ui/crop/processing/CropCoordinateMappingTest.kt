package com.sseotdabwa.buyornot.core.ui.crop.processing

import com.sseotdabwa.buyornot.core.ui.crop.state.NormalizedRect
import org.junit.Assert.assertEquals
import org.junit.Test

class CropCoordinateMappingTest {
    @Test
    fun `정규화_rect_0_0_1_1은_전체_비트맵_영역으로_매핑된다`() {
        val r = mapNormalizedToPixel(NormalizedRect.Full, bitmapWidth = 1000, bitmapHeight = 800)
        assertEquals(0, r.srcX)
        assertEquals(0, r.srcY)
        assertEquals(1000, r.srcW)
        assertEquals(800, r.srcH)
    }

    @Test
    fun `정규화_rect_0_0_0_5_0_5는_좌상단_사분면으로_매핑된다`() {
        val r =
            mapNormalizedToPixel(
                NormalizedRect(0f, 0f, 0.5f, 0.5f),
                bitmapWidth = 1000,
                bitmapHeight = 800,
            )
        assertEquals(0, r.srcX)
        assertEquals(0, r.srcY)
        assertEquals(500, r.srcW)
        assertEquals(400, r.srcH)
    }

    @Test
    fun `소수점_좌표는_정수로_반올림되며_비트맵_경계를_벗어나지_않는다`() {
        val r =
            mapNormalizedToPixel(
                NormalizedRect(0.999f, 0.999f, 1f, 1f),
                bitmapWidth = 100,
                bitmapHeight = 100,
            )
        // srcX는 99 또는 100 근처. srcX + srcW <= 100 보장
        assert(r.srcX + r.srcW <= 100)
        assert(r.srcY + r.srcH <= 100)
        assert(r.srcW >= 1)
        assert(r.srcH >= 1)
    }

    @Test
    fun `0_길이_rect_요청은_최소_1픽셀로_보정된다`() {
        val r =
            mapNormalizedToPixel(
                NormalizedRect(0.5f, 0.5f, 0.5f, 0.5f),
                bitmapWidth = 100,
                bitmapHeight = 100,
            )
        assertEquals(1, r.srcW)
        assertEquals(1, r.srcH)
    }
}
