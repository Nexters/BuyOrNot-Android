package com.sseotdabwa.buyornot.core.common.util

import org.junit.Test
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals
import kotlin.test.assertNull

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
    fun `7일_초과_14일_미만일_경우에도_1주_전으로_표시된다`() {
        val past = now.minusDays(8)
        val isoString = past.format(DateTimeFormatter.ISO_DATE_TIME)
        val result = TimeUtils.formatRelativeTime(isoString, now)
        assertEquals("1주 전", result)
    }

    @Test
    fun `14일_이상일_경우_절대_날짜로_표시된다`() {
        val past = now.minusDays(14)
        val isoString = past.format(DateTimeFormatter.ISO_DATE_TIME)
        val result = TimeUtils.formatRelativeTime(isoString, now)
        assertEquals("2026.2.8", result)
    }

    @Test
    fun `오프셋_없는_서버_시각은_한국_시간으로_읽는다`() {
        assertEquals(
            Instant.parse("2026-10-30T08:13:45Z").toEpochMilli(),
            TimeUtils.parseEpochMillis("2026-10-30T17:13:45"),
        )
    }

    @Test
    fun `소수점_초가_붙은_서버_시각도_읽는다`() {
        assertEquals(
            Instant.parse("2026-10-30T08:13:45.971Z").toEpochMilli(),
            TimeUtils.parseEpochMillis("2026-10-30T17:13:45.971008"),
        )
    }

    @Test
    fun `오프셋이_있는_시각은_그_오프셋대로_읽는다`() {
        val expected = Instant.parse("2026-10-30T08:13:45Z").toEpochMilli()
        assertEquals(expected, TimeUtils.parseEpochMillis("2026-10-30T08:13:45Z"))
        assertEquals(expected, TimeUtils.parseEpochMillis("2026-10-30T17:13:45+09:00"))
    }

    @Test
    fun `시각으로_읽을_수_없으면_null을_돌려준다`() {
        assertNull(TimeUtils.parseEpochMillis(""))
        assertNull(TimeUtils.parseEpochMillis("not-a-date"))
    }

    @Test
    fun `남은_시간이_0이면_00시_00분_00초로_표시한다`() {
        assertEquals("00:00:00", TimeUtils.formatRemainingTime(0L))
    }

    @Test
    fun `남은_시간의_시는_24를_넘어도_그대로_쌓는다`() {
        val remaining =
            Duration
                .ofHours(47)
                .plusMinutes(59)
                .plusSeconds(50)
                .toMillis()
        assertEquals("47:59:50", TimeUtils.formatRemainingTime(remaining))
    }

    @Test
    fun `1초가_안_되는_자투리는_올린다`() {
        assertEquals("00:01:00", TimeUtils.formatRemainingTime(59_001L))
    }

    @Test
    fun `마감_직전_1초_미만이_남으면_00시_00분_01초로_표시한다`() {
        assertEquals("00:00:01", TimeUtils.formatRemainingTime(1L))
    }

    @Test
    fun `이미_지난_시각은_00시_00분_00초로_고정한다`() {
        assertEquals("00:00:00", TimeUtils.formatRemainingTime(-5_000L))
    }
}
