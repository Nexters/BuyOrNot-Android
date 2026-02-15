package com.sseotdabwa.buyornot.core.network.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class BaseResponse<T>(
    val data: T,
    val message: String,
    val status: String,
    val errorCode: String? = null,
)
