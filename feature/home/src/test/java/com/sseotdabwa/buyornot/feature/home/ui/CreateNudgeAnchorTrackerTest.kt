package com.sseotdabwa.buyornot.feature.home.ui

import org.junit.Test
import kotlin.test.assertEquals

class CreateNudgeAnchorTrackerTest {
    private val feeds = listOf("A", "B", "C")

    private fun observation(
        visible: Boolean,
        index: Int = 0,
        offset: Int = 0,
        content: Any? = feeds,
    ) = CreateNudgeAnchorObservation(
        isAnchorVisible = visible,
        firstVisibleItemIndex = index,
        firstVisibleItemScrollOffset = offset,
        content = content,
    )

    private fun CreateNudgeAnchorTracker.observeAll(vararg observations: CreateNudgeAnchorObservation): List<Boolean> =
        observations.map(::observe)

    @Test
    fun `스크롤로_경계가_새로_보이면_알린다`() {
        val results =
            CreateNudgeAnchorTracker().observeAll(
                observation(visible = false),
                observation(visible = false, offset = 300),
                observation(visible = true, offset = 600),
            )

        assertEquals(listOf(false, false, true), results)
    }

    @Test
    fun `첫_관측은_알리지_않는다`() {
        assertEquals(false, CreateNudgeAnchorTracker().observe(observation(visible = true)))
    }

    @Test
    fun `목록을_불러와_경계가_보이게_되면_스크롤이_아니므로_알리지_않는다`() {
        val results =
            CreateNudgeAnchorTracker().observeAll(
                observation(visible = false, content = emptyList<String>()),
                observation(visible = true, content = feeds),
            )

        assertEquals(listOf(false, false), results)
    }

    @Test
    fun `새로고침으로_맨_앞에_피드가_끼어_위치가_바뀌어도_스크롤이_아니므로_알리지_않는다`() {
        val results =
            CreateNudgeAnchorTracker().observeAll(
                observation(visible = false, index = 0),
                observation(visible = true, index = 1, content = listOf("N") + feeds),
            )

        assertEquals(listOf(false, false), results)
    }

    @Test
    fun `경계가_안_보일_때_스크롤한_뒤_새로고침으로_보이게_되면_알리지_않는다`() {
        val results =
            CreateNudgeAnchorTracker().observeAll(
                observation(visible = false),
                observation(visible = false, offset = 100),
                observation(visible = true, index = 1, offset = 100, content = listOf("N") + feeds),
            )

        assertEquals(listOf(false, false, false), results)
    }

    @Test
    fun `스크롤_뒤_헤더가_접히면서_위치_변화_없이_경계가_드러나면_알린다`() {
        val results =
            CreateNudgeAnchorTracker().observeAll(
                observation(visible = false),
                observation(visible = false, offset = 40),
                observation(visible = true, offset = 40),
            )

        assertEquals(listOf(false, false, true), results)
    }

    @Test
    fun `진입_때부터_보이던_경계는_계속_보이는_채로_스크롤해도_알리지_않는다`() {
        val results =
            CreateNudgeAnchorTracker().observeAll(
                observation(visible = true),
                observation(visible = true, offset = 100),
                observation(visible = true, offset = 200),
            )

        assertEquals(listOf(false, false, false), results)
    }

    @Test
    fun `보이던_경계를_스크롤로_지나쳤다가_다시_스크롤로_마주치면_알린다`() {
        val results =
            CreateNudgeAnchorTracker().observeAll(
                observation(visible = true),
                observation(visible = false, index = 2),
                observation(visible = true, index = 1, offset = 500),
            )

        assertEquals(listOf(false, false, true), results)
    }
}
