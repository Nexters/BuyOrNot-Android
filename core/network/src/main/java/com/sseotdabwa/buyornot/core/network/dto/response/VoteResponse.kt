package com.sseotdabwa.buyornot.core.network.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VoteResponse(
    @SerialName("feedId")
    val feedId: Long,
    @SerialName("choice")
    val choice: String, // "YES" or "NO"
    @SerialName("yesCount")
    val yesCount: Int,
    @SerialName("noCount")
    val noCount: Int,
    @SerialName("totalCount")
    val totalCount: Int,
)

