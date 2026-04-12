package com.sseotdabwa.buyornot.feature.upload.util

object LinkValidator {
    private val VALID_URL_REGEX = Regex("^https?://\\S+$")

    fun isValid(url: String): Boolean {
        if (url.isEmpty()) return true
        return VALID_URL_REGEX.matches(url)
    }
}
