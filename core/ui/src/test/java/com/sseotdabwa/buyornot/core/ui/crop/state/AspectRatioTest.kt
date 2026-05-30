package com.sseotdabwa.buyornot.core.ui.crop.state

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AspectRatioTest {
    @Test
    fun `Free의_targetRatio는_null이다`() {
        assertNull(AspectRatio.Free.targetRatio())
    }

    @Test
    fun `R1x1의_targetRatio는_1f이다`() {
        assertEquals(1f, AspectRatio.R1x1.targetRatio()!!, 0.0001f)
    }

    @Test
    fun `R3x4의_targetRatio는_0_75f이다`() {
        assertEquals(0.75f, AspectRatio.R3x4.targetRatio()!!, 0.0001f)
    }

    @Test
    fun `R4x3의_targetRatio는_약_1_333f이다`() {
        assertEquals(4f / 3f, AspectRatio.R4x3.targetRatio()!!, 0.0001f)
    }
}
