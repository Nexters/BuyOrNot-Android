package com.sseotdabwa.buyornot.core.ui.crop.geometry

import com.sseotdabwa.buyornot.core.ui.crop.state.NormalizedRect
import org.junit.Assert.assertEquals
import org.junit.Test

class CropGeometryTest {
    private val bounds = NormalizedRect.Full

    // clampTo
    @Test
    fun `clampTo는_rect를_bounds_안으로_이동시킨다`() {
        val rect = NormalizedRect(-0.2f, -0.1f, 0.3f, 0.4f)
        val clamped = rect.clampTo(bounds)
        assertEquals(0f, clamped.left, 0.0001f)
        assertEquals(0f, clamped.top, 0.0001f)
        assertEquals(0.5f, clamped.right, 0.0001f)
        assertEquals(0.5f, clamped.bottom, 0.0001f)
    }

    @Test
    fun `clampTo는_rect_크기가_bounds보다_크면_bounds_크기로_축소한다`() {
        val rect = NormalizedRect(-0.5f, -0.5f, 1.5f, 1.5f)
        val clamped = rect.clampTo(bounds)
        assertEquals(0f, clamped.left, 0.0001f)
        assertEquals(1f, clamped.right, 0.0001f)
        assertEquals(0f, clamped.top, 0.0001f)
        assertEquals(1f, clamped.bottom, 0.0001f)
    }

    // computeRectForRatio
    @Test
    fun `자유형으로_변환하면_현재_rect를_bounds로_clamp만_한다`() {
        val current = NormalizedRect(0.2f, 0.2f, 0.6f, 0.5f)
        val result = computeRectForRatio(current, bounds, targetRatio = null)
        assertEquals(current, result)
    }

    @Test
    fun `1대1로_변환하면_정사각형이_되고_bounds에_꽉_찬다`() {
        val current = NormalizedRect(0.2f, 0.3f, 0.8f, 0.5f) // cx=0.5, cy=0.4
        val result = computeRectForRatio(current, bounds, targetRatio = 1f)
        val w = result.right - result.left
        val h = result.bottom - result.top
        assertEquals(w, h, 0.0001f)
        assertEquals(0.5f, (result.left + result.right) / 2f, 0.0001f)
        // 1×1 square fills the full bounds; the original cy=0.4 gets shifted to 0.5
        assertEquals(0.5f, (result.top + result.bottom) / 2f, 0.0001f)
    }

    @Test
    fun `3대4로_변환하면_세로가_더_긴_직사각형이_된다`() {
        val current = NormalizedRect(0.3f, 0.3f, 0.7f, 0.7f)
        val result = computeRectForRatio(current, bounds, targetRatio = 0.75f)
        val w = result.right - result.left
        val h = result.bottom - result.top
        assertEquals(0.75f, w / h, 0.0001f)
    }

    @Test
    fun `4대3으로_변환하면_가로가_더_긴_직사각형이_된다`() {
        val current = NormalizedRect(0.3f, 0.3f, 0.7f, 0.7f)
        val result = computeRectForRatio(current, bounds, targetRatio = 4f / 3f)
        val w = result.right - result.left
        val h = result.bottom - result.top
        assertEquals(4f / 3f, w / h, 0.0001f)
    }

    @Test
    fun `비율_변환_결과가_bounds_왼쪽_밖이면_오른쪽으로_shift된다`() {
        val current = NormalizedRect(-0.2f, 0.4f, 0.2f, 0.6f) // 중심 x=0
        val result = computeRectForRatio(current, bounds, targetRatio = 1f)
        assertEquals(0f, result.left, 0.0001f)
        assertEquals(result.right - result.left, result.bottom - result.top, 0.0001f)
    }

    @Test
    fun `비율_변환_결과가_bounds_위쪽_밖이면_아래로_shift된다`() {
        val current = NormalizedRect(0.4f, -0.2f, 0.6f, 0.2f) // 중심 y=0
        val result = computeRectForRatio(current, bounds, targetRatio = 1f)
        assertEquals(0f, result.top, 0.0001f)
    }

