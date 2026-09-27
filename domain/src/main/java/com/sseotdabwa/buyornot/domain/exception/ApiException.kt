package com.sseotdabwa.buyornot.domain.exception

/**
 * 서버가 에러 응답으로 내려준 비즈니스 에러.
 *
 * @property code 서버 에러 코드 (예: `USER_011`). 에러 바디를 해석하지 못하면 null
 */
class ApiException(
    val code: String?,
    message: String?,
    cause: Throwable? = null,
) : Exception(message, cause)
