package com.sseotdabwa.buyornot.core.ui.crop

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import org.junit.Assert.assertEquals
import org.junit.Test

class CropGeometryTest {
    // ─── detectHandle ───────────────────────────────────────────────

    @Test
    fun `detectHandle returns TL when touch is within radius of TL corner`() {
        val cropRect = Rect(100f, 100f, 300f, 300f)
        val result = detectHandle(Offset(110f, 110f), cropRect, touchRadius = 50f)
        assertEquals(HandleZone.TL, result)
    }

    @Test
    fun `detectHandle returns TR when touch is within radius of TR corner`() {
        val cropRect = Rect(100f, 100f, 300f, 300f)
        val result = detectHandle(Offset(290f, 110f), cropRect, touchRadius = 50f)
        assertEquals(HandleZone.TR, result)
    }

    @Test
    fun `detectHandle returns BL when touch is within radius of BL corner`() {
        val cropRect = Rect(100f, 100f, 300f, 300f)
        val result = detectHandle(Offset(110f, 290f), cropRect, touchRadius = 50f)
        assertEquals(HandleZone.BL, result)
    }

    @Test
    fun `detectHandle returns BR when touch is within radius of BR corner`() {
        val cropRect = Rect(100f, 100f, 300f, 300f)
        val result = detectHandle(Offset(290f, 290f), cropRect, touchRadius = 50f)
        assertEquals(HandleZone.BR, result)
    }

    @Test
    fun `detectHandle returns BODY when touch is inside crop box and not near corner`() {
        val cropRect = Rect(100f, 100f, 300f, 300f)
        val result = detectHandle(Offset(200f, 200f), cropRect, touchRadius = 30f)
        assertEquals(HandleZone.BODY, result)
    }

    @Test
    fun `detectHandle returns NONE when touch is outside crop box`() {
        val cropRect = Rect(100f, 100f, 300f, 300f)
        val result = detectHandle(Offset(50f, 50f), cropRect, touchRadius = 30f)
        assertEquals(HandleZone.NONE, result)
    }

    @Test
    fun `detectHandle corner takes priority over BODY`() {
        // Position is both inside and near a corner (small crop box)
        val cropRect = Rect(100f, 100f, 140f, 140f)
        val result = detectHandle(Offset(105f, 105f), cropRect, touchRadius = 30f)
        assertEquals(HandleZone.TL, result)
    }

    // ─── resizeFrom ─────────────────────────────────────────────────

    @Test
    fun `resizeFrom BR with positive delta increases size keeping TL fixed`() {
        val rect = Rect(100f, 100f, 300f, 300f) // 200x200
        val result = rect.resizeFrom(HandleZone.BR, Offset(20f, 0f), minSize = 50f)
        assertEquals(100f, result.left, 0.01f)
        assertEquals(100f, result.top, 0.01f)
        assertEquals(220f, result.width, 0.01f)
        assertEquals(220f, result.height, 0.01f)
    }

    @Test
    fun `resizeFrom TL with negative delta increases size keeping BR fixed`() {
        val rect = Rect(100f, 100f, 300f, 300f) // 200x200
        val result = rect.resizeFrom(HandleZone.TL, Offset(-20f, 0f), minSize = 50f)
        assertEquals(300f, result.right, 0.01f)
        assertEquals(300f, result.bottom, 0.01f)
        assertEquals(220f, result.width, 0.01f)
        assertEquals(220f, result.height, 0.01f)
    }

    @Test
    fun `resizeFrom TR with positive x delta increases size keeping BL fixed`() {
        val rect = Rect(100f, 100f, 300f, 300f)
        val result = rect.resizeFrom(HandleZone.TR, Offset(20f, 0f), minSize = 50f)
        assertEquals(100f, result.left, 0.01f)
        assertEquals(300f, result.bottom, 0.01f)
        assertEquals(220f, result.width, 0.01f)
    }

    @Test
    fun `resizeFrom BL with negative x delta increases size keeping TR fixed`() {
        val rect = Rect(100f, 100f, 300f, 300f)
        val result = rect.resizeFrom(HandleZone.BL, Offset(-20f, 0f), minSize = 50f)
        assertEquals(300f, result.right, 0.01f)
        assertEquals(100f, result.top, 0.01f)
        assertEquals(220f, result.width, 0.01f)
    }

    @Test
    fun `resizeFrom does not go below minSize`() {
        val rect = Rect(100f, 100f, 300f, 300f) // 200x200
        val result = rect.resizeFrom(HandleZone.BR, Offset(-500f, 0f), minSize = 96f)
        assertEquals(96f, result.width, 0.01f)
        assertEquals(96f, result.height, 0.01f)
    }

    @Test
    fun `resizeFrom uses larger abs axis for primary delta`() {
        val rect = Rect(100f, 100f, 300f, 300f)
        // delta.x=30, delta.y=10 → use x axis (larger abs)
        val result = rect.resizeFrom(HandleZone.BR, Offset(30f, 10f), minSize = 50f)
        assertEquals(230f, result.width, 0.01f) // 200 + 30
    }

    @Test
    fun `resizeFrom result is always 1to1 aspect ratio`() {
        val rect = Rect(100f, 100f, 300f, 300f)
        val result = rect.resizeFrom(HandleZone.BR, Offset(37f, 13f), minSize = 50f)
        assertEquals(result.width, result.height, 0.01f)
    }

    // ─── clampTo ────────────────────────────────────────────────────

    @Test
    fun `clampTo keeps rect within bounds when rect is fully inside`() {
        val bounds = Rect(0f, 0f, 400f, 400f)
        val rect = Rect(50f, 50f, 250f, 250f)
        val result = rect.clampTo(bounds)
        assertEquals(rect.left, result.left, 0.01f)
        assertEquals(rect.top, result.top, 0.01f)
        assertEquals(rect.width, result.width, 0.01f)
    }

    @Test
    fun `clampTo clamps left overflow`() {
        val bounds = Rect(0f, 0f, 400f, 400f)
        val rect = Rect(-10f, 50f, 190f, 250f) // left overflows
        val result = rect.clampTo(bounds)
        assertEquals(0f, result.left, 0.01f)
        assertEquals(200f, result.width, 0.01f)
    }

    @Test
    fun `clampTo clamps right overflow`() {
        val bounds = Rect(0f, 0f, 400f, 400f)
        val rect = Rect(250f, 50f, 450f, 250f) // right overflows by 50
        val result = rect.clampTo(bounds)
        assertEquals(400f, result.right, 0.01f)
        assertEquals(200f, result.width, 0.01f)
    }

    @Test
    fun `clampTo clamps top overflow`() {
        val bounds = Rect(0f, 0f, 400f, 400f)
        val rect = Rect(50f, -20f, 250f, 180f)
        val result = rect.clampTo(bounds)
        assertEquals(0f, result.top, 0.01f)
    }

    @Test
    fun `clampTo result is always 1to1 aspect ratio`() {
        val bounds = Rect(0f, 0f, 400f, 400f)
        val rect = Rect(-10f, -10f, 190f, 190f)
        val result = rect.clampTo(bounds)
        assertEquals(result.width, result.height, 0.01f)
    }
}