    @Test
    fun `bounds보다_큰_비율_요청시_가능한_최대_크기로_제한된다`() {
        // bounds 폭=1, 높이=1, target 4:3 → max w=1, h=0.75
        val current = NormalizedRect(0.2f, 0.2f, 0.8f, 0.8f)
        val result = computeRectForRatio(current, bounds, targetRatio = 4f / 3f)
        assertEquals(1f, result.right - result.left, 0.0001f)
        assertEquals(0.75f, result.bottom - result.top, 0.0001f)
    }

    // resizeFrom — 자유비율
    @Test
    fun `자유비율_resizeFrom_BR은_드래그_방향대로_핸들을_이동시킨다`() {
        val rect = NormalizedRect(0.2f, 0.2f, 0.6f, 0.5f)
        val result =
            rect.resizeFrom(
                corner = HandleZone.BR,
                deltaX = 0.1f,
                deltaY = 0.2f,
                minSize = 0.05f,
                targetRatio = null,
            )
        assertEquals(0.2f, result.left, 0.0001f)
        assertEquals(0.2f, result.top, 0.0001f)
        assertEquals(0.7f, result.right, 0.0001f)
        assertEquals(0.7f, result.bottom, 0.0001f)
    }

    @Test
    fun `1대1_resizeFrom_BR은_정사각형_비율을_유지한다`() {
        val rect = NormalizedRect(0.2f, 0.2f, 0.4f, 0.4f) // 0.2 x 0.2
        val result =
            rect.resizeFrom(
                corner = HandleZone.BR,
                deltaX = 0.1f,
                deltaY = 0.0f,
                minSize = 0.05f,
                targetRatio = 1f,
            )
        val w = result.right - result.left
        val h = result.bottom - result.top
        assertEquals(w, h, 0.0001f)
        assertEquals(0.3f, w, 0.0001f) // 더 큰 쪽 (deltaX=0.1) 채택
    }

    @Test
    fun `resizeFrom은_minSize_이하로는_줄어들지_않는다`() {
        val rect = NormalizedRect(0.4f, 0.4f, 0.5f, 0.5f) // 0.1
        val result =
            rect.resizeFrom(
                corner = HandleZone.BR,
                deltaX = -0.5f,
                deltaY = -0.5f,
                minSize = 0.05f,
                targetRatio = null,
            )
        assertEquals(0.05f, result.right - result.left, 0.0001f)
        assertEquals(0.05f, result.bottom - result.top, 0.0001f)
    }

    @Test
    fun `1대1_resizeFrom_TL은_정사각형_비율을_유지하며_BR을_고정한다`() {
        val rect = NormalizedRect(0.3f, 0.3f, 0.5f, 0.5f) // 0.2 x 0.2
        val result =
            rect.resizeFrom(
                corner = HandleZone.TL,
                deltaX = -0.1f,
                deltaY = 0.0f,
                minSize = 0.05f,
                targetRatio = 1f,
            )
        val w = result.right - result.left
        val h = result.bottom - result.top
        assertEquals(w, h, 0.0001f)
        assertEquals(0.3f, w, 0.0001f) // signedDelta = -(-0.1) = 0.1, width 0.2 + 0.1 = 0.3
        assertEquals(0.5f, result.right, 0.0001f) // BR x fixed
        assertEquals(0.5f, result.bottom, 0.0001f) // BR y fixed
    }

    @Test
    fun `1대1_resizeFrom은_fixed_ratio에서도_minSize_이하로는_줄어들지_않는다`() {
        val rect = NormalizedRect(0.4f, 0.4f, 0.5f, 0.5f) // 0.1
        val result =
            rect.resizeFrom(
                corner = HandleZone.BR,
                deltaX = -0.5f,
                deltaY = -0.5f,
                minSize = 0.05f,
                targetRatio = 1f,
            )
        assertEquals(0.05f, result.right - result.left, 0.0001f)
        assertEquals(0.05f, result.bottom - result.top, 0.0001f)
    }

