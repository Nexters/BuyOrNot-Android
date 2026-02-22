package com.sseotdabwa.buyornot.core.common.util

import java.time.Duration
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * 시간 및 날짜 관련 유틸리티 클래스
 */
object TimeUtils {

    private val KOREA_ZONE = ZoneId.of("Asia/Seoul")

    /**
     * ISO 8601 형식의 날짜/시간 문자열을 현재 시간 기준의 상대 시간으로 변환합니다.
     *
     * 파싱 규칙:
     * 1. 오프셋이 포함된 경우(예: 'Z', '+09:00') UTC 기준으로 변환하여 계산합니다.
     * 2. 오프셋이 없는 경우 UTC로 간주하여 LocalDateTime으로 파싱합니다.
     * 3. 파싱 실패 시 원본 문자열을 반환합니다.
     *
     * 변환 규칙:
     * - 1분 미만: '방금 전'
     * - 1분 이상 60분 미만: 'N분 전'
     * - 1시간 이상 24시간 미만: 'N시간 전'
     * - 1일 이상 7일 미만: 'N일 전'
     * - 7일 이상 14일 미만: '1주 전'
     * - 그 외: 'yyyy.M.d' (절대 날짜)
     *
     * @param isoString ISO 8601 형식의 날짜/시간 문자열
     * @param now 비교 기준 시간 (기본값: 한국 시간대 기준 현재 시간)
     * @return 정책에 따라 변환된 상대 시간 또는 절대 날짜 문자열
     */
    fun formatRelativeTime(
        isoString: String,
        now: LocalDateTime = LocalDateTime.now(KOREA_ZONE),
    ): String {
        val dateTime = runCatching {
            // 1. 오프셋이 있으면 UTC로 변환 후 한국 시간으로 맞춤 (안정성 강화)
            OffsetDateTime.parse(isoString)
                .atZoneSameInstant(KOREA_ZONE)
                .toLocalDateTime()
        }.recoverCatching {
            // 2. 오프셋이 없으면 서버가 한국 시간(KST)으로 보냈다고 가정하고 파싱
            LocalDateTime.parse(isoString)
        }.getOrElse {
            return isoString
        }

        // 미래 시간 예외 처리 (isAfter 사용)
        if (dateTime.isAfter(now)) {
            val remainingDays = Duration.between(now, dateTime).toDays()
            return if (remainingDays == 0L) "오늘 종료 예정" else "${remainingDays}일 후 종료"
        }

        val duration = Duration.between(dateTime, now)
        val minutes = duration.toMinutes()
        val hours = duration.toHours()
        val days = duration.toDays()
        val seconds = duration.seconds

        return when {
            minutes < 1 -> "방금 전"
            minutes in 1..59 -> "${minutes}분 전"
            hours in 1..23 -> "${hours}시간 전"
            days in 1..6 -> "${days}일 전"
            days in 7..13 -> "1주 전"
            else -> dateTime.format(DateTimeFormatter.ofPattern("yyyy.M.d"))
        }
    }
}
