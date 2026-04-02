package com.sseotdabwa.buyornot.domain.model

data class AppUpdateInfo(
    val latestVersion: Int,
    val minimumVersion: Int,
    val updateStrategy: UpdateStrategy,
)

enum class UpdateStrategy {
    NONE,
    SOFT,
    FORCE,
}
