package com.sseotdabwa.buyornot.feature.upload.util

object LinkValidator {
    private val VALID_URL_REGEX = Regex("^https?://\\S+$")
    private val HANGUL_REGEX = Regex("[\uAC00-\uD7A3\u1100-\u11FF\u3130-\u318F]")

    fun isValid(url: String): Boolean {
        if (url.isEmpty()) return true
        if (!VALID_URL_REGEX.matches(url)) return false
        if (HANGUL_REGEX.containsMatchIn(url)) return false
        return true
    }
}