    // detectHandle
    @Test
    fun `detectHandle은_터치_반경_안의_TL_코너를_반환한다`() {
        val rect = NormalizedRect(0.3f, 0.3f, 0.7f, 0.7f)
        val zone =
            detectHandle(
                posX = 0.31f,
                posY = 0.31f,
                cropRect = rect,
                touchRadius = 0.05f,
            )
        assertEquals(HandleZone.TL, zone)
    }

    @Test
    fun `detectHandle은_cropRect_내부_터치를_BODY로_반환한다`() {
        val rect = NormalizedRect(0.3f, 0.3f, 0.7f, 0.7f)
        val zone =
            detectHandle(
                posX = 0.5f,
                posY = 0.5f,
                cropRect = rect,
                touchRadius = 0.05f,
            )
        assertEquals(HandleZone.BODY, zone)
    }

    @Test
    fun `detectHandle은_cropRect_바깥_터치를_NONE으로_반환한다`() {
        val rect = NormalizedRect(0.3f, 0.3f, 0.7f, 0.7f)
        val zone =
            detectHandle(
                posX = 0.1f,
                posY = 0.1f,
                cropRect = rect,
                touchRadius = 0.05f,
            )
        assertEquals(HandleZone.NONE, zone)
    }

    // rotateCounterClockwise
    @Test
    fun `rotateCCW_0회는_변환하지_않는다`() {
        val rect = NormalizedRect(0.1f, 0.2f, 0.6f, 0.8f)
        val result = rect.rotateCounterClockwise(0)
        assertEquals(rect, result)
    }

    @Test
    fun `rotateCCW_1회는_좌표를_반시계_90도로_매핑한다`() {
        val rect = NormalizedRect(0.1f, 0.2f, 0.6f, 0.8f)
        val result = rect.rotateCounterClockwise(1)
        // (l, t, r, b) -> (t, 1-r, b, 1-l) = (0.2, 0.4, 0.8, 0.9)
        assertEquals(0.2f, result.left, 0.0001f)
        assertEquals(0.4f, result.top, 0.0001f)
        assertEquals(0.8f, result.right, 0.0001f)
        assertEquals(0.9f, result.bottom, 0.0001f)
    }

    @Test
    fun `rotateCCW_2회는_점대칭으로_변환한다`() {
        val rect = NormalizedRect(0.1f, 0.2f, 0.6f, 0.8f)
        val result = rect.rotateCounterClockwise(2)
        // 2회 적용: (l, t, r, b) -> (1-r, 1-b, 1-l, 1-t) = (0.4, 0.2, 0.9, 0.8)
        assertEquals(0.4f, result.left, 0.0001f)
        assertEquals(0.2f, result.top, 0.0001f)
        assertEquals(0.9f, result.right, 0.0001f)
        assertEquals(0.8f, result.bottom, 0.0001f)
    }

    @Test
    fun `rotateCCW_4회는_원래_좌표로_돌아온다`() {
        val rect = NormalizedRect(0.1f, 0.2f, 0.6f, 0.8f)
        val result = rect.rotateCounterClockwise(4)
        assertEquals(rect.left, result.left, 0.0001f)
        assertEquals(rect.top, result.top, 0.0001f)
        assertEquals(rect.right, result.right, 0.0001f)
        assertEquals(rect.bottom, result.bottom, 0.0001f)
    }

    @Test
    fun `rotateCCW_음수_quarters는_mod_4로_정규화된다`() {
        val rect = NormalizedRect(0.1f, 0.2f, 0.6f, 0.8f)
        // -1 회 = +3 회와 동일
        val resultNeg = rect.rotateCounterClockwise(-1)
        val resultPos = rect.rotateCounterClockwise(3)
        assertEquals(resultPos.left, resultNeg.left, 0.0001f)
        assertEquals(resultPos.top, resultNeg.top, 0.0001f)
        assertEquals(resultPos.right, resultNeg.right, 0.0001f)
        assertEquals(resultPos.bottom, resultNeg.bottom, 0.0001f)
    }
}
