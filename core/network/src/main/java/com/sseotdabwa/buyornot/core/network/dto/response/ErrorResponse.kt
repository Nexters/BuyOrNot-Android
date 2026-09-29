package com.sseotdabwa.buyornot.core.network.dto.response

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import retrofit2.HttpException

@Serializable
private data class ErrorResponse(
    val message: String? = null,
    val errorCode: String? = null,
)

/** 에러 응답 본문에서 꺼낸 서버 에러 코드와 메시지. */
data class ApiError(
    val code: String?,
    val message: String?,
)

private val errorJson = Json { ignoreUnknownKeys = true }

/** 에러 본문은 한 번만 읽을 수 있으므로 코드와 메시지가 둘 다 필요하면 이 함수를 쓴다. */
fun HttpException.apiErrorOrNull(): ApiError? =
    runCatching {
        response()?.errorBody()?.string()?.let {
            val body = errorJson.decodeFromString<ErrorResponse>(it)
            ApiError(code = body.errorCode, message = body.message)
        }
    }.getOrNull()

fun HttpException.errorCodeOrNull(): String? = apiErrorOrNull()?.code
