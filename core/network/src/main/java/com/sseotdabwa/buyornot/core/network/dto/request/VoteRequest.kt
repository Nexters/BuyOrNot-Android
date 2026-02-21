package com.sseotdabwa.buyornot.core.network.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VoteRequest(
    @SerialName("choice")
    val choice: String, // "YES" or "NO"
)

