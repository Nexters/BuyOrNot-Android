package com.sseotdabwa.buyornot.core.ui.imageviewer

import org.junit.Assert.assertEquals
import org.junit.Test

class ComputeMaxOffsetTest {
    @Test
    fun `square image square container scale 2 returns half container size`() {
        val (maxX, maxY) =
            computeMaxOffset(
                containerWidth = 400,
                containerHeight = 400,
                imageWidth = 400f,
                imageHeight = 400f,
                scale = 2f,
            )!!
        assertEquals(200f, maxX, 0.01f)
        assertEquals(200f, maxY, 0.01f)
    }

    @Test
    fun `scale 1 returns zero max offset`() {
        val (maxX, maxY) =
            computeMaxOffset(
                containerWidth = 400,
                containerHeight = 400,
                imageWidth = 400f,
                imageHeight = 400f,
                scale = 1f,
            )!!
        assertEquals(0f, maxX, 0.01f)
        assertEquals(0f, maxY, 0.01f)
    }

    // ratio = min(400/800, 400/400) = 0.5
    // renderedW = 400, renderedH = 200
    // scale=2f → maxX = (400*2-400)/2 = 200, maxY = (200*2-400)/2 = 0
    @Test
    fun `wide image letterbox vertical returns zero vertical max offset at scale 2`() {
        val (maxX, maxY) =
            computeMaxOffset(
                containerWidth = 400,
                containerHeight = 400,
                imageWidth = 800f,
                imageHeight = 400f,
                scale = 2f,
            )!!
        assertEquals(200f, maxX, 0.01f)
        assertEquals(0f, maxY, 0.01f)
    }

    @Test
    fun `zero container returns null`() {
        val result =
            computeMaxOffset(
                containerWidth = 0,
                containerHeight = 400,
                imageWidth = 400f,
                imageHeight = 400f,
                scale = 2f,
            )
        assertEquals(null, result)
    }

    @Test
    fun `NaN image size returns null`() {
        val result =
            computeMaxOffset(
                containerWidth = 400,
                containerHeight = 400,
                imageWidth = Float.NaN,
                imageHeight = 400f,
                scale = 2f,
            )
        assertEquals(null, result)
    }
}
