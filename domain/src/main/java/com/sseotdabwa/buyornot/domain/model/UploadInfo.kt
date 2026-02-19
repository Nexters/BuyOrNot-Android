package com.sseotdabwa.buyornot.domain.model

data class UploadInfo(
    val uploadUrl: String,
    val s3ObjectKey: String,
    val viewUrl: String,
)
