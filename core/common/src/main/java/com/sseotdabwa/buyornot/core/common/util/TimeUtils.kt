package com.sseotdabwa.buyornot.core.common.util

import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object TimeUtils {
    /**
     * ISO 8601 형식의 날짜/시간 문자열을 상대 시간으로 변환합니다.
     *
     * @param isoString UTC 시간대 기준의 ISO 8601 문자열 (예: "2026-02-22T10:00:00Z")
     * @return 정책에 따라 변환된 상대 시간 문자열
     */
    fun formatRelativeTime(isoString: String): String {
        // ZonedDateTime으로 파싱하여 시스템 기본 시간대로 변환
        val dateTime = LocalDateTime.parse(isoString, DateTimeFormatter.ISO_DATE_TIME)
        val now = LocalDateTime.now(ZoneId.of("UTC"))

        val duration = Duration.between(dateTime, now)
        val minutes = duration.toMinutes()
        val hours = duration.toHours()
        val days = duration.toDays()

        return when {
            minutes < 1 -> "방금 전"
            minutes < 60 -> "${minutes}분 전"
            hours < 24 -> "${hours}시간 전"
            days < 7 -> "${days}일 전"
            days == 7L -> "1주 전"
            else -> {
                val formatter = DateTimeFormatter.ofPattern("yyyy.M.d")
                dateTime.format(formatter)
            }
        }
    }
}
