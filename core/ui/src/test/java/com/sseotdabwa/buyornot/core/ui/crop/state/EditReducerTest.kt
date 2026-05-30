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
    fun `CommitRotate로_quarters가_바뀌면_crop은_null로_리셋된다`() {
        val state = EditSpec(rotationQuarters = 0, crop = sampleCrop)
        val next = reduce(state, EditEvent.CommitRotate(1))
        assertEquals(1, next.rotationQuarters)
        assertNull(next.crop)
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
    fun `CommitRotate_quarters_4는_0으로_정규화되어_crop이_리셋된다`() {
        val state = EditSpec(rotationQuarters = 1, crop = sampleCrop)
        val next = reduce(state, EditEvent.CommitRotate(4))
        assertEquals(0, next.rotationQuarters)
        assertNull(next.crop)
    }

    @Test
    fun `CommitRotate는_음수_quarters를_올바르게_정규화한다`() {
        val state = EditSpec(rotationQuarters = 0, crop = null)
        val next = reduce(state, EditEvent.CommitRotate(-1))
        assertEquals(3, next.rotationQuarters)
    }
}
