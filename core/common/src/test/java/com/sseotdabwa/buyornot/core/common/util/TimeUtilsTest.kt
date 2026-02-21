package com.sseotdabwa.buyornot.core.common.util

import org.junit.Test
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals

class TimeUtilsTest {
    private val now = LocalDateTime.of(2026, 2, 22, 12, 0, 0)

    @Test
    fun `1분 미만일 경우 방금 전으로 표시된다`() {
        val past = now.minusSeconds(30)
        val isoString = past.format(DateTimeFormatter.ISO_DATE_TIME)
        val result = TimeUtils.formatRelativeTime(isoString, now)
        assertEquals("방금 전", result)
    }

    @Test
    fun `1분 이상 60분 미만일 경우 N분 전으로 표시된다`() {
        val past = now.minusMinutes(30)
        val isoString = past.format(DateTimeFormatter.ISO_DATE_TIME)
        val result = TimeUtils.formatRelativeTime(isoString, now)
        assertEquals("30분 전", result)
    }

    @Test
    fun `1시간 이상 24시간 미만일 경우 N시간 전으로 표시된다`() {
        val past = now.minusHours(5)
        val isoString = past.format(DateTimeFormatter.ISO_DATE_TIME)
        val result = TimeUtils.formatRelativeTime(isoString, now)
        assertEquals("5시간 전", result)
    }

    @Test
    fun `24시간 이상 7일 미만일 경우 N일 전으로 표시된다`() {
        val past = now.minusDays(3)
        val isoString = past.format(DateTimeFormatter.ISO_DATE_TIME)
        val result = TimeUtils.formatRelativeTime(isoString, now)
        assertEquals("3일 전", result)
    }

    @Test
    fun `정확히 7일 전일 경우 1주 전으로 표시된다`() {
        val past = now.minusDays(7)
        val isoString = past.format(DateTimeFormatter.ISO_DATE_TIME)
        val result = TimeUtils.formatRelativeTime(isoString, now)
        assertEquals("1주 전", result)
    }

    @Test
    fun `7일을 초과할 경우 절대 날짜로 표시된다`() {
        val past = now.minusDays(8)
        val isoString = past.format(DateTimeFormatter.ISO_DATE_TIME)
        val result = TimeUtils.formatRelativeTime(isoString, now)
        assertEquals("2026.2.14", result)
    }
}
