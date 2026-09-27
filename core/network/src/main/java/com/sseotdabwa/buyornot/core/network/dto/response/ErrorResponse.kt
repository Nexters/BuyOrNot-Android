package com.sseotdabwa.buyornot.core.network.dto.response

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import retrofit2.HttpException

@Serializable
private data class ErrorResponse(
    val message: String? = null,
    val errorCode: String? = null,
)

private val errorJson = Json { ignoreUnknownKeys = true }

/**
 * 2xx가 아닌 응답은 Retrofit이 [HttpException]으로 던져 `BaseResponse.getOrThrow()`까지 오지 않는다.
 * 에러 바디의 `errorCode`를 꺼내야 사용자에게 상황별 문구를 보여줄 수 있다.
 *
 * @return 에러 바디의 `errorCode`. 바디가 없거나 형식이 다르면 null
 */
fun HttpException.errorCodeOrNull(): String? =
    runCatching {
        response()?.errorBody()?.string()?.let { errorJson.decodeFromString<ErrorResponse>(it).errorCode }
    }.getOrNull()
