package com.sseotdabwa.buyornot.core.ui.crop.state

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class EditReducerTest {
    private val sampleCrop =
        CropSpec(
            ratio = AspectRatio.R1x1,
            rectNormalized = NormalizedRect(0.1f, 0.1f, 0.6f, 0.6f),
        )

    @Test
    fun `초기_EditSpec은_quarters_0과_crop_null이다`() {
        val initial = EditSpec()
        assertEquals(0, initial.rotationQuarters)
        assertNull(initial.crop)
    }

    @Test
    fun `CommitCrop은_editSpec_crop을_새_값으로_교체한다`() {
        val state = EditSpec(rotationQuarters = 1, crop = null)
        val next = reduce(state, EditEvent.CommitCrop(sampleCrop))
        assertEquals(sampleCrop, next.crop)
    }

    @Test
    fun `CommitCrop은_rotationQuarters를_변경하지_않는다`() {
        val state = EditSpec(rotationQuarters = 2, crop = null)
        val next = reduce(state, EditEvent.CommitCrop(sampleCrop))
        assertEquals(2, next.rotationQuarters)
    }

    @Test
    fun `CommitRotate로_quarters가_바뀌면_crop은_새_좌표계로_매핑되어_보존된다`() {
        val crop =
            CropSpec(
                ratio = AspectRatio.R1x1,
                rectNormalized = NormalizedRect(0.1f, 0.2f, 0.6f, 0.8f),
            )
        val state = EditSpec(rotationQuarters = 0, crop = crop)
        val next = reduce(state, EditEvent.CommitRotate(1))
        assertEquals(1, next.rotationQuarters)
        val mapped = next.crop!!.rectNormalized
        // 1회 CCW: (l, t, r, b) -> (t, 1-r, b, 1-l) = (0.2, 0.4, 0.8, 0.9)
        assertEquals(0.2f, mapped.left, 0.0001f)
        assertEquals(0.4f, mapped.top, 0.0001f)
        assertEquals(0.8f, mapped.right, 0.0001f)
        assertEquals(0.9f, mapped.bottom, 0.0001f)
        assertEquals(crop.ratio, next.crop!!.ratio)
    }

    @Test
    fun `CommitRotate로_quarters가_4번_변경되면_crop은_원래_좌표로_돌아온다`() {
        val crop =
            CropSpec(
                ratio = AspectRatio.R3x4,
                rectNormalized = NormalizedRect(0.1f, 0.2f, 0.6f, 0.8f),
            )
        var state = EditSpec(rotationQuarters = 0, crop = crop)
        state = reduce(state, EditEvent.CommitRotate(1))
        state = reduce(state, EditEvent.CommitRotate(2))
        state = reduce(state, EditEvent.CommitRotate(3))
        state = reduce(state, EditEvent.CommitRotate(0))
        assertEquals(crop.rectNormalized.left, state.crop!!.rectNormalized.left, 0.0001f)
        assertEquals(crop.rectNormalized.top, state.crop!!.rectNormalized.top, 0.0001f)
        assertEquals(crop.rectNormalized.right, state.crop!!.rectNormalized.right, 0.0001f)
        assertEquals(crop.rectNormalized.bottom, state.crop!!.rectNormalized.bottom, 0.0001f)
    }

    @Test
    fun `CommitRotate로_quarters가_동일하면_crop은_보존되고_상태_그대로_반환된다`() {
        val state = EditSpec(rotationQuarters = 2, crop = sampleCrop)
        val next = reduce(state, EditEvent.CommitRotate(2))
        assertSame(state, next)
    }

    @Test
    fun `CommitRotate는_quarters를_mod_4로_정규화한다`() {
        val state = EditSpec(rotationQuarters = 3, crop = null)
        val next = reduce(state, EditEvent.CommitRotate(5))
        assertEquals(1, next.rotationQuarters)
    }

    @Test
    fun `CommitRotate_quarters_4는_0으로_정규화되어_crop이_원래_좌표로_매핑된다`() {
        val crop =
            CropSpec(
                ratio = AspectRatio.R1x1,
                rectNormalized = NormalizedRect(0.1f, 0.2f, 0.6f, 0.7f),
            )
        val state = EditSpec(rotationQuarters = 1, crop = crop)
        val next = reduce(state, EditEvent.CommitRotate(4))
        assertEquals(0, next.rotationQuarters)
        // delta = (0 - 1 + 4) % 4 = 3 CCW rotations
        // 1회 CCW: (0.1, 0.2, 0.6, 0.7) -> (0.2, 0.4, 0.7, 0.9)
        // 2회 CCW: (0.2, 0.4, 0.7, 0.9) -> (0.4, 0.3, 0.9, 0.8)
        // 3회 CCW: (0.4, 0.3, 0.9, 0.8) -> (0.3, 0.1, 0.8, 0.6)
        val mapped = next.crop!!.rectNormalized
        assertEquals(0.3f, mapped.left, 0.0001f)
        assertEquals(0.1f, mapped.top, 0.0001f)
        assertEquals(0.8f, mapped.right, 0.0001f)
        assertEquals(0.6f, mapped.bottom, 0.0001f)
    }

    @Test
    fun `CommitRotate는_음수_quarters를_올바르게_정규화한다`() {
        val state = EditSpec(rotationQuarters = 0, crop = null)
        val next = reduce(state, EditEvent.CommitRotate(-1))
        assertEquals(3, next.rotationQuarters)
    }
}
