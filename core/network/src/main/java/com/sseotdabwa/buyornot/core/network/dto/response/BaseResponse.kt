package com.sseotdabwa.buyornot.core.network.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class BaseResponse<T>(
    val data: T? = null,
    val message: String,
    val status: String,
    val errorCode: String? = null,
)

/**
 * API 응답에 `errorCode`가 있을 경우 예외를 던집니다.
 * 데이터가 없는 응답(`BaseResponse<Unit>`)을 위해 오버로딩 되었습니다.
 *
 * @throws Exception API 응답에 `errorCode`가 포함된 경우
 */
fun BaseResponse<Unit>.getOrThrow() {
    if (errorCode != null) {
        throw Exception("API Error Code: $errorCode, Message: $message")
    }
}

/**
 * BaseResponse에서 데이터를 안전하게 추출하거나, 실패 시 예외를 던집니다.
 * `errorCode` 확인 로직을 포함하며, 데이터가 있는 응답을 위한 함수입니다.
 *
 * @return `data` 필드 (non-null)
 * @throws IllegalStateException `data` 필드가 null일 경우
 * @throws Exception API 응답에 `errorCode`가 포함된 경우
 */
fun <T> BaseResponse<T>.getOrThrow(): T {
    if (errorCode != null) {
        throw Exception("API Error Code: $errorCode, Message: $message")
    }
    return data ?: throw IllegalStateException("Response data is null")
}
