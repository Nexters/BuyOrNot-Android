package com.sseotdabwa.buyornot.domain.model

data class PostDraft(
    val title: String,
    val content: String,
    val source: PostDraftSource,
)

enum class PostDraftSource {
    ON_DEVICE,
    CLOUD,
}
