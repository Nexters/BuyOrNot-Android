package com.sseotdabwa.buyornot.core.ui.crop.processing

import com.sseotdabwa.buyornot.core.ui.crop.state.NormalizedRect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
    fun `소수점_좌표는_정수로_버림되며_비트맵_경계를_벗어나지_않는다`() {
        val r =
            mapNormalizedToPixel(
                NormalizedRect(0.999f, 0.999f, 1f, 1f),
                bitmapWidth = 100,
                bitmapHeight = 100,
            )
        // srcX는 99 또는 100 근처. srcX + srcW <= 100 보장
        assertTrue(r.srcX + r.srcW <= 100)
        assertTrue(r.srcY + r.srcH <= 100)
        assertTrue(r.srcW >= 1)
        assertTrue(r.srcH >= 1)
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

    @Test
    fun `squared는_짧은_변에_맞춰_가운데를_남긴_정사각형을_만든다`() {
        val r = PixelRect(srcX = 10, srcY = 20, srcW = 301, srcH = 300).squared()
        assertEquals(PixelRect(srcX = 10, srcY = 20, srcW = 300, srcH = 300), r)

        val tall = PixelRect(srcX = 0, srcY = 0, srcW = 100, srcH = 110).squared()
        assertEquals(PixelRect(srcX = 0, srcY = 5, srcW = 100, srcH = 100), tall)
    }
}
